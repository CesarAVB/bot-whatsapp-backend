package br.com.sistema.bot.dtos.response;

import br.com.sistema.bot.entity.BotTemplateAuditoria;

import java.time.LocalDateTime;

public record BotTemplateAuditoriaResponse(
        String templateChave,
        String textoAnterior,
        String textoNovo,
        String alteradoPor,
        LocalDateTime alteradoEm
) {
    public static BotTemplateAuditoriaResponse from(BotTemplateAuditoria a) {
        return new BotTemplateAuditoriaResponse(
                a.getTemplateChave(),
                a.getTextoAnterior(),
                a.getTextoNovo(),
                a.getAlteradoPor(),
                a.getAlteradoEm()
        );
    }
}
