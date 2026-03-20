package br.com.sistema.bot.dtos.response;

import br.com.sistema.bot.entity.MensagemHistorico;
import br.com.sistema.bot.enums.Direcao;

import java.time.LocalDateTime;

public record MensagemHistoricoResponse(
        Direcao direcao,
        String conteudo,
        LocalDateTime createdAt
) {
    public static MensagemHistoricoResponse from(MensagemHistorico m) {
        return new MensagemHistoricoResponse(
                m.getDirecao(),
                m.getConteudo(),
                m.getCreatedAt()
        );
    }
}
