package br.com.sistema.bot.engine.executor;

import br.com.sistema.bot.dtos.response.HubsoftClienteResponse;
import br.com.sistema.bot.engine.FluxoEngine;
import br.com.sistema.bot.engine.FluxoExecucaoCtx;
import br.com.sistema.bot.engine.NodeExecutor;
import br.com.sistema.bot.entity.FluxoConexao;
import br.com.sistema.bot.entity.FluxoNode;
import br.com.sistema.bot.enums.TipoNode;
import br.com.sistema.bot.service.BotTemplateService;
import br.com.sistema.bot.service.HubsoftService;
import br.com.sistema.bot.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Handles AGUARDA_INPUT nodes para validação de CPF/CNPJ.
 * Valida o formato, consulta o Hubsoft e transita via conexão "cpf_valido".
 * O CPF e o nome do titular são armazenados no contextData (formato "cpf|nome").
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AguardaCpfNodeExecutor implements NodeExecutor {

    private final FluxoEngine engine;
    private final WhatsAppService whatsAppService;
    private final BotTemplateService templateService;
    private final HubsoftService hubsoftService;

    @Override
    public TipoNode getTipo() {
        return TipoNode.AGUARDA_INPUT;
    }

    @Override
    public void executar(FluxoExecucaoCtx ctx, FluxoNode node) {
        String cpfCnpj = ctx.input() != null ? ctx.input().replaceAll("[^0-9]", "") : "";

        if (!isCpfCnpjValido(cpfCnpj)) {
            whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("cpf.invalido"));
            return;
        }

        HubsoftClienteResponse resposta;
        try {
            resposta = hubsoftService.buscarClientePorCpfCnpj(cpfCnpj);
        } catch (Exception e) {
            log.error("Erro ao consultar Hubsoft. CPF/CNPJ: {}", cpfCnpj, e);
            whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("cpf.erro_consulta"));
            return;
        }

        if (resposta.clientes() == null || resposta.clientes().isEmpty()) {
            whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("cpf.nao_encontrado"));
            return;
        }

        String nome = resposta.clientes().get(0).nomeRazaosocial();

        // contextData: "cpf|nome" — CPF para usar no próximo nó, nome para substituir no template
        String contextData = cpfCnpj + "|" + nome;

        FluxoConexao conexao = engine.encontrarConexao(node, "cpf_valido");
        if (conexao == null) {
            log.warn("Nó '{}' não tem conexão 'cpf_valido'", node.getNodeKey());
            return;
        }

        engine.transicionarComContexto(ctx, conexao.getParaNode(), contextData,
                Map.of("nome", nome));
    }

    private boolean isCpfCnpjValido(String digits) {
        return digits.length() == 11 || digits.length() == 14;
    }
}
