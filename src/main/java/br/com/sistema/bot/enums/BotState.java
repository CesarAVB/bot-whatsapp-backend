package br.com.sistema.bot.enums;

/**
 * Estados possíveis de uma conversa no fluxo do bot.
 * Armazenado em banco de dados (substitui as labels do Chatwoot).
 */
public enum BotState {

    MENU_INICIAL,
    SOU_CLIENTE,
    FINANCEIRO,

    // Fluxo de identificação
    AGUARDA_CPF_FATURA,
    AGUARDA_CPF_DESBLOQUEIO,
    CONFIRMA_IDENTIDADE_FATURA,
    CONFIRMA_IDENTIDADE_DESBLOQUEIO,

    // Estados terminais
    TRANSFERIDO,    // conversa passou para humano — bot ignora mensagens
    ENCERRADO       // conversa encerrada — próxima mensagem reinicia em MENU_INICIAL
}
