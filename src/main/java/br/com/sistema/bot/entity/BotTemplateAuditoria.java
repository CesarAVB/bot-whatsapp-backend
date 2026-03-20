package br.com.sistema.bot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bot_template_auditoria", indexes = {
        @Index(name = "idx_auditoria_chave", columnList = "template_chave")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BotTemplateAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "template_chave", nullable = false, length = 100)
    private String templateChave;

    @Column(name = "texto_anterior", columnDefinition = "TEXT")
    private String textoAnterior;

    @Column(name = "texto_novo", nullable = false, columnDefinition = "TEXT")
    private String textoNovo;

    @Column(name = "alterado_por", length = 100)
    private String alteradoPor;

    @Column(name = "alterado_em", nullable = false)
    private LocalDateTime alteradoEm;

    @PrePersist
    protected void onCreate() {
        this.alteradoEm = LocalDateTime.now();
    }
}
