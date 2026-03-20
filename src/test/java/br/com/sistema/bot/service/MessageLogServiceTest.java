package br.com.sistema.bot.service;

import br.com.sistema.bot.repository.MessageLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageLogServiceTest {

    @Mock
    private MessageLogRepository messageLogRepository;

    @InjectMocks
    private MessageLogService service;

    @Test
    @DisplayName("Deve retornar verdadeiro quando mensagem ja tiver sido processada")
    void deveRetornarVerdadeiroQuandoMensagemJaTiverSidoProcessada() {
        when(messageLogRepository.existsByMessageId("wamid.1")).thenReturn(true);

        boolean resultado = service.jaProcessado("wamid.1");

        assertTrue(resultado);
    }

    @Test
    @DisplayName("Deve registrar mensagem quando persistencia ocorrer com sucesso")
    void deveRegistrarMensagemQuandoPersistenciaOcorrerComSucesso() {
        service.registrar("wamid.2", "5511999999999");

        verify(messageLogRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deve ignorar excecao de persistencia quando ocorrer race condition")
    void deveIgnorarExcecaoDePersistenciaQuandoOcorrerRaceCondition() {
        doThrow(new RuntimeException("duplicado")).when(messageLogRepository).save(any());

        service.registrar("wamid.3", "5511888888888");

        verify(messageLogRepository, times(1)).save(any());
        assertFalse(service.jaProcessado("inexistente"));
    }
}
