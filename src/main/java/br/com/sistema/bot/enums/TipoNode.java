package br.com.sistema.bot.enums;

/**
 * Tipo visual de cada nó no fluxo do bot (usado na tela Typebot do painel).
 */
public enum TipoNode {
    /** Exibe um menu numerado e aguarda a opção digitada. */
    MENU,
    /** Aguarda digitação livre (ex: CPF/CNPJ). */
    AGUARDA_INPUT,
    /** Aguarda confirmação 1=Sim / 2=Não. */
    CONFIRMACAO,
    /** Transfere o atendimento para uma equipe humana no Chatwoot. */
    TRANSFERENCIA,
    /** Chama API externa (Hubsoft) e envia o resultado ao cliente. */
    RESULTADO_API,
    /** Envia uma mensagem informativa sem mudar o estado da conversa. */
    MENSAGEM,
    /** Encerra o atendimento e retorna ao MENU_INICIAL. */
    ENCERRAMENTO
}
