package br.com.sistema.bot.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record AtualizarConexaoRequest(
        @NotBlank String condicao,
        String label,
        int ordem
) {}
