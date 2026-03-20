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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SouClienteHandlerTest {

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
    private SouClienteHandler handler;

    @Test
    @DisplayName("Deve ir para menu financeiro quando opcao dois")
    void deveIrParaMenuFinanceiroQuandoOpcaoDois() {
        handler.handle(ctx("2"));

        verify(conversationStateService).setState("5511999999999", BotState.FINANCEIRO);
        verify(whatsAppService).enviarTexto(org.mockito.ArgumentMatchers.eq("5511999999999"), org.mockito.ArgumentMatchers.contains("Menu Financeiro"));
    }

    @Test
    @DisplayName("Deve transferir para suporte quando dentro do horario")
    void deveTransferirParaSuporteQuandoDentroDoHorario() {
        when(horarioAtendimentoService.isSuporteTecnicoDisponivel()).thenReturn(true);
        when(chatwootService.transferir(anyString(), anyString(), anyString(), anyInt())).thenReturn(20L);

        handler.handle(ctx("1"));

        verify(conversationStateService).setState("5511999999999", BotState.TRANSFERIDO);
        verify(conversationStateService).setChatwootConversationId("5511999999999", 20L);
        verify(whatsAppService).enviarTexto(org.mockito.ArgumentMatchers.eq("5511999999999"), org.mockito.ArgumentMatchers.contains("Transferindo para a equipe"));
    }

    @Test
    @DisplayName("Deve informar horario e voltar ao menu quando equipe indisponivel")
    void deveInformarHorarioEVoltarAoMenuQuandoEquipeIndisponivel() {
        when(horarioAtendimentoService.isFinanceiroComercialDisponivel()).thenReturn(false);

        handler.handle(ctx("3"));

        verify(whatsAppService).enviarTexto(org.mockito.ArgumentMatchers.eq("5511999999999"), org.mockito.ArgumentMatchers.contains("fora do horário"));
        verify(conversationStateService).setState("5511999999999", BotState.MENU_INICIAL);
        verify(chatwootService, never()).transferir(any(), any(), any(), anyInt());
    }

    @Test
    @DisplayName("Deve encerrar atendimento quando opcao cinco")
    void deveEncerrarAtendimentoQuandoOpcaoCinco() {
        ConversationContext ctx = ctx("5");

        handler.handle(ctx);

        verify(encerrarHandler).handle(ctx);
    }

    private ConversationContext ctx(String content) {
        return new ConversationContext("5511999999999", "wamid.1", "Cliente Teste", content, BotState.SOU_CLIENTE, null);
    }
}
