package br.com.sistema.bot.engine;

/**
 * Contexto de execução passado para cada NodeExecutor.
 * Imutável — o executor lê daqui e usa o FluxoEngine para transitar.
 */
public record FluxoExecucaoCtx(
        String phone,
        String messageId,
        String senderName,
        /** Texto digitado pelo usuário (pode ser null em nós auto-execute). */
        String input,
        /** Dado temporário persistido entre nós (ex: CPF/CNPJ). */
        String contextData
) {}
