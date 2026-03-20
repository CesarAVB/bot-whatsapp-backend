package br.com.sistema.bot.model;

/**
 * Contexto de uma mensagem em processamento.
 * Construído pelo WebhookService a partir do payload do WhatsApp + estado do banco.
 */
public record ConversationContext(
        String phone,
        String messageId,
        String senderName,
        String content,
        String currentNodeKey,  // chave do nó atual no fluxo dinâmico
        String contextData      // dado temporário persistido (ex.: CPF digitado)
) {}
