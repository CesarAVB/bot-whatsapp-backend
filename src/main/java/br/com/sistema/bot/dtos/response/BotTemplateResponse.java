package br.com.sistema.bot.dtos.response;

import br.com.sistema.bot.entity.BotTemplate;

import java.time.LocalDateTime;

public record BotTemplateResponse(
        String chave,
        String texto,
        String descricao,
        boolean ativo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static BotTemplateResponse from(BotTemplate t) {
        return new BotTemplateResponse(
                t.getChave(),
                t.getTexto(),
                t.getDescricao(),
                t.isAtivo(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }
}
