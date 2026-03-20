package br.com.sistema.bot.dtos.response;

import br.com.sistema.bot.entity.ConversationState;
import br.com.sistema.bot.enums.BotState;

import java.time.LocalDateTime;
import java.util.List;

public record ConversaDetalheResponse(
        String whatsappPhone,
        BotState currentState,
        Long chatwootConversationId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<MensagemHistoricoResponse> historico
) {
    public static ConversaDetalheResponse from(ConversationState s, List<MensagemHistoricoResponse> historico) {
        return new ConversaDetalheResponse(
                s.getWhatsappPhone(),
                s.getCurrentState(),
                s.getChatwootConversationId(),
                s.getCreatedAt(),
                s.getUpdatedAt(),
                historico
        );
    }
}
