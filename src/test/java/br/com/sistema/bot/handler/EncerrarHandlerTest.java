package br.com.sistema.bot.handler;

import br.com.sistema.bot.enums.BotState;
import br.com.sistema.bot.model.ConversationContext;
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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EncerrarHandlerTest {

    @Mock
    private WhatsAppService whatsAppService;

    @Mock
    private ConversationStateService conversationStateService;

    @InjectMocks
    private EncerrarHandler handler;

    @Test
    @DisplayName("Deve aceitar comando sair quando texto estiver em maiusculas e com espacos")
    void deveAceitarComandoSairQuandoTextoEstiverEmMaiusculasEComEspacos() {
        ConversationContext ctx = new ConversationContext("5511999999999", "wamid.1", "Cliente", "  SAIR  ", BotState.MENU_INICIAL, null);

        assertTrue(handler.canHandle(ctx));
    }

    @Test
    @DisplayName("Deve rejeitar comando diferente de sair ou cancelar")
    void deveRejeitarComandoDiferenteDeSairOuCancelar() {
        ConversationContext ctx = new ConversationContext("5511999999999", "wamid.1", "Cliente", "oi", BotState.MENU_INICIAL, null);

        assertFalse(handler.canHandle(ctx));
    }

    @Test
    @DisplayName("Deve resetar estado e enviar mensagem de despedida ao encerrar")
    void deveResetarEstadoEEnviarMensagemDeDespedidaAoEncerrar() {
        ConversationContext ctx = new ConversationContext("5511888888888", "wamid.2", "Cliente", "sair", BotState.FINANCEIRO, null);

        handler.handle(ctx);

        verify(conversationStateService).resetar("5511888888888");
        verify(whatsAppService).enviarTexto(org.mockito.ArgumentMatchers.eq("5511888888888"), org.mockito.ArgumentMatchers.contains("atendimento foi encerrado"));
    }
}
