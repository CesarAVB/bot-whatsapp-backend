package br.com.sistema.bot.service;

import br.com.sistema.bot.entity.ConversationState;
import br.com.sistema.bot.enums.BotState;
import br.com.sistema.bot.repository.ConversationStateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationStateServiceTest {

    @Mock
    private ConversationStateRepository repository;

    @InjectMocks
    private ConversationStateService service;

    @Test
    @DisplayName("Deve criar estado inicial quando telefone não existir")
    void deveCriarEstadoInicialQuandoTelefoneNaoExistir() {
        String phone = "5511999999999";
        when(repository.findByWhatsappPhone(phone)).thenReturn(Optional.empty());
        when(repository.save(any(ConversationState.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConversationState resultado = service.buscarOuCriar(phone);

        assertEquals(phone, resultado.getWhatsappPhone());
        assertEquals(BotState.MENU_INICIAL, resultado.getCurrentState());
        verify(repository).save(any(ConversationState.class));
    }

    @Test
    @DisplayName("Deve atualizar estado e limpar contexto quando definir novo estado")
    void deveAtualizarEstadoELimparContextoQuandoDefinirNovoEstado() {
        String phone = "5511888888888";
        ConversationState state = ConversationState.builder()
                .whatsappPhone(phone)
                .currentState(BotState.SOU_CLIENTE)
                .contextData("12345678901")
                .build();

        when(repository.findByWhatsappPhone(phone)).thenReturn(Optional.of(state));

        service.setState(phone, BotState.FINANCEIRO);

        assertEquals(BotState.FINANCEIRO, state.getCurrentState());
        assertNull(state.getContextData());
        verify(repository).save(state);
    }

    @Test
    @DisplayName("Deve atualizar estado com contexto quando contexto for informado")
    void deveAtualizarEstadoComContextoQuandoContextoForInformado() {
        String phone = "5511777777777";
        ConversationState state = ConversationState.builder()
                .whatsappPhone(phone)
                .currentState(BotState.MENU_INICIAL)
                .build();

        when(repository.findByWhatsappPhone(phone)).thenReturn(Optional.of(state));

        service.setStateComContexto(phone, BotState.AGUARDA_CPF_FATURA, "12345678901");

        assertEquals(BotState.AGUARDA_CPF_FATURA, state.getCurrentState());
        assertEquals("12345678901", state.getContextData());
        verify(repository).save(state);
    }

    @Test
    @DisplayName("Deve resetar conversa para estado inicial quando telefone existir")
    void deveResetarConversaParaEstadoInicialQuandoTelefoneExistir() {
        String phone = "5511666666666";
        ConversationState state = ConversationState.builder()
                .whatsappPhone(phone)
                .currentState(BotState.TRANSFERIDO)
                .contextData("ctx")
                .chatwootConversationId(42L)
                .build();

        when(repository.findByWhatsappPhone(phone)).thenReturn(Optional.of(state));

        service.resetar(phone);

        assertEquals(BotState.MENU_INICIAL, state.getCurrentState());
        assertNull(state.getContextData());
        assertNull(state.getChatwootConversationId());
        verify(repository).save(state);
    }

    @Test
    @DisplayName("Deve nao salvar chatwootConversationId quando telefone nao existir")
    void deveNaoSalvarChatwootConversationIdQuandoTelefoneNaoExistir() {
        String phone = "5511555555555";
        when(repository.findByWhatsappPhone(phone)).thenReturn(Optional.empty());

        service.setChatwootConversationId(phone, 99L);

        verify(repository, never()).save(any(ConversationState.class));
    }
}
