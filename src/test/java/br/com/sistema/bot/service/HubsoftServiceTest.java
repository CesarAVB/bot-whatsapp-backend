package br.com.sistema.bot.service;

import br.com.sistema.bot.client.HubsoftClient;
import br.com.sistema.bot.dtos.response.HubsoftClienteResponse;
import br.com.sistema.bot.dtos.response.HubsoftDesbloqueioResponse;
import br.com.sistema.bot.dtos.response.HubsoftFaturaResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HubsoftServiceTest {

    @Mock
    private HubsoftClient hubsoftClient;

    @InjectMocks
    private HubsoftService service;

    @Test
    @DisplayName("Deve delegar busca de cliente por cpf cnpj")
    void deveDelegarBuscaDeClientePorCpfCnpj() {
        HubsoftClienteResponse esperado = new HubsoftClienteResponse(List.of());
        when(hubsoftClient.buscarCliente("123")).thenReturn(esperado);

        HubsoftClienteResponse resultado = service.buscarClientePorCpfCnpj("123");

        assertSame(esperado, resultado);
        verify(hubsoftClient).buscarCliente("123");
    }

    @Test
    @DisplayName("Deve delegar busca de faturas")
    void deveDelegarBuscaDeFaturas() {
        HubsoftFaturaResponse esperado = new HubsoftFaturaResponse(List.of());
        when(hubsoftClient.buscarFaturas("123")).thenReturn(esperado);

        HubsoftFaturaResponse resultado = service.buscarFaturas("123");

        assertSame(esperado, resultado);
        verify(hubsoftClient).buscarFaturas("123");
    }

    @Test
    @DisplayName("Deve delegar desbloqueio")
    void deveDelegarDesbloqueio() {
        HubsoftDesbloqueioResponse esperado = new HubsoftDesbloqueioResponse("success", "ok");
        when(hubsoftClient.desbloquear(10L)).thenReturn(esperado);

        HubsoftDesbloqueioResponse resultado = service.desbloquear(10L);

        assertSame(esperado, resultado);
        verify(hubsoftClient).desbloquear(10L);
    }
}
