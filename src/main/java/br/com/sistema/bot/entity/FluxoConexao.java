package br.com.sistema.bot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fluxo_conexao")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FluxoConexao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "de_node_id", nullable = false)
    private FluxoNode deNode;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "para_node_id", nullable = false)
    private FluxoNode paraNode;

    /**
     * Condição que dispara esta transição.
     * Exemplos: "1", "2", "cpf_valido", "cpf_invalido",
     *           "fora_horario", "auto", "default".
     */
    @Column(name = "condicao", nullable = false, length = 50)
    private String condicao;

    /** Rótulo legível exibido sobre a seta no editor visual. */
    @Column(name = "label", length = 150)
    private String label;

    /** Ordem de avaliação quando há múltiplas saídas do mesmo nó. */
    @Column(name = "ordem", nullable = false)
    @Builder.Default
    private int ordem = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
