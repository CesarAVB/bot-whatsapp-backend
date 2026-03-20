package br.com.sistema.bot.handler;

import br.com.sistema.bot.dtos.response.HubsoftClienteItem;
import br.com.sistema.bot.dtos.response.HubsoftClienteResponse;
import br.com.sistema.bot.dtos.response.HubsoftDesbloqueioResponse;
import br.com.sistema.bot.dtos.response.HubsoftFaturaItem;
import br.com.sistema.bot.dtos.response.HubsoftFaturaResponse;
import br.com.sistema.bot.dtos.response.HubsoftServicoItem;
import br.com.sistema.bot.enums.BotState;
import br.com.sistema.bot.model.ConversationContext;
import br.com.sistema.bot.service.ConversationStateService;
import br.com.sistema.bot.service.HubsoftService;
import br.com.sistema.bot.service.WhatsAppService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmaCpfHandlerTest {

    @Mock
    private WhatsAppService whatsAppService;
    @Mock
    private ConversationStateService conversationStateService;
    @Mock
    private HubsoftService hubsoftService;

    @InjectMocks
    private ConfirmaCpfHandler handler;

    @Test
    @DisplayName("Deve voltar ao menu quando cliente negar identidade")
    void deveVoltarAoMenuQuandoClienteNegarIdentidade() {
        handler.handle(ctx(BotState.CONFIRMA_IDENTIDADE_FATURA, "2", "12345678901"));

        verify(conversationStateService).setState("5511999999999", BotState.MENU_INICIAL);
        verify(whatsAppService).enviarTexto(org.mockito.ArgumentMatchers.eq("5511999999999"), org.mockito.ArgumentMatchers.contains("Entendido"));
    }

    @Test
    @DisplayName("Deve pedir resposta valida quando opcao for invalida")
    void devePedirRespostaValidaQuandoOpcaoForInvalida() {
        handler.handle(ctx(BotState.CONFIRMA_IDENTIDADE_FATURA, "9", "12345678901"));

        verify(whatsAppService).enviarTexto(org.mockito.ArgumentMatchers.eq("5511999999999"), org.mockito.ArgumentMatchers.contains("responda"));
    }

    @Test
    @DisplayName("Deve resetar fluxo quando contextData estiver ausente")
    void deveResetarFluxoQuandoContextDataEstiverAusente() {
        handler.handle(ctx(BotState.CONFIRMA_IDENTIDADE_FATURA, "1", null));

        verify(conversationStateService).setState("5511999999999", BotState.MENU_INICIAL);
        verify(whatsAppService).enviarTexto(org.mockito.ArgumentMatchers.eq("5511999999999"), org.mockito.ArgumentMatchers.contains("inicie o processo"));
    }

    @Test
    @DisplayName("Deve enviar documento quando houver link de fatura")
    void deveEnviarDocumentoQuandoHouverLinkDeFatura() {
        HubsoftFaturaItem fatura = new HubsoftFaturaItem("2026-03-20", "12345", "https://boleto.pdf", "");
        when(hubsoftService.buscarFaturas("12345678901"))
                .thenReturn(new HubsoftFaturaResponse(List.of(fatura)));

        handler.handle(ctx(BotState.CONFIRMA_IDENTIDADE_FATURA, "1", "12345678901"));

        verify(conversationStateService).setState("5511999999999", BotState.MENU_INICIAL);
        verify(whatsAppService).enviarDocumento(org.mockito.ArgumentMatchers.eq("5511999999999"), org.mockito.ArgumentMatchers.eq("https://boleto.pdf"), org.mockito.ArgumentMatchers.contains("boleto"), org.mockito.ArgumentMatchers.contains("Linha digitável"));
    }

    @Test
    @DisplayName("Deve enviar sucesso quando desbloqueio retornar success")
    void deveEnviarSucessoQuandoDesbloqueioRetornarSuccess() {
        HubsoftServicoItem servico = new HubsoftServicoItem(99L, "Internet", "ativo", 1L);
        HubsoftClienteItem cliente = new HubsoftClienteItem("Cliente", "12345678901", List.of(servico));

        when(hubsoftService.buscarClientePorCpfCnpj("12345678901"))
                .thenReturn(new HubsoftClienteResponse(List.of(cliente)));
        when(hubsoftService.desbloquear(99L))
                .thenReturn(new HubsoftDesbloqueioResponse("success", "ok"));

        handler.handle(ctx(BotState.CONFIRMA_IDENTIDADE_DESBLOQUEIO, "1", "12345678901"));

        verify(conversationStateService).setState("5511999999999", BotState.MENU_INICIAL);
        verify(whatsAppService).enviarTexto(org.mockito.ArgumentMatchers.eq("5511999999999"), org.mockito.ArgumentMatchers.contains("Desbloqueio realizado com sucesso"));
    }

    private ConversationContext ctx(BotState state, String content, String contextData) {
        return new ConversationContext("5511999999999", "wamid.1", "Cliente Teste", content, state, contextData);
    }
}
