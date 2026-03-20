package br.com.sistema.bot.entity;

import br.com.sistema.bot.enums.BotState;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "conversation_state", indexes = {
        @Index(name = "idx_whatsapp_phone", columnList = "whatsapp_phone", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConversationState {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "whatsapp_phone", nullable = false, unique = true)
    private String whatsappPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_state", nullable = false)
    private BotState currentState;

    // Dados temporários do fluxo atual (ex.: CPF digitado pelo cliente)
    @Column(name = "context_data", columnDefinition = "TEXT")
    private String contextData;

    // Preenchido quando a conversa é transferida para um humano no Chatwoot
    @Column(name = "chatwoot_conversation_id")
    private Long chatwootConversationId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
