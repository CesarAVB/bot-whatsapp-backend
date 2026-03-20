package br.com.sistema.bot.dtos.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WhatsAppWebhookChange(
        WhatsAppWebhookValue value,
        String field
) {}
