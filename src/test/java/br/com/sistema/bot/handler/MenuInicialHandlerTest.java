package br.com.sistema.bot.handler;

import br.com.sistema.bot.enums.BotState;
import br.com.sistema.bot.enums.TeamId;
import br.com.sistema.bot.model.ConversationContext;
import br.com.sistema.bot.service.ChatwootService;
import br.com.sistema.bot.service.ConversationStateService;
import br.com.sistema.bot.service.WhatsAppService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuInicialHandlerTest {

    @Mock
    private WhatsAppService whatsAppService;
    @Mock
    private ConversationStateService conversationStateService;
    @Mock
    private ChatwootService chatwootService;

    @InjectMocks
    private MenuInicialHandler handler;

    @Test
    @DisplayName("Deve aceitar apenas estado menu inicial")
    void deveAceitarApenasEstadoMenuInicial() {
        assertTrue(handler.canHandle(ctx(BotState.MENU_INICIAL, "1")));
        assertFalse(handler.canHandle(ctx(BotState.FINANCEIRO, "1")));
    }

    @Test
    @DisplayName("Deve enviar menu de boas vindas quando conteudo estiver vazio")
    void deveEnviarMenuDeBoasVindasQuandoConteudoEstiverVazio() {
        handler.handle(ctx(BotState.MENU_INICIAL, " "));

        verify(whatsAppService).enviarTexto(org.mockito.ArgumentMatchers.eq("5511999999999"), org.mockito.ArgumentMatchers.contains("Seja bem-vindo"));
    }

    @Test
    @DisplayName("Deve ir para sou cliente quando opcao um")
    void deveIrParaSouClienteQuandoOpcaoUm() {
        handler.handle(ctx(BotState.MENU_INICIAL, "1"));

        verify(conversationStateService).setState("5511999999999", BotState.SOU_CLIENTE);
        verify(whatsAppService).enviarTexto(org.mockito.ArgumentMatchers.eq("5511999999999"), org.mockito.ArgumentMatchers.contains("Assistência Técnica"));
    }

    @Test
    @DisplayName("Deve transferir para comercial quando opcao dois")
    void deveTransferirParaComercialQuandoOpcaoDois() {
        when(chatwootService.transferir(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq(TeamId.DUVIDAS_COMERCIAL.getId())))
                .thenReturn(10L);

        handler.handle(ctx(BotState.MENU_INICIAL, "2"));

        verify(conversationStateService).setState("5511999999999", BotState.TRANSFERIDO);
        verify(conversationStateService).setChatwootConversationId("5511999999999", 10L);
        verify(whatsAppService).enviarTexto(org.mockito.ArgumentMatchers.eq("5511999999999"), org.mockito.ArgumentMatchers.contains("equipe comercial"));
    }

    @Test
    @DisplayName("Deve reenviar menu quando opcao for invalida")
    void deveReenviarMenuQuandoOpcaoForInvalida() {
        handler.handle(ctx(BotState.MENU_INICIAL, "99"));

        verify(whatsAppService).enviarTexto(org.mockito.ArgumentMatchers.eq("5511999999999"), org.mockito.ArgumentMatchers.contains("Seja bem-vindo"));
        verify(conversationStateService, never()).setState("5511999999999", BotState.SOU_CLIENTE);
    }

    private ConversationContext ctx(BotState state, String content) {
        return new ConversationContext("5511999999999", "wamid.1", "Cliente Teste", content, state, null);
    }
}
