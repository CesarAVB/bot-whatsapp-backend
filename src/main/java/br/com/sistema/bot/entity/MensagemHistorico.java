package br.com.sistema.bot.entity;

import br.com.sistema.bot.enums.Direcao;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mensagem_historico", indexes = {
        @Index(name = "idx_historico_phone", columnList = "whatsapp_phone"),
        @Index(name = "idx_historico_created_at", columnList = "created_at")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MensagemHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "whatsapp_phone", nullable = false, length = 20)
    private String whatsappPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "direcao", nullable = false, length = 10)
    private Direcao direcao;

    @Column(name = "conteudo", nullable = false, columnDefinition = "TEXT")
    private String conteudo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
