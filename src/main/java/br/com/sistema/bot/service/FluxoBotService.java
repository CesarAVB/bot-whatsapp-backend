package br.com.sistema.bot.service;

import br.com.sistema.bot.dtos.response.FluxoArestaResponse;
import br.com.sistema.bot.dtos.response.FluxoBotResponse;
import br.com.sistema.bot.dtos.response.FluxoNodeResponse;
import br.com.sistema.bot.dtos.response.FluxoTemplateResponse;
import br.com.sistema.bot.entity.BotTemplate;
import br.com.sistema.bot.enums.TipoNode;
import br.com.sistema.bot.repository.BotTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FluxoBotService {

    private final BotTemplateRepository templateRepository;

    public FluxoBotResponse construirFluxo() {
        Map<String, BotTemplate> templateMap = templateRepository.findAll().stream()
                .collect(Collectors.toMap(BotTemplate::getChave, t -> t));

        return new FluxoBotResponse(buildNodes(templateMap), buildArestas());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Nodes
    // ─────────────────────────────────────────────────────────────────────────

    private List<FluxoNodeResponse> buildNodes(Map<String, BotTemplate> m) {
        return List.of(
                node("menu_inicial", "Menu Inicial", "MENU_INICIAL", TipoNode.MENU, 0, 400,
                        tpls(m, "menu.inicial.boas_vindas"), null, null),

                node("sou_cliente", "Sou Cliente", "SOU_CLIENTE", TipoNode.MENU, 300, 200,
                        tpls(m, "menu.inicial.opcoes_cliente"), null, null),

                node("financeiro", "Menu Financeiro", "FINANCEIRO", TipoNode.MENU, 600, 0,
                        tpls(m, "sou_cliente.menu_financeiro"), null, null),

                node("aguarda_cpf_fatura", "Aguarda CPF (Fatura)", "AGUARDA_CPF_FATURA",
                        TipoNode.AGUARDA_INPUT, 900, -200,
                        tpls(m, "financeiro.solicitar_cpf"), null, null),

                node("aguarda_cpf_desbloqueio", "Aguarda CPF (Desbloqueio)", "AGUARDA_CPF_DESBLOQUEIO",
                        TipoNode.AGUARDA_INPUT, 900, 50,
                        tpls(m, "financeiro.solicitar_cpf"), null, null),

                node("confirma_identidade_fatura", "Confirma Titular (Fatura)", "CONFIRMA_IDENTIDADE_FATURA",
                        TipoNode.CONFIRMACAO, 1200, -200,
                        tpls(m, "cpf.confirma_titular"), null, null),

                node("confirma_identidade_desbloqueio", "Confirma Titular (Desbloqueio)", "CONFIRMA_IDENTIDADE_DESBLOQUEIO",
                        TipoNode.CONFIRMACAO, 1200, 50,
                        tpls(m, "cpf.confirma_titular"), null, null),

                node("resultado_fatura", "Enviar Fatura", null, TipoNode.RESULTADO_API, 1500, -200,
                        tpls(m, "confirma.fatura", "confirma.sem_fatura"), null, null),

                node("resultado_desbloqueio", "Executar Desbloqueio", null, TipoNode.RESULTADO_API, 1500, 50,
                        tpls(m, "confirma.desbloqueio_sucesso", "confirma.desbloqueio_falha"), null, null),

                node("comprovante", "Instrução Comprovante", null, TipoNode.MENSAGEM, 900, 250,
                        tpls(m, "financeiro.comprovante"), null, null),

                node("transfer_comercial", "Transfer → Comercial", "TRANSFERIDO",
                        TipoNode.TRANSFERENCIA, 300, 650,
                        tpls(m, "menu.inicial.comercial_transfer"), "DUVIDAS_COMERCIAL", null),

                node("transfer_suporte", "Transfer → Suporte Técnico", "TRANSFERIDO",
                        TipoNode.TRANSFERENCIA, 600, 250,
                        tpls(m, "sou_cliente.transfer"), "SUPORTE", "Dom–Dom 09h–21h"),

                node("transfer_duvidas", "Transfer → Dúvidas/Sugestões", "TRANSFERIDO",
                        TipoNode.TRANSFERENCIA, 600, 450,
                        tpls(m, "sou_cliente.transfer"), "DUVIDAS_COMERCIAL", "Seg–Sáb 09h–18h"),

                node("transfer_cancelamento", "Transfer → Cancelamento", "TRANSFERIDO",
                        TipoNode.TRANSFERENCIA, 600, 650,
                        tpls(m, "sou_cliente.transfer"), "CANCELAMENTO", "Seg–Sáb 09h–18h"),

                node("transfer_financeiro", "Transfer → Financeiro", "TRANSFERIDO",
                        TipoNode.TRANSFERENCIA, 900, 450,
                        tpls(m, "financeiro.transfer"), "FINANCEIRO", "Seg–Sáb 09h–18h"),

                node("encerrado", "Encerrar Atendimento", "ENCERRADO",
                        TipoNode.ENCERRAMENTO, 600, 850,
                        tpls(m, "encerrar.despedida"), null, null)
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Edges
    // ─────────────────────────────────────────────────────────────────────────

    private List<FluxoArestaResponse> buildArestas() {
        List<FluxoArestaResponse> list = new ArrayList<>();

        // menu_inicial
        list.add(a("menu_inicial", "sou_cliente", "1", "1 – Sou cliente ASB"));
        list.add(a("menu_inicial", "transfer_comercial", "2", "2 – Quero me tornar cliente"));

        // sou_cliente
        list.add(a("sou_cliente", "transfer_suporte", "1", "1 – Assistência Técnica"));
        list.add(a("sou_cliente", "financeiro", "2", "2 – Financeiro"));
        list.add(a("sou_cliente", "transfer_duvidas", "3", "3 – Dúvidas/Sugestões"));
        list.add(a("sou_cliente", "transfer_cancelamento", "4", "4 – Cancelamento"));
        list.add(a("sou_cliente", "encerrado", "5", "5 – Encerrar"));

        // financeiro
        list.add(a("financeiro", "aguarda_cpf_fatura", "1", "1 – Segunda via de boleto"));
        list.add(a("financeiro", "aguarda_cpf_desbloqueio", "2", "2 – Desbloqueio de confiança"));
        list.add(a("financeiro", "comprovante", "3", "3 – Enviar comprovante"));
        list.add(a("financeiro", "transfer_financeiro", "4", "4 – Falar com atendente"));
        list.add(a("financeiro", "encerrado", "5", "5 – Encerrar"));

        // aguarda_cpf → confirma_identidade
        list.add(a("aguarda_cpf_fatura", "confirma_identidade_fatura", "cpf_valido", "CPF/CNPJ válido encontrado"));
        list.add(a("aguarda_cpf_desbloqueio", "confirma_identidade_desbloqueio", "cpf_valido", "CPF/CNPJ válido encontrado"));

        // confirma_identidade
        list.add(a("confirma_identidade_fatura", "resultado_fatura", "1", "1 – Sim, sou o titular"));
        list.add(a("confirma_identidade_fatura", "menu_inicial", "2", "2 – Não sou o titular"));
        list.add(a("confirma_identidade_desbloqueio", "resultado_desbloqueio", "1", "1 – Sim, sou o titular"));
        list.add(a("confirma_identidade_desbloqueio", "menu_inicial", "2", "2 – Não sou o titular"));

        // resultado_api → menu_inicial (retorno automático)
        list.add(a("resultado_fatura", "menu_inicial", "auto", "Retorno automático ao menu"));
        list.add(a("resultado_desbloqueio", "menu_inicial", "auto", "Retorno automático ao menu"));

        // comprovante → financeiro (estado não muda, cliente continua no menu financeiro)
        list.add(a("comprovante", "financeiro", "auto", "Instrução enviada – aguarda próximo input"));

        // transfers fora do horário → menu_inicial
        list.add(a("transfer_suporte", "menu_inicial", "fora_horario", "Fora do horário de atendimento"));
        list.add(a("transfer_duvidas", "menu_inicial", "fora_horario", "Fora do horário de atendimento"));
        list.add(a("transfer_cancelamento", "menu_inicial", "fora_horario", "Fora do horário de atendimento"));
        list.add(a("transfer_financeiro", "menu_inicial", "fora_horario", "Fora do horário de atendimento"));

        return List.copyOf(list);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private FluxoNodeResponse node(String id, String label, String estado, TipoNode tipo,
                                   int posX, int posY, List<FluxoTemplateResponse> mensagens,
                                   String equipe, String horario) {
        return new FluxoNodeResponse(id, label, estado, tipo, posX, posY, mensagens, equipe, horario);
    }

    private FluxoArestaResponse a(String de, String para, String condicao, String label) {
        return new FluxoArestaResponse(de + "_to_" + para + "_" + condicao, de, para, condicao, label);
    }

    private List<FluxoTemplateResponse> tpls(Map<String, BotTemplate> map, String... chaves) {
        List<FluxoTemplateResponse> result = new ArrayList<>();
        for (String chave : chaves) {
            BotTemplate t = map.get(chave);
            if (t != null) {
                result.add(new FluxoTemplateResponse(t.getChave(), t.getTexto(), t.getDescricao()));
            } else {
                result.add(new FluxoTemplateResponse(chave, "[" + chave + "]", null));
            }
        }
        return List.copyOf(result);
    }
}
