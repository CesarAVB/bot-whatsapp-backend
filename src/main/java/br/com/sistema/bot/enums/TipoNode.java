package br.com.sistema.bot.enums;

/**
 * Tipo visual/comportamental de cada nó no fluxo do bot.
 */
public enum TipoNode {

    /** Exibe um menu numerado e aguarda a opção digitada pelo usuário. */
    MENU(false),

    /** Aguarda digitação livre (ex: CPF/CNPJ), valida e consulta API. */
    AGUARDA_INPUT(false),

    /** Aguarda confirmação 1=Sim / 2=Não. */
    CONFIRMACAO(false),

    /** Transfere o atendimento para uma equipe humana no Chatwoot. */
    TRANSFERENCIA(true),

    /** Chama API externa (Hubsoft) e envia o resultado ao cliente. */
    RESULTADO_API(true),

    /** Envia uma mensagem informativa e segue automaticamente para o próximo nó. */
    MENSAGEM(true),

    /** Encerra o atendimento e retorna ao menu_inicial. */
    ENCERRAMENTO(true);

    /**
     * Se true, o nó executa imediatamente ao ser ativado (sem aguardar input do usuário).
     */
    private final boolean autoExecute;

    TipoNode(boolean autoExecute) {
        this.autoExecute = autoExecute;
    }

    public boolean isAutoExecute() {
        return autoExecute;
    }
}
