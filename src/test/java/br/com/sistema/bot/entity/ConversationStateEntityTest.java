package br.com.sistema.bot.entity;

import br.com.sistema.bot.enums.BotState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConversationStateEntityTest {

    @Test
    @DisplayName("Deve preencher createdAt e updatedAt no prePersist")
    void devePreencherCreatedAtEUpdatedAtNoPrePersist() {
        ConversationState state = ConversationState.builder()
                .whatsappPhone("5511999999999")
                .currentState(BotState.MENU_INICIAL)
                .build();

        state.onCreate();

        assertNotNull(state.getCreatedAt());
        assertNotNull(state.getUpdatedAt());
    }

    @Test
    @DisplayName("Deve atualizar updatedAt no preUpdate")
    void deveAtualizarUpdatedAtNoPreUpdate() {
        ConversationState state = ConversationState.builder()
                .whatsappPhone("5511999999999")
                .currentState(BotState.MENU_INICIAL)
                .build();

        state.onUpdate();

        assertNotNull(state.getUpdatedAt());
    }
}
