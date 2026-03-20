package br.com.sistema.bot.dtos.request;

import jakarta.validation.constraints.NotNull;

public record AtualizarNodePosicaoRequest(
        @NotNull Integer posX,
        @NotNull Integer posY,
        String label
) {}
