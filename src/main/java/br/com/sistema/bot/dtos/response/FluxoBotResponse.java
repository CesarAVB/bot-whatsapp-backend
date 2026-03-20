package br.com.sistema.bot.dtos.response;

import java.util.List;

public record FluxoBotResponse(
        List<FluxoNodeResponse> nodes,
        List<FluxoArestaResponse> arestas
) {}
