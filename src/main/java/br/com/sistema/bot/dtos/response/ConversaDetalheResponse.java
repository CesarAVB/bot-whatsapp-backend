package br.com.sistema.bot.dtos.response;

import br.com.sistema.bot.entity.ConversationState;

import java.time.LocalDateTime;
import java.util.List;

public record ConversaDetalheResponse(
        String whatsappPhone,
        String currentNodeKey,
        boolean transferidoParaHumano,
        Long chatwootConversationId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<MensagemHistoricoResponse> historico
) {
    public static ConversaDetalheResponse from(ConversationState s, List<MensagemHistoricoResponse> historico) {
        return new ConversaDetalheResponse(
                s.getWhatsappPhone(),
                s.getCurrentNodeKey(),
                s.isTransferidoParaHumano(),
                s.getChatwootConversationId(),
                s.getCreatedAt(),
                s.getUpdatedAt(),
                historico
        );
    }
}
