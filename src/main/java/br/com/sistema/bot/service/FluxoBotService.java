package br.com.sistema.bot.service;

import br.com.sistema.bot.dtos.response.FluxoArestaResponse;
import br.com.sistema.bot.dtos.response.FluxoBotResponse;
import br.com.sistema.bot.dtos.response.FluxoNodeResponse;
import br.com.sistema.bot.dtos.response.FluxoTemplateResponse;
import br.com.sistema.bot.entity.BotTemplate;
import br.com.sistema.bot.entity.FluxoConexao;
import br.com.sistema.bot.entity.FluxoMensagem;
import br.com.sistema.bot.entity.FluxoNode;
import br.com.sistema.bot.repository.BotTemplateRepository;
import br.com.sistema.bot.repository.FluxoConexaoRepository;
import br.com.sistema.bot.repository.FluxoMensagemRepository;
import br.com.sistema.bot.repository.FluxoNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FluxoBotService {

    private final FluxoNodeRepository nodeRepository;
    private final FluxoMensagemRepository mensagemRepository;
    private final FluxoConexaoRepository conexaoRepository;
    private final BotTemplateRepository templateRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // GET /fluxo — leitura completa do grafo com templates resolvidos
    // ─────────────────────────────────────────────────────────────────────────

    public FluxoBotResponse construirFluxo() {
        Map<String, BotTemplate> templateMap = templateRepository.findAll().stream()
                .collect(Collectors.toMap(BotTemplate::getChave, t -> t));

        List<FluxoNode> nodes = nodeRepository.findAllByAtivoTrue();

        List<FluxoNodeResponse> nodeResponses = nodes.stream()
                .map(n -> toNodeResponse(n, templateMap))
                .toList();

        List<FluxoArestaResponse> arestaResponses = nodes.stream()
                .flatMap(n -> n.getConexoesSaida().stream())
                .map(this::toArestaResponse)
                .toList();

        return new FluxoBotResponse(nodeResponses, arestaResponses);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Edição de nós
    // ─────────────────────────────────────────────────────────────────────────

    public FluxoNode atualizarPosicao(String nodeId, int posX, int posY, String label) {
        FluxoNode node = nodeRepository.findById(java.util.UUID.fromString(nodeId))
                .orElseThrow(() -> new IllegalArgumentException("Nó não encontrado: " + nodeId));
        node.setPosX(posX);
        node.setPosY(posY);
        if (label != null && !label.isBlank()) node.setLabel(label);
        return nodeRepository.save(node);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Edição de mensagens
    // ─────────────────────────────────────────────────────────────────────────

    public FluxoMensagem adicionarMensagem(String nodeId, String templateChave, int ordem) {
        FluxoNode node = nodeRepository.findById(java.util.UUID.fromString(nodeId))
                .orElseThrow(() -> new IllegalArgumentException("Nó não encontrado: " + nodeId));
        FluxoMensagem m = FluxoMensagem.builder()
                .node(node)
                .templateChave(templateChave)
                .ordem(ordem)
                .build();
        node.getMensagens().add(m);
        nodeRepository.save(node);
        return m;
    }

    public void removerMensagem(String mensagemId) {
        mensagemRepository.deleteById(java.util.UUID.fromString(mensagemId));
    }

    public FluxoMensagem atualizarOrdemMensagem(String mensagemId, int novaOrdem) {
        FluxoMensagem m = mensagemRepository.findById(java.util.UUID.fromString(mensagemId))
                .orElseThrow(() -> new IllegalArgumentException("Mensagem não encontrada: " + mensagemId));
        m.setOrdem(novaOrdem);
        return mensagemRepository.save(m);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Edição de conexões
    // ─────────────────────────────────────────────────────────────────────────

    public FluxoConexao criarConexao(java.util.UUID deNodeId, java.util.UUID paraNodeId,
                                      String condicao, String label, int ordem) {
        FluxoNode deNode   = nodeRepository.findById(deNodeId)
                .orElseThrow(() -> new IllegalArgumentException("Nó origem não encontrado"));
        FluxoNode paraNode = nodeRepository.findById(paraNodeId)
                .orElseThrow(() -> new IllegalArgumentException("Nó destino não encontrado"));

        FluxoConexao conexao = FluxoConexao.builder()
                .deNode(deNode)
                .paraNode(paraNode)
                .condicao(condicao)
                .label(label)
                .ordem(ordem)
                .build();
        deNode.getConexoesSaida().add(conexao);
        nodeRepository.save(deNode);
        return conexao;
    }

    public FluxoConexao atualizarConexao(String conexaoId, String condicao, String label, int ordem) {
        FluxoConexao c = conexaoRepository.findById(java.util.UUID.fromString(conexaoId))
                .orElseThrow(() -> new IllegalArgumentException("Conexão não encontrada: " + conexaoId));
        c.setCondicao(condicao);
        if (label != null) c.setLabel(label);
        c.setOrdem(ordem);
        return conexaoRepository.save(c);
    }

    public void removerConexao(String conexaoId) {
        conexaoRepository.deleteById(java.util.UUID.fromString(conexaoId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mapeamento para DTOs de resposta
    // ─────────────────────────────────────────────────────────────────────────

    private FluxoNodeResponse toNodeResponse(FluxoNode n, Map<String, BotTemplate> templateMap) {
        List<FluxoTemplateResponse> mensagens = n.getMensagens().stream()
                .map(m -> resolverTemplate(m, templateMap))
                .toList();
        return new FluxoNodeResponse(
                n.getId().toString(), n.getLabel(), null,
                n.getTipo(), n.getPosX(), n.getPosY(),
                mensagens, n.getEquipeTransferencia(), n.getTipoHorario());
    }

    private FluxoTemplateResponse resolverTemplate(FluxoMensagem m, Map<String, BotTemplate> map) {
        BotTemplate t = map.get(m.getTemplateChave());
        if (t == null) return new FluxoTemplateResponse(m.getTemplateChave(), "[" + m.getTemplateChave() + "]", null);
        return new FluxoTemplateResponse(t.getChave(), t.getTexto(), t.getDescricao());
    }

    private FluxoArestaResponse toArestaResponse(FluxoConexao c) {
        return new FluxoArestaResponse(
                c.getId().toString(),
                c.getDeNode().getId().toString(),
                c.getParaNode().getId().toString(),
                c.getCondicao(),
                c.getLabel()
        );
    }
}
