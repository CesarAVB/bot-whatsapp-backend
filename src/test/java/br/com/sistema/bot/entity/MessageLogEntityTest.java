package br.com.sistema.bot.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MessageLogEntityTest {

    @Test
    @DisplayName("Deve preencher createdAt no prePersist")
    void devePreencherCreatedAtNoPrePersist() {
        MessageLog entity = MessageLog.builder()
                .messageId("wamid.1")
                .senderPhone("5511999999999")
                .build();

        entity.onCreate();

        assertNotNull(entity.getCreatedAt());
    }

    @Test
    @DisplayName("Deve atualizar updatedAt no preUpdate")
    void deveAtualizarUpdatedAtNoPreUpdate() {
        MessageLog entity = MessageLog.builder()
                .messageId("wamid.2")
                .senderPhone("5511888888888")
                .build();

        entity.onUpdate();

        assertNotNull(entity.getUpdatedAt());
    }
}
