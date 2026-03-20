package br.com.sistema.bot.engine.executor;

import br.com.sistema.bot.engine.FluxoEngine;
import br.com.sistema.bot.engine.FluxoExecucaoCtx;
import br.com.sistema.bot.engine.NodeExecutor;
import br.com.sistema.bot.entity.FluxoConexao;
import br.com.sistema.bot.entity.FluxoNode;
import br.com.sistema.bot.enums.TipoNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handles MENSAGEM nodes.
 * Envia as mensagens do nó e segue automaticamente a conexão "auto".
 * Usado para mensagens informativas que não aguardam resposta (ex: instrução de comprovante).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MensagemNodeExecutor implements NodeExecutor {

    private final FluxoEngine engine;

    @Override
    public TipoNode getTipo() {
        return TipoNode.MENSAGEM;
    }

    @Override
    public void executar(FluxoExecucaoCtx ctx, FluxoNode node) {
        engine.enviarMensagensDoNo(ctx, node);

        FluxoConexao conexao = engine.encontrarConexao(node, "auto");
        if (conexao != null) {
            engine.transicionarPara(ctx, conexao.getParaNode());
        }
    }
}
