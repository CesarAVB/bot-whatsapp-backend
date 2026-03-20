package br.com.sistema.bot.model;

import br.com.sistema.bot.enums.BotState;

/**
 * Contexto de uma mensagem em processamento.
 * Construído pelo WebhookService a partir do payload do WhatsApp + estado do banco.
 */
public record ConversationContext(
        String phone,
        String messageId,
        String senderName,
        String content,
        BotState currentState,
        String contextData   // dado temporário persistido (ex.: CPF digitado)
) {}
