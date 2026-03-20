package br.com.sistema.bot.service;

import br.com.sistema.bot.dtos.request.WhatsAppWebhookChange;
import br.com.sistema.bot.dtos.request.WhatsAppWebhookContact;
import br.com.sistema.bot.dtos.request.WhatsAppWebhookEntry;
import br.com.sistema.bot.dtos.request.WhatsAppWebhookMessage;
import br.com.sistema.bot.dtos.request.WhatsAppWebhookProfile;
import br.com.sistema.bot.dtos.request.WhatsAppWebhookRequest;
import br.com.sistema.bot.dtos.request.WhatsAppWebhookText;
import br.com.sistema.bot.dtos.request.WhatsAppWebhookValue;
import br.com.sistema.bot.entity.ConversationState;
import br.com.sistema.bot.enums.BotState;
import br.com.sistema.bot.handler.ConfirmaCpfHandler;
import br.com.sistema.bot.handler.EncerrarHandler;
import br.com.sistema.bot.handler.FinanceiroHandler;
import br.com.sistema.bot.handler.MenuInicialHandler;
import br.com.sistema.bot.handler.ProcessaCpfCnpjHandler;
import br.com.sistema.bot.handler.SouClienteHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    @Mock
    private MenuInicialHandler menuInicialHandler;
    @Mock
    private SouClienteHandler souClienteHandler;
    @Mock
    private FinanceiroHandler financeiroHandler;
    @Mock
    private ProcessaCpfCnpjHandler processaCpfCnpjHandler;
    @Mock
    private ConfirmaCpfHandler confirmaCpfHandler;
    @Mock
    private EncerrarHandler encerrarHandler;
    @Mock
    private ConversationStateService conversationStateService;
    @Mock
    private MessageLogService messageLogService;

    @InjectMocks
    private WebhookService service;

    @Test
    @DisplayName("Deve ignorar payload sem entry")
    void deveIgnorarPayloadSemEntry() {
        WhatsAppWebhookRequest payload = new WhatsAppWebhookRequest("whatsapp_business_account", null);

        service.processar(payload);

        verify(menuInicialHandler, never()).handle(any());
        verify(encerrarHandler, never()).handle(any());
    }

    @Test
    @DisplayName("Deve ignorar mensagem nao texto")
    void deveIgnorarMensagemNaoTexto() {
        WhatsAppWebhookRequest payload = criarPayload("wamid.1", "5511999999999", "image", null, BotState.MENU_INICIAL);

        service.processar(payload);

        verify(messageLogService, never()).registrar(any(), any());
        verify(menuInicialHandler, never()).handle(any());
    }

    @Test
    @DisplayName("Deve ignorar mensagem duplicada quando ja processada")
    void deveIgnorarMensagemDuplicadaQuandoJaProcessada() {
        when(messageLogService.jaProcessado("wamid.2")).thenReturn(true);
        WhatsAppWebhookRequest payload = criarPayload("wamid.2", "5511999999999", "text", "oi", BotState.MENU_INICIAL);

        service.processar(payload);

        verify(messageLogService, never()).registrar(any(), any());
        verify(menuInicialHandler, never()).handle(any());
    }

    @Test
    @DisplayName("Deve encerrar quando receber comando global sair")
    void deveEncerrarQuandoReceberComandoGlobalSair() {
        when(messageLogService.jaProcessado("wamid.3")).thenReturn(false);
        when(conversationStateService.buscarOuCriar("5511999999999")).thenReturn(criarEstado(BotState.FINANCEIRO));
        WhatsAppWebhookRequest payload = criarPayload("wamid.3", "5511999999999", "text", "sair", BotState.FINANCEIRO);

        service.processar(payload);

        verify(messageLogService).registrar("wamid.3", "5511999999999");
        verify(encerrarHandler).handle(any());
        verify(financeiroHandler, never()).handle(any());
    }

    @Test
    @DisplayName("Deve rotear para processaCpf quando estado aguarda cpf")
    void deveRotearParaProcessaCpfQuandoEstadoAguardaCpf() {
        when(messageLogService.jaProcessado("wamid.4")).thenReturn(false);
        when(conversationStateService.buscarOuCriar("5511888888888"))
                .thenReturn(criarEstado(BotState.AGUARDA_CPF_FATURA));
        WhatsAppWebhookRequest payload = criarPayload("wamid.4", "5511888888888", "text", "12345678901", BotState.AGUARDA_CPF_FATURA);

        service.processar(payload);

        verify(processaCpfCnpjHandler).handle(any());
        verify(menuInicialHandler, never()).handle(any());
    }

    @Test
    @DisplayName("Deve rotear para confirmaCpf quando estado confirmar identidade")
    void deveRotearParaConfirmaCpfQuandoEstadoConfirmarIdentidade() {
        when(messageLogService.jaProcessado("wamid.5")).thenReturn(false);
        when(conversationStateService.buscarOuCriar("5511777777777"))
                .thenReturn(criarEstado(BotState.CONFIRMA_IDENTIDADE_FATURA));
        WhatsAppWebhookRequest payload = criarPayload("wamid.5", "5511777777777", "text", "1", BotState.CONFIRMA_IDENTIDADE_FATURA);

        service.processar(payload);

        verify(confirmaCpfHandler).handle(any());
        verify(menuInicialHandler, never()).handle(any());
    }

    @Test
    @DisplayName("Deve voltar ao menu quando estado estiver encerrado")
    void deveVoltarAoMenuQuandoEstadoEstiverEncerrado() {
        when(messageLogService.jaProcessado("wamid.6")).thenReturn(false);
        when(conversationStateService.buscarOuCriar("5511666666666"))
                .thenReturn(criarEstado(BotState.ENCERRADO));
        WhatsAppWebhookRequest payload = criarPayload("wamid.6", "5511666666666", "text", "oi", BotState.ENCERRADO);

        service.processar(payload);

        verify(conversationStateService).setState("5511666666666", BotState.MENU_INICIAL);
        verify(menuInicialHandler).handle(any());
    }

    private ConversationState criarEstado(BotState estado) {
        return ConversationState.builder()
                .whatsappPhone("5511999999999")
                .currentState(estado)
                .build();
    }

    private WhatsAppWebhookRequest criarPayload(String messageId,
                                                String from,
                                                String type,
                                                String body,
                                                BotState ignored) {
        WhatsAppWebhookMessage message = new WhatsAppWebhookMessage(
                messageId,
                from,
                "1710000000",
                type,
                body == null ? null : new WhatsAppWebhookText(body)
        );

        WhatsAppWebhookValue value = new WhatsAppWebhookValue(
                "whatsapp",
                null,
                List.of(new WhatsAppWebhookContact(new WhatsAppWebhookProfile("Cliente Teste"), from)),
                List.of(message)
        );

        WhatsAppWebhookChange change = new WhatsAppWebhookChange(value, "messages");
        WhatsAppWebhookEntry entry = new WhatsAppWebhookEntry("entry-1", List.of(change));
        return new WhatsAppWebhookRequest("whatsapp_business_account", List.of(entry));
    }
}
