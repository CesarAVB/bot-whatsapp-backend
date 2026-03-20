package br.com.sistema.bot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fluxo_mensagem")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FluxoMensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "node_id", nullable = false)
    private FluxoNode node;

    /** Referência à chave do BotTemplate que contém o texto desta mensagem. */
    @Column(name = "template_chave", nullable = false, length = 100)
    private String templateChave;

    /** Posição desta mensagem dentro do nó (0 = primeira). */
    @Column(name = "ordem", nullable = false)
    private int ordem;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
