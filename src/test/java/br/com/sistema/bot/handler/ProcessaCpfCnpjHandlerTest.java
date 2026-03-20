package br.com.sistema.bot.handler;

import br.com.sistema.bot.dtos.response.HubsoftClienteItem;
import br.com.sistema.bot.dtos.response.HubsoftClienteResponse;
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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessaCpfCnpjHandlerTest {

    @Mock
    private WhatsAppService whatsAppService;
    @Mock
    private ConversationStateService conversationStateService;
    @Mock
    private HubsoftService hubsoftService;

    @InjectMocks
    private ProcessaCpfCnpjHandler handler;

    @Test
    @DisplayName("Deve rejeitar cpf cnpj invalido")
    void deveRejeitarCpfCnpjInvalido() {
        handler.handle(ctx(BotState.AGUARDA_CPF_FATURA, "12"));

        verify(whatsAppService).enviarTexto(org.mockito.ArgumentMatchers.eq("5511999999999"), org.mockito.ArgumentMatchers.contains("inválido"));
        verify(hubsoftService, never()).buscarClientePorCpfCnpj(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("Deve avisar quando cadastro nao for encontrado")
    void deveAvisarQuandoCadastroNaoForEncontrado() {
        when(hubsoftService.buscarClientePorCpfCnpj("12345678901"))
                .thenReturn(new HubsoftClienteResponse(List.of()));

        handler.handle(ctx(BotState.AGUARDA_CPF_FATURA, "12345678901"));

        verify(whatsAppService).enviarTexto(org.mockito.ArgumentMatchers.eq("5511999999999"), org.mockito.ArgumentMatchers.contains("Não encontramos cadastro"));
    }

    @Test
    @DisplayName("Deve avancar para confirmacao de fatura quando cliente encontrado")
    void deveAvancarParaConfirmacaoDeFaturaQuandoClienteEncontrado() {
        HubsoftClienteItem cliente = new HubsoftClienteItem("Cliente XPTO", "12345678901", List.of());
        when(hubsoftService.buscarClientePorCpfCnpj("12345678901"))
                .thenReturn(new HubsoftClienteResponse(List.of(cliente)));

        handler.handle(ctx(BotState.AGUARDA_CPF_FATURA, "123.456.789-01"));

        verify(conversationStateService).setStateComContexto("5511999999999", BotState.CONFIRMA_IDENTIDADE_FATURA, "12345678901");
        verify(whatsAppService).enviarTexto(org.mockito.ArgumentMatchers.eq("5511999999999"), org.mockito.ArgumentMatchers.contains("Você é o titular"));
    }

    @Test
    @DisplayName("Deve avancar para confirmacao de desbloqueio quando fluxo for desbloqueio")
    void deveAvancarParaConfirmacaoDeDesbloqueioQuandoFluxoForDesbloqueio() {
        HubsoftClienteItem cliente = new HubsoftClienteItem("Cliente XPTO", "12345678901234", List.of());
        when(hubsoftService.buscarClientePorCpfCnpj("12345678901234"))
                .thenReturn(new HubsoftClienteResponse(List.of(cliente)));

        handler.handle(ctx(BotState.AGUARDA_CPF_DESBLOQUEIO, "12.345.678/9012-34"));

        verify(conversationStateService).setStateComContexto("5511999999999", BotState.CONFIRMA_IDENTIDADE_DESBLOQUEIO, "12345678901234");
    }

    private ConversationContext ctx(BotState state, String content) {
        return new ConversationContext("5511999999999", "wamid.1", "Cliente Teste", content, state, null);
    }
}
