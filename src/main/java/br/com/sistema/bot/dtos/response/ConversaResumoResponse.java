package br.com.sistema.bot.dtos.response;

import br.com.sistema.bot.entity.ConversationState;
import br.com.sistema.bot.enums.BotState;

import java.time.LocalDateTime;

public record ConversaResumoResponse(
        String whatsappPhone,
        BotState currentState,
        Long chatwootConversationId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ConversaResumoResponse from(ConversationState s) {
        return new ConversaResumoResponse(
                s.getWhatsappPhone(),
                s.getCurrentState(),
                s.getChatwootConversationId(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }
}
