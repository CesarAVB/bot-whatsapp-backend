package br.com.sistema.bot.dtos.response;

import br.com.sistema.bot.entity.ConversationState;

import java.time.LocalDateTime;

public record ConversaResumoResponse(
        String whatsappPhone,
        String currentNodeKey,
        boolean transferidoParaHumano,
        Long chatwootConversationId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ConversaResumoResponse from(ConversationState s) {
        return new ConversaResumoResponse(
                s.getWhatsappPhone(),
                s.getCurrentNodeKey(),
                s.isTransferidoParaHumano(),
                s.getChatwootConversationId(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }
}
