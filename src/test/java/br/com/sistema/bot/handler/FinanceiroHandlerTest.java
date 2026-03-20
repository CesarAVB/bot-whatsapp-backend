package br.com.sistema.bot.handler;

import br.com.sistema.bot.enums.BotState;
import br.com.sistema.bot.model.ConversationContext;
import br.com.sistema.bot.service.ChatwootService;
import br.com.sistema.bot.service.ConversationStateService;
import br.com.sistema.bot.service.HorarioAtendimentoService;
import br.com.sistema.bot.service.WhatsAppService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinanceiroHandlerTest {

    @Mock
    private WhatsAppService whatsAppService;
    @Mock
    private ConversationStateService conversationStateService;
    @Mock
    private ChatwootService chatwootService;
    @Mock
    private HorarioAtendimentoService horarioAtendimentoService;
    @Mock
    private EncerrarHandler encerrarHandler;

    @InjectMocks
    private FinanceiroHandler handler;

    @Test
    @DisplayName("Deve solicitar cpf para segunda via")
    void deveSolicitarCpfParaSegundaVia() {
        handler.handle(ctx("1"));

        verify(conversationStateService).setState("5511999999999", BotState.AGUARDA_CPF_FATURA);
        verify(whatsAppService).enviarTexto(org.mockito.ArgumentMatchers.eq("5511999999999"), org.mockito.ArgumentMatchers.contains("CPF"));
    }

    @Test
    @DisplayName("Deve solicitar cpf para desbloqueio")
    void deveSolicitarCpfParaDesbloqueio() {
        handler.handle(ctx("2"));

        verify(conversationStateService).setState("5511999999999", BotState.AGUARDA_CPF_DESBLOQUEIO);
    }

    @Test
    @DisplayName("Deve transferir para financeiro quando horario disponivel")
    void deveTransferirParaFinanceiroQuandoHorarioDisponivel() {
        when(horarioAtendimentoService.isFinanceiroComercialDisponivel()).thenReturn(true);
        when(chatwootService.transferir(anyString(), anyString(), anyString(), anyInt())).thenReturn(7L);

        handler.handle(ctx("4"));

        verify(conversationStateService).setState("5511999999999", BotState.TRANSFERIDO);
        verify(conversationStateService).setChatwootConversationId("5511999999999", 7L);
        verify(whatsAppService).enviarTexto(org.mockito.ArgumentMatchers.eq("5511999999999"), org.mockito.ArgumentMatchers.contains("Financeira"));
    }

    @Test
    @DisplayName("Deve informar indisponibilidade quando fora do horario")
    void deveInformarIndisponibilidadeQuandoForaDoHorario() {
        when(horarioAtendimentoService.isFinanceiroComercialDisponivel()).thenReturn(false);

        handler.handle(ctx("4"));

        verify(whatsAppService).enviarTexto(org.mockito.ArgumentMatchers.eq("5511999999999"), org.mockito.ArgumentMatchers.contains("fora do horário"));
        verify(conversationStateService).setState("5511999999999", BotState.MENU_INICIAL);
    }

    @Test
    @DisplayName("Deve encerrar quando opcao cinco")
    void deveEncerrarQuandoOpcaoCinco() {
        ConversationContext ctx = ctx("5");

        handler.handle(ctx);

        verify(encerrarHandler).handle(ctx);
    }

    @Test
    @DisplayName("Deve responder opcao invalida")
    void deveResponderOpcaoInvalida() {
        handler.handle(ctx("9"));

        verify(whatsAppService).enviarTexto(org.mockito.ArgumentMatchers.eq("5511999999999"), org.mockito.ArgumentMatchers.contains("Opção inválida"));
    }

    private ConversationContext ctx(String content) {
        return new ConversationContext("5511999999999", "wamid.1", "Cliente Teste", content, BotState.FINANCEIRO, null);
    }
}
