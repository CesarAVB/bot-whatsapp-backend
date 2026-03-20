package br.com.sistema.bot.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtualizarTemplateRequest(

        @NotBlank(message = "O texto não pode estar em branco")
        String texto,

        @Size(max = 100, message = "O campo 'alteradoPor' deve ter no máximo 100 caracteres")
        String alteradoPor
) {}
