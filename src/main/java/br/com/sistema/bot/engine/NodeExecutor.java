package br.com.sistema.bot.engine;

import br.com.sistema.bot.entity.FluxoNode;
import br.com.sistema.bot.enums.TipoNode;

public interface NodeExecutor {

    TipoNode getTipo();

    /**
     * Executa a lógica do nó.
     * O FluxoEngine é passado como parâmetro para evitar dependência circular
     * (os executores precisam chamar engine.transicionarPara, mas o engine
     *  injeta a lista de executores).
     */
    void executar(FluxoExecucaoCtx ctx, FluxoNode node, FluxoEngine engine);
}
