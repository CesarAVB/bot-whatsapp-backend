package br.com.sistema.bot.dtos.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WhatsAppWebhookRequest(
        String object,
        List<WhatsAppWebhookEntry> entry
) {}
