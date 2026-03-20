package br.com.sistema.bot.engine;

import br.com.sistema.bot.entity.FluxoConexao;
import br.com.sistema.bot.entity.FluxoMensagem;
import br.com.sistema.bot.entity.FluxoNode;
import br.com.sistema.bot.enums.TipoNode;
import br.com.sistema.bot.repository.FluxoNodeRepository;
import br.com.sistema.bot.service.BotTemplateService;
import br.com.sistema.bot.service.ConversationStateService;
import br.com.sistema.bot.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Motor de execução do fluxo dinâmico.
 *
 * Responsabilidades:
 *  - Encontrar o nó atual da conversa
 *  - Delegar para o NodeExecutor correto
 *  - Enviar as mensagens de entrada de um nó
 *  - Realizar transições entre nós (com auto-execute em cadeia)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FluxoEngine {

    private final FluxoNodeRepository nodeRepository;
    private final ConversationStateService stateService;
    private final BotTemplateService templateService;
    private final WhatsAppService whatsAppService;
    private final List<NodeExecutor> executors;

    // ─────────────────────────────────────────────────────────────────────────
    // Ponto de entrada — chamado pelo WebhookService
    // ─────────────────────────────────────────────────────────────────────────

    public void processar(FluxoExecucaoCtx ctx) {
        // Comando global: "sair" ou "cancelar" em qualquer estado
        String input = ctx.input() != null ? ctx.input().trim().toLowerCase() : "";
        if ("sair".equals(input) || "cancelar".equals(input)) {
            FluxoNode encerrarNode = nodeRepository
                    .findFirstByTipo(TipoNode.ENCERRAMENTO)
                    .orElseThrow(() -> new IllegalStateException("Nó ENCERRAMENTO não encontrado no fluxo"));
            executor(TipoNode.ENCERRAMENTO).executar(ctx, encerrarNode, this);
            return;
        }

        String nodeKey = stateService.buscarOuCriar(ctx.phone()).getCurrentNodeKey();
        FluxoNode currentNode = nodeRepository.findByNodeKey(nodeKey)
                .orElseGet(() -> nodeRepository.findByNodeKey("menu_inicial")
                        .orElseThrow(() -> new IllegalStateException("Nó menu_inicial não encontrado")));

        log.info("Executando nó '{}' para {} | input='{}'", currentNode.getNodeKey(), ctx.phone(), ctx.input());
        executor(currentNode.getTipo()).executar(ctx, currentNode, this);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Transição para um nó destino (chamada pelos executores)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Transita para o nó alvo.
     * - Persiste o novo nodeKey
     * - Se o nó destino for auto-execute, executa imediatamente (sem aguardar input)
     * - Caso contrário, envia as mensagens de entrada do nó e aguarda o próximo input
     */
    public void transicionarPara(FluxoExecucaoCtx ctx, FluxoNode targetNode) {
        transicionarPara(ctx, targetNode, Map.of());
    }

    public void transicionarPara(FluxoExecucaoCtx ctx, FluxoNode targetNode,
                                  Map<String, String> variaveis) {
        log.debug("Transição: {} → '{}'", ctx.phone(), targetNode.getNodeKey());
        stateService.setNodeKey(ctx.phone(), targetNode.getNodeKey());

        if (targetNode.getTipo().isAutoExecute()) {
            executor(targetNode.getTipo()).executar(ctx, targetNode, this);
        } else {
            enviarMensagensDoNo(ctx, targetNode, variaveis);
        }
    }

    /**
     * Transita persistindo também o contextData (ex: CPF entre nós).
     */
    public void transicionarComContexto(FluxoExecucaoCtx ctx, FluxoNode targetNode,
                                         String contextData, Map<String, String> variaveis) {
        log.debug("Transição com contexto: {} → '{}'", ctx.phone(), targetNode.getNodeKey());
        stateService.setNodeKeyComContexto(ctx.phone(), targetNode.getNodeKey(), contextData);

        if (targetNode.getTipo().isAutoExecute()) {
            FluxoExecucaoCtx ctxComContexto = new FluxoExecucaoCtx(
                    ctx.phone(), ctx.messageId(), ctx.senderName(), ctx.input(), contextData);
            executor(targetNode.getTipo()).executar(ctxComContexto, targetNode, this);
        } else {
            enviarMensagensDoNo(ctx, targetNode, variaveis);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers públicos usados pelos executores
    // ─────────────────────────────────────────────────────────────────────────

    /** Envia todas as mensagens do nó em ordem, sem variáveis. */
    public void enviarMensagensDoNo(FluxoExecucaoCtx ctx, FluxoNode node) {
        enviarMensagensDoNo(ctx, node, Map.of());
    }

    /** Envia todas as mensagens do nó em ordem, substituindo placeholders. */
    public void enviarMensagensDoNo(FluxoExecucaoCtx ctx, FluxoNode node,
                                     Map<String, String> variaveis) {
        for (FluxoMensagem m : node.getMensagens()) {
            String texto = variaveis.isEmpty()
                    ? templateService.buscarTexto(m.getTemplateChave())
                    : templateService.buscarTexto(m.getTemplateChave(), variaveis);
            whatsAppService.enviarTexto(ctx.phone(), texto);
        }
    }

    /**
     * Retorna a primeira conexão de saída do nó cuja condição bata com o input.
     * Prioridade: match exato → "default".
     */
    public FluxoConexao encontrarConexao(FluxoNode node, String condicao) {
        List<FluxoConexao> saidas = node.getConexoesSaida();

        return saidas.stream()
                .filter(c -> c.getCondicao().equalsIgnoreCase(condicao))
                .findFirst()
                .or(() -> saidas.stream()
                        .filter(c -> "default".equalsIgnoreCase(c.getCondicao()))
                        .findFirst())
                .orElse(null);
    }

    /** Busca nó pelo nodeKey, lança exceção se não encontrar. */
    public FluxoNode buscarNo(String nodeKey) {
        return nodeRepository.findByNodeKey(nodeKey)
                .orElseThrow(() -> new IllegalStateException("Nó não encontrado: " + nodeKey));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Interno
    // ─────────────────────────────────────────────────────────────────────────

    private NodeExecutor executor(TipoNode tipo) {
        return executors.stream()
                .filter(e -> e.getTipo() == tipo)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Executor não encontrado para tipo: " + tipo));
    }
}
