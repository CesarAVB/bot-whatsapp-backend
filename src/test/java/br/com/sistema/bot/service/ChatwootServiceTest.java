package br.com.sistema.bot.service;

import br.com.sistema.bot.client.ChatwootClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatwootServiceTest {

    @Mock
    private ChatwootClient chatwootClient;

    @InjectMocks
    private ChatwootService service;

    @Test
    @DisplayName("Deve retornar menos um quando nao criar contato")
    void deveRetornarMenosUmQuandoNaoCriarContato() {
        when(chatwootClient.criarContato(anyString(), anyString())).thenReturn(null);

        long resultado = service.transferir("5511999999999", "Cliente", "nota", 1);

        assertEquals(-1L, resultado);
    }

    @Test
    @DisplayName("Deve retornar menos um quando nao criar conversa")
    void deveRetornarMenosUmQuandoNaoCriarConversa() {
        when(chatwootClient.criarContato(anyString(), anyString())).thenReturn(1L);
        when(chatwootClient.criarConversa(1L)).thenReturn(null);

        long resultado = service.transferir("5511999999999", "Cliente", "nota", 1);

        assertEquals(-1L, resultado);
    }

    @Test
    @DisplayName("Deve transferir com sucesso e adicionar nota quando houver resumo")
    void deveTransferirComSucessoEAdicionarNotaQuandoHouverResumo() {
        when(chatwootClient.criarContato(anyString(), anyString())).thenReturn(1L);
        when(chatwootClient.criarConversa(1L)).thenReturn(2L);

        long resultado = service.transferir("5511999999999", "Cliente", "nota", 4);

        assertEquals(2L, resultado);
        verify(chatwootClient).adicionarNota(2L, "nota");
        verify(chatwootClient).transferirParaEquipe(2L, 4);
    }

    @Test
    @DisplayName("Deve nao adicionar nota quando resumo estiver vazio")
    void deveNaoAdicionarNotaQuandoResumoEstiverVazio() {
        when(chatwootClient.criarContato(anyString(), anyString())).thenReturn(1L);
        when(chatwootClient.criarConversa(1L)).thenReturn(2L);

        long resultado = service.transferir("5511999999999", "Cliente", " ", 4);

        assertEquals(2L, resultado);
        verify(chatwootClient, never()).adicionarNota(anyLong(), anyString());
        verify(chatwootClient).transferirParaEquipe(2L, 4);
    }
}
