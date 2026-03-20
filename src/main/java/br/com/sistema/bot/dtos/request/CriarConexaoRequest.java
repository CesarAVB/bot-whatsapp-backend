package br.com.sistema.bot.dtos.request;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CriarConexaoRequest(
        @NotBlank UUID deNodeId,
        @NotBlank UUID paraNodeId,
        @NotBlank String condicao,
        String label,
        int ordem
) {}
