package br.com.sistema.bot.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record AdicionarMensagemRequest(
        @NotBlank String templateChave,
        int ordem
) {}
