package br.com.sistema.bot.enums;

/**
 * Identificadores de equipes no Chatwoot para transferência de atendimento.
 */
public enum TeamId {

    FINANCEIRO(1),
    SUPORTE(2),
    DUVIDAS_COMERCIAL(3),
    CANCELAMENTO(4);

    private final int id;

    TeamId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
