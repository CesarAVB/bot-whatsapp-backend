package br.com.sistema.bot.service;

import br.com.sistema.bot.client.WhatsAppClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WhatsAppServiceTest {

    @Mock
    private WhatsAppClient whatsAppClient;

    @InjectMocks
    private WhatsAppService service;

    @Test
    @DisplayName("Deve delegar envio de texto para client")
    void deveDelegarEnvioDeTextoParaClient() {
        service.enviarTexto("5511999999999", "ola");

        verify(whatsAppClient).enviarTexto("5511999999999", "ola");
    }

    @Test
    @DisplayName("Deve nao propagar excecao ao enviar texto")
    void deveNaoPropagarExcecaoAoEnviarTexto() {
        doThrow(new RuntimeException("erro")).when(whatsAppClient).enviarTexto("5511999999999", "ola");

        service.enviarTexto("5511999999999", "ola");

        verify(whatsAppClient).enviarTexto("5511999999999", "ola");
    }

    @Test
    @DisplayName("Deve delegar envio de documento para client")
    void deveDelegarEnvioDeDocumentoParaClient() {
        service.enviarDocumento("5511999999999", "https://x", "boleto.pdf", "caption");

        verify(whatsAppClient).enviarDocumento("5511999999999", "https://x", "boleto.pdf", "caption");
    }
}
