package br.com.sistema.bot.dtos.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WhatsAppWebhookMessage(
        String id,
        String from,
        String timestamp,
        String type,
        WhatsAppWebhookText text
) {}
