package br.com.sistema.bot.dtos.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WhatsAppWebhookContact(
        WhatsAppWebhookProfile profile,
        @JsonProperty("wa_id") String waId
) {}
