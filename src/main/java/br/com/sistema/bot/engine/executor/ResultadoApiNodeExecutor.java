package br.com.sistema.bot.engine.executor;

import br.com.sistema.bot.engine.FluxoExecucaoCtx;
import br.com.sistema.bot.engine.NodeExecutor;
import br.com.sistema.bot.entity.FluxoNode;
import br.com.sistema.bot.enums.TipoNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handles RESULTADO_API nodes.
 * Delega para o executor correto com base no actionKey do nó:
 *   "buscar_fatura" → ResultadoFaturaNodeExecutor
 *   "desbloquear"   → ResultadoDesbloqueioNodeExecutor
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResultadoApiNodeExecutor implements NodeExecutor {

    private final ResultadoFaturaNodeExecutor faturaExecutor;
    private final ResultadoDesbloqueioNodeExecutor desbloqueioExecutor;

    @Override
    public TipoNode getTipo() {
        return TipoNode.RESULTADO_API;
    }

    @Override
    public void executar(FluxoExecucaoCtx ctx, FluxoNode node) {
        String actionKey = node.getActionKey();
        if ("buscar_fatura".equals(actionKey)) {
            faturaExecutor.executar(ctx, node);
        } else if ("desbloquear".equals(actionKey)) {
            desbloqueioExecutor.executar(ctx, node);
        } else {
            log.warn("actionKey desconhecido no nó '{}': {}", node.getNodeKey(), actionKey);
        }
    }
}
