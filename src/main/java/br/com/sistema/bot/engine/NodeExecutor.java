package br.com.sistema.bot.engine;

import br.com.sistema.bot.entity.FluxoNode;
import br.com.sistema.bot.enums.TipoNode;

/**
 * Contrato para os executores de cada tipo de nó do fluxo dinâmico.
 * Cada implementação encapsula a lógica de negócio de um TipoNode específico.
 */
public interface NodeExecutor {

    TipoNode getTipo();

    /**
     * Executa a lógica do nó para o contexto atual da conversa.
     *
     * @param ctx  contexto de execução (telefone, input do usuário, contextData…)
     * @param node o nó sendo executado
     */
    void executar(FluxoExecucaoCtx ctx, FluxoNode node);
}
