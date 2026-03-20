package br.com.sistema.bot.dtos.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HubsoftServicoItem(
        Long id,
        String descricao,
        String status,
        @JsonProperty("id_cliente") Long idCliente
) {}
