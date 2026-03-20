package br.com.sistema.bot.entity;

import br.com.sistema.bot.enums.TipoNode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "fluxo_node", indexes = {
        @Index(name = "idx_fluxo_node_key", columnList = "node_key", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FluxoNode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Chave única usada como identificador de estado da conversa. Ex: "menu_inicial". */
    @Column(name = "node_key", nullable = false, unique = true, length = 100)
    private String nodeKey;

    @Column(name = "label", nullable = false, length = 150)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoNode tipo;

    @Column(name = "pos_x", nullable = false)
    @Builder.Default
    private int posX = 0;

    @Column(name = "pos_y", nullable = false)
    @Builder.Default
    private int posY = 0;

    /**
     * Nome da equipe Chatwoot de destino.
     * Usado por nós do tipo TRANSFERENCIA. Ex: "SUPORTE", "FINANCEIRO".
     */
    @Column(name = "equipe_transferencia", length = 50)
    private String equipeTransferencia;

    /**
     * Código da verificação de horário.
     * Valores: "SUPORTE" ou "FINANCEIRO_COMERCIAL". Null = sem restrição de horário.
     */
    @Column(name = "tipo_horario", length = 30)
    private String tipoHorario;

    /**
     * Chave da ação de negócio para nós RESULTADO_API.
     * Valores: "buscar_fatura" | "desbloquear".
     */
    @Column(name = "action_key", length = 50)
    private String actionKey;

    @Column(name = "ativo", nullable = false)
    @Builder.Default
    private boolean ativo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "node", cascade = CascadeType.ALL, orphanRemoval = true,
               fetch = FetchType.EAGER)
    @OrderBy("ordem ASC")
    @Builder.Default
    private List<FluxoMensagem> mensagens = new ArrayList<>();

    @OneToMany(mappedBy = "deNode", cascade = CascadeType.ALL, orphanRemoval = true,
               fetch = FetchType.EAGER)
    @OrderBy("ordem ASC")
    @Builder.Default
    private List<FluxoConexao> conexoesSaida = new ArrayList<>();

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
