package br.com.sistema.bot.engine.executor;

import br.com.sistema.bot.dtos.response.HubsoftClienteItem;
import br.com.sistema.bot.dtos.response.HubsoftClienteResponse;
import br.com.sistema.bot.dtos.response.HubsoftDesbloqueioResponse;
import br.com.sistema.bot.engine.FluxoEngine;
import br.com.sistema.bot.engine.FluxoExecucaoCtx;
import br.com.sistema.bot.entity.FluxoConexao;
import br.com.sistema.bot.entity.FluxoNode;
import br.com.sistema.bot.service.BotTemplateService;
import br.com.sistema.bot.service.HubsoftService;
import br.com.sistema.bot.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Lógica de negócio para executar o desbloqueio de confiança.
 * Chamado pelo ResultadoApiNodeExecutor quando actionKey="desbloquear".
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResultadoDesbloqueioNodeExecutor {

    private final FluxoEngine engine;
    private final WhatsAppService whatsAppService;
    private final BotTemplateService templateService;
    private final HubsoftService hubsoftService;

    public void executar(FluxoExecucaoCtx ctx, FluxoNode node) {
        String cpfCnpj = extrairCpf(ctx.contextData());

        HubsoftClienteResponse clienteResponse;
        try {
            clienteResponse = hubsoftService.buscarClientePorCpfCnpj(cpfCnpj);
        } catch (Exception e) {
            log.error("Erro ao buscar cliente para desbloqueio. CPF/CNPJ: {}", cpfCnpj, e);
            whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("confirma.erro"));
            transitar(ctx, node);
            return;
        }

        if (clienteResponse.clientes() == null || clienteResponse.clientes().isEmpty()) {
            whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("confirma.sem_cadastro_desbloqueio"));
            transitar(ctx, node);
            return;
        }

        HubsoftClienteItem cliente = clienteResponse.clientes().get(0);

        if (cliente.servicos() == null || cliente.servicos().isEmpty()) {
            whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("confirma.sem_servico"));
            transitar(ctx, node);
            return;
        }

        long idServico = cliente.servicos().get(0).id();

        HubsoftDesbloqueioResponse resultado;
        try {
            resultado = hubsoftService.desbloquear(idServico);
        } catch (Exception e) {
            log.error("Erro ao desbloquear serviço {}", idServico, e);
            whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("confirma.erro"));
            transitar(ctx, node);
            return;
        }

        if ("success".equalsIgnoreCase(resultado.status())) {
            whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("confirma.desbloqueio_sucesso"));
        } else {
            whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("confirma.desbloqueio_falha"));
        }

        transitar(ctx, node);
    }

    private void transitar(FluxoExecucaoCtx ctx, FluxoNode node) {
        FluxoConexao conexao = engine.encontrarConexao(node, "auto");
        if (conexao != null) engine.transicionarPara(ctx, conexao.getParaNode());
    }

    private String extrairCpf(String contextData) {
        if (contextData == null) return "";
        int sep = contextData.indexOf('|');
        return sep > 0 ? contextData.substring(0, sep) : contextData;
    }
}
