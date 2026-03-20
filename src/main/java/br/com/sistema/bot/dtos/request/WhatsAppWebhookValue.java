package br.com.sistema.bot.dtos.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WhatsAppWebhookValue(
        @JsonProperty("messaging_product") String messagingProduct,
        WhatsAppWebhookMetadata metadata,
        List<WhatsAppWebhookContact> contacts,
        List<WhatsAppWebhookMessage> messages
) {}
