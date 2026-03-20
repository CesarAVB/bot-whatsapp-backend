package br.com.sistema.bot.engine.executor;

import br.com.sistema.bot.engine.FluxoEngine;
import br.com.sistema.bot.engine.FluxoExecucaoCtx;
import br.com.sistema.bot.engine.NodeExecutor;
import br.com.sistema.bot.entity.FluxoConexao;
import br.com.sistema.bot.entity.FluxoNode;
import br.com.sistema.bot.enums.TipoNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MenuNodeExecutor implements NodeExecutor {

    @Override
    public TipoNode getTipo() {
        return TipoNode.MENU;
    }

    @Override
    public void executar(FluxoExecucaoCtx ctx, FluxoNode node, FluxoEngine engine) {
        String input = ctx.input() != null ? ctx.input().trim() : "";

        FluxoConexao conexao = engine.encontrarConexao(node, input);

        if (conexao == null) {
            log.debug("Nenhuma conexão para input='{}' no nó '{}'. Repetindo mensagens.", input, node.getNodeKey());
            engine.enviarMensagensDoNo(ctx, node);
            return;
        }

        engine.transicionarPara(ctx, conexao.getParaNode());
    }
}
