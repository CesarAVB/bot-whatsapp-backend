package br.com.sistema.bot.engine.executor;

import br.com.sistema.bot.engine.FluxoEngine;
import br.com.sistema.bot.engine.FluxoExecucaoCtx;
import br.com.sistema.bot.engine.NodeExecutor;
import br.com.sistema.bot.entity.FluxoConexao;
import br.com.sistema.bot.entity.FluxoNode;
import br.com.sistema.bot.enums.TipoNode;
import org.springframework.stereotype.Component;

@Component
public class MensagemNodeExecutor implements NodeExecutor {

    @Override
    public TipoNode getTipo() {
        return TipoNode.MENSAGEM;
    }

    @Override
    public void executar(FluxoExecucaoCtx ctx, FluxoNode node, FluxoEngine engine) {
        engine.enviarMensagensDoNo(ctx, node);
        FluxoConexao conexao = engine.encontrarConexao(node, "auto");
        if (conexao != null) engine.transicionarPara(ctx, conexao.getParaNode());
    }
}
