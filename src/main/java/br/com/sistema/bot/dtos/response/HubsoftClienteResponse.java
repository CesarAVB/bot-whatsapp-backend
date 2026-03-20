package br.com.sistema.bot.dtos.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HubsoftClienteResponse(
        List<HubsoftClienteItem> clientes
) {}
