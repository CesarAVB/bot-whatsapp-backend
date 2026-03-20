package br.com.sistema.bot.config;

import br.com.sistema.bot.entity.FluxoConexao;
import br.com.sistema.bot.entity.FluxoMensagem;
import br.com.sistema.bot.entity.FluxoNode;
import br.com.sistema.bot.enums.TipoNode;
import br.com.sistema.bot.repository.FluxoNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Popula as tabelas fluxo_node / fluxo_mensagem / fluxo_conexao com o fluxo
 * padrão do bot na primeira inicialização.
 * Só insere se o nó ainda não existir — nunca sobrescreve alterações feitas pelo painel.
 *
 * Order(2) garante execução após BotTemplateDataInitializer (Order padrão = mais alto).
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class FluxoDataInitializer implements ApplicationRunner {

    private final FluxoNodeRepository nodeRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Inicializando fluxo dinâmico do bot...");

        // ── Criar nós ──────────────────────────────────────────────────────────
        FluxoNode menuInicial       = criarNo("menu_inicial",      "Menu Inicial",                    TipoNode.MENU,          0,    400, null,             null,                   null);
        FluxoNode souCliente        = criarNo("sou_cliente",       "Sou Cliente",                     TipoNode.MENU,          300,  200, null,             null,                   null);
        FluxoNode financeiro        = criarNo("financeiro",        "Menu Financeiro",                 TipoNode.MENU,          600,  0,   null,             null,                   null);
        FluxoNode aguardaCpfFatura  = criarNo("aguarda_cpf_fatura","Aguarda CPF (Fatura)",            TipoNode.AGUARDA_INPUT, 900, -200, null,             null,                   null);
        FluxoNode aguardaCpfDesb    = criarNo("aguarda_cpf_desbloqueio","Aguarda CPF (Desbloqueio)",  TipoNode.AGUARDA_INPUT, 900,  50,  null,             null,                   null);
        FluxoNode confirmFatura     = criarNo("confirma_identidade_fatura","Confirma Titular (Fatura)",TipoNode.CONFIRMACAO, 1200,-200, null,             null,                   null);
        FluxoNode confirmDesb       = criarNo("confirma_identidade_desbloqueio","Confirma Titular (Desbloqueio)",TipoNode.CONFIRMACAO,1200,50,null,        null,                   null);
        FluxoNode resultadoFatura   = criarNo("resultado_fatura",  "Enviar Fatura",                   TipoNode.RESULTADO_API, 1500,-200, null,             null,                   "buscar_fatura");
        FluxoNode resultadoDesb     = criarNo("resultado_desbloqueio","Executar Desbloqueio",         TipoNode.RESULTADO_API, 1500, 50,  null,             null,                   "desbloquear");
        FluxoNode comprovante       = criarNo("comprovante",       "Instrução Comprovante",           TipoNode.MENSAGEM,      900,  250, null,             null,                   null);
        FluxoNode transferComercial = criarNo("transfer_comercial","Transfer → Comercial",            TipoNode.TRANSFERENCIA, 300,  650, "DUVIDAS_COMERCIAL", null,               null);
        FluxoNode transferSuporte   = criarNo("transfer_suporte",  "Transfer → Suporte Técnico",      TipoNode.TRANSFERENCIA, 600,  250, "SUPORTE",        "SUPORTE",              null);
        FluxoNode transferDuvidas   = criarNo("transfer_duvidas",  "Transfer → Dúvidas/Sugestões",    TipoNode.TRANSFERENCIA, 600,  450, "DUVIDAS_COMERCIAL","FINANCEIRO_COMERCIAL",null);
        FluxoNode transferCancel    = criarNo("transfer_cancelamento","Transfer → Cancelamento",      TipoNode.TRANSFERENCIA, 600,  650, "CANCELAMENTO",   "FINANCEIRO_COMERCIAL", null);
        FluxoNode transferFinanc    = criarNo("transfer_financeiro","Transfer → Financeiro",          TipoNode.TRANSFERENCIA, 900,  450, "FINANCEIRO",     "FINANCEIRO_COMERCIAL", null);
        FluxoNode encerrado         = criarNo("encerrado",         "Encerrar Atendimento",            TipoNode.ENCERRAMENTO,  600,  850, null,             null,                   null);

        // ── Mensagens por nó ──────────────────────────────────────────────────
        adicionarMensagem(menuInicial,       "menu.inicial.boas_vindas",         0);
        adicionarMensagem(souCliente,        "menu.inicial.opcoes_cliente",      0);
        adicionarMensagem(financeiro,        "sou_cliente.menu_financeiro",      0);
        adicionarMensagem(aguardaCpfFatura,  "financeiro.solicitar_cpf",         0);
        adicionarMensagem(aguardaCpfDesb,    "financeiro.solicitar_cpf",         0);
        adicionarMensagem(confirmFatura,     "cpf.confirma_titular",             0);
        adicionarMensagem(confirmDesb,       "cpf.confirma_titular",             0);
        adicionarMensagem(resultadoFatura,   "confirma.fatura",                  0);
        adicionarMensagem(resultadoFatura,   "confirma.sem_fatura",              1);
        adicionarMensagem(resultadoDesb,     "confirma.desbloqueio_sucesso",     0);
        adicionarMensagem(resultadoDesb,     "confirma.desbloqueio_falha",       1);
        adicionarMensagem(comprovante,       "financeiro.comprovante",           0);
        adicionarMensagem(transferComercial, "menu.inicial.comercial_transfer",  0);
        adicionarMensagem(transferSuporte,   "sou_cliente.transfer",             0);
        adicionarMensagem(transferDuvidas,   "sou_cliente.transfer",             0);
        adicionarMensagem(transferCancel,    "sou_cliente.transfer",             0);
        adicionarMensagem(transferFinanc,    "financeiro.transfer",              0);
        adicionarMensagem(encerrado,         "encerrar.despedida",               0);

        // ── Conexões (arestas) ────────────────────────────────────────────────
        Map<String, FluxoNode> nos = Map.ofEntries(
            Map.entry("menu_inicial",                  menuInicial),
            Map.entry("sou_cliente",                   souCliente),
            Map.entry("financeiro",                    financeiro),
            Map.entry("aguarda_cpf_fatura",            aguardaCpfFatura),
            Map.entry("aguarda_cpf_desbloqueio",       aguardaCpfDesb),
            Map.entry("confirma_identidade_fatura",    confirmFatura),
            Map.entry("confirma_identidade_desbloqueio", confirmDesb),
            Map.entry("resultado_fatura",              resultadoFatura),
            Map.entry("resultado_desbloqueio",         resultadoDesb),
            Map.entry("comprovante",                   comprovante),
            Map.entry("transfer_comercial",            transferComercial),
            Map.entry("transfer_suporte",              transferSuporte),
            Map.entry("transfer_duvidas",              transferDuvidas),
            Map.entry("transfer_cancelamento",         transferCancel),
            Map.entry("transfer_financeiro",           transferFinanc),
            Map.entry("encerrado",                     encerrado)
        );

        // menu_inicial
        conectar(nos, "menu_inicial", "sou_cliente",        "1", "1 – Sou cliente ASB",             0);
        conectar(nos, "menu_inicial", "transfer_comercial", "2", "2 – Quero me tornar cliente",      1);

        // sou_cliente
        conectar(nos, "sou_cliente", "transfer_suporte",     "1", "1 – Assistência Técnica",         0);
        conectar(nos, "sou_cliente", "financeiro",           "2", "2 – Financeiro",                  1);
        conectar(nos, "sou_cliente", "transfer_duvidas",     "3", "3 – Dúvidas/Sugestões",           2);
        conectar(nos, "sou_cliente", "transfer_cancelamento","4", "4 – Cancelamento",                3);
        conectar(nos, "sou_cliente", "encerrado",            "5", "5 – Encerrar",                    4);

        // financeiro
        conectar(nos, "financeiro", "aguarda_cpf_fatura",        "1", "1 – Segunda via de boleto",  0);
        conectar(nos, "financeiro", "aguarda_cpf_desbloqueio",   "2", "2 – Desbloqueio de confiança",1);
        conectar(nos, "financeiro", "comprovante",               "3", "3 – Enviar comprovante",      2);
        conectar(nos, "financeiro", "transfer_financeiro",       "4", "4 – Falar com atendente",     3);
        conectar(nos, "financeiro", "encerrado",                 "5", "5 – Encerrar",                4);

        // aguarda_cpf
        conectar(nos, "aguarda_cpf_fatura",      "confirma_identidade_fatura",      "cpf_valido", "CPF/CNPJ válido", 0);
        conectar(nos, "aguarda_cpf_desbloqueio", "confirma_identidade_desbloqueio", "cpf_valido", "CPF/CNPJ válido", 0);

        // confirma_identidade
        conectar(nos, "confirma_identidade_fatura",      "resultado_fatura",      "1", "1 – Sim, sou o titular", 0);
        conectar(nos, "confirma_identidade_fatura",      "menu_inicial",          "2", "2 – Não sou o titular",  1);
        conectar(nos, "confirma_identidade_desbloqueio", "resultado_desbloqueio", "1", "1 – Sim, sou o titular", 0);
        conectar(nos, "confirma_identidade_desbloqueio", "menu_inicial",          "2", "2 – Não sou o titular",  1);

        // resultado_api → volta ao menu
        conectar(nos, "resultado_fatura",      "menu_inicial", "auto", "Retorno automático", 0);
        conectar(nos, "resultado_desbloqueio", "menu_inicial", "auto", "Retorno automático", 0);

        // comprovante → volta ao financeiro (estado não muda, bot aguarda próxima opção)
        conectar(nos, "comprovante", "financeiro", "auto", "Instrução enviada", 0);

        // transfers fora do horário → menu_inicial
        conectar(nos, "transfer_suporte",      "menu_inicial", "fora_horario", "Fora do horário", 0);
        conectar(nos, "transfer_duvidas",      "menu_inicial", "fora_horario", "Fora do horário", 0);
        conectar(nos, "transfer_cancelamento", "menu_inicial", "fora_horario", "Fora do horário", 0);
        conectar(nos, "transfer_financeiro",   "menu_inicial", "fora_horario", "Fora do horário", 0);

        log.info("Fluxo dinâmico inicializado com sucesso.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private FluxoNode criarNo(String nodeKey, String label, TipoNode tipo,
                               int posX, int posY,
                               String equipeTransferencia, String tipoHorario,
                               String actionKey) {
        if (nodeRepository.findByNodeKey(nodeKey).isPresent()) {
            return nodeRepository.findByNodeKey(nodeKey).get();
        }
        FluxoNode node = FluxoNode.builder()
                .nodeKey(nodeKey)
                .label(label)
                .tipo(tipo)
                .posX(posX)
                .posY(posY)
                .equipeTransferencia(equipeTransferencia)
                .tipoHorario(tipoHorario)
                .actionKey(actionKey)
                .build();
        return nodeRepository.save(node);
    }

    private void adicionarMensagem(FluxoNode node, String templateChave, int ordem) {
        boolean jaExiste = node.getMensagens().stream()
                .anyMatch(m -> m.getTemplateChave().equals(templateChave) && m.getOrdem() == ordem);
        if (jaExiste) return;

        FluxoMensagem mensagem = FluxoMensagem.builder()
                .node(node)
                .templateChave(templateChave)
                .ordem(ordem)
                .build();
        node.getMensagens().add(mensagem);
        nodeRepository.save(node);
    }

    private void conectar(Map<String, FluxoNode> nos, String deKey, String paraKey,
                           String condicao, String label, int ordem) {
        FluxoNode deNode   = nos.get(deKey);
        FluxoNode paraNode = nos.get(paraKey);

        boolean jaExiste = deNode.getConexoesSaida().stream()
                .anyMatch(c -> c.getCondicao().equals(condicao)
                        && c.getParaNode().getNodeKey().equals(paraKey));
        if (jaExiste) return;

        FluxoConexao conexao = FluxoConexao.builder()
                .deNode(deNode)
                .paraNode(paraNode)
                .condicao(condicao)
                .label(label)
                .ordem(ordem)
                .build();
        deNode.getConexoesSaida().add(conexao);
        nodeRepository.save(deNode);
    }
}
