package br.com.sistema.bot.handler;

import br.com.sistema.bot.model.ConversationContext;

/**
 * Contrato para todos os handlers de mensagem do bot.
 * Cada handler é responsável por um estado específico da conversa
 * (representado por uma label no Chatwoot).
 */
public interface MessageHandler {

    /**
     * Verifica se este handler é capaz de processar a conversa no estado atual.
     */
    boolean canHandle(ConversationContext ctx);

    /**
     * Executa a lógica de atendimento para este estado.
     */
    void handle(ConversationContext ctx);
}
