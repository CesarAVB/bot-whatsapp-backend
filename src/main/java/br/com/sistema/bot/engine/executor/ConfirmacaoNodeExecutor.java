package br.com.sistema.bot.engine.executor;

import br.com.sistema.bot.engine.FluxoEngine;
import br.com.sistema.bot.engine.FluxoExecucaoCtx;
import br.com.sistema.bot.engine.NodeExecutor;
import br.com.sistema.bot.entity.FluxoConexao;
import br.com.sistema.bot.entity.FluxoNode;
import br.com.sistema.bot.enums.TipoNode;
import br.com.sistema.bot.service.BotTemplateService;
import br.com.sistema.bot.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConfirmacaoNodeExecutor implements NodeExecutor {

    private final WhatsAppService whatsAppService;
    private final BotTemplateService templateService;

    @Override
    public TipoNode getTipo() {
        return TipoNode.CONFIRMACAO;
    }

    @Override
    public void executar(FluxoExecucaoCtx ctx, FluxoNode node, FluxoEngine engine) {
        String input = ctx.input() != null ? ctx.input().trim() : "";

        FluxoConexao conexao = engine.encontrarConexao(node, input);

        if (conexao == null) {
            whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("confirma.opcao_invalida"));
            return;
        }

        engine.transicionarPara(ctx, conexao.getParaNode());
    }
}
