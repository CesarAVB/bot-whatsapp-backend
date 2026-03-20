package br.com.sistema.bot.engine.executor;

import br.com.sistema.bot.dtos.response.HubsoftFaturaItem;
import br.com.sistema.bot.dtos.response.HubsoftFaturaResponse;
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

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResultadoFaturaNodeExecutor {

    private final WhatsAppService whatsAppService;
    private final BotTemplateService templateService;
    private final HubsoftService hubsoftService;

    public void executar(FluxoExecucaoCtx ctx, FluxoNode node, FluxoEngine engine) {
        String cpfCnpj = extrairCpf(ctx.contextData());

        HubsoftFaturaResponse faturas;
        try {
            faturas = hubsoftService.buscarFaturas(cpfCnpj);
        } catch (Exception e) {
            log.error("Erro ao buscar faturas. CPF/CNPJ: {}", cpfCnpj, e);
            whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("confirma.erro"));
            transitar(ctx, node, engine);
            return;
        }

        if (faturas.faturas() == null || faturas.faturas().isEmpty()) {
            whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("confirma.sem_fatura"));
            transitar(ctx, node, engine);
            return;
        }

        HubsoftFaturaItem fatura = faturas.faturas().get(0);

        if (fatura.link() != null && !fatura.link().isBlank()) {
            whatsAppService.enviarDocumento(ctx.phone(), fatura.link(),
                    "boleto-" + fatura.dataVencimento() + ".pdf",
                    "Vencimento: " + fatura.dataVencimento() + "\nLinha digitável: " + fatura.linhaDigitavel());
        } else {
            whatsAppService.enviarTexto(ctx.phone(),
                    templateService.buscarTexto("confirma.fatura",
                            Map.of("data", fatura.dataVencimento(), "linha", fatura.linhaDigitavel())));
        }

        transitar(ctx, node, engine);
    }

    private void transitar(FluxoExecucaoCtx ctx, FluxoNode node, FluxoEngine engine) {
        FluxoConexao conexao = engine.encontrarConexao(node, "auto");
        if (conexao != null) engine.transicionarPara(ctx, conexao.getParaNode());
    }

    private String extrairCpf(String contextData) {
        if (contextData == null) return "";
        int sep = contextData.indexOf('|');
        return sep > 0 ? contextData.substring(0, sep) : contextData;
    }
}
