package br.com.sistema.bot.dtos.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatwootConversationResponse(
        Long id
) {}
