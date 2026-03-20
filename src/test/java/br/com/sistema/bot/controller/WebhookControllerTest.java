package br.com.sistema.bot.controller;

import br.com.sistema.bot.config.WhatsAppProperties;
import br.com.sistema.bot.service.WebhookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebhookControllerTest {

    @Mock
    private WebhookService webhookService;

    private WebhookController controller;

    @BeforeEach
    void setup() {
        WhatsAppProperties properties = new WhatsAppProperties();
        ReflectionTestUtils.setField(properties, "verifyToken", "token-teste");
        ReflectionTestUtils.setField(properties, "appSecret", "segredo-teste");

        controller = new WebhookController(webhookService, properties, new ObjectMapper());
    }

    @Test
    @DisplayName("Deve retornar challenge quando verificacao for valida")
    void deveRetornarChallengeQuandoVerificacaoForValida() {
        ResponseEntity<String> response = controller.verificarWebhook("subscribe", "token-teste", "abc123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("abc123", response.getBody());
    }

    @Test
    @DisplayName("Deve retornar forbidden quando token for invalido")
    void deveRetornarForbiddenQuandoTokenForInvalido() {
        ResponseEntity<String> response = controller.verificarWebhook("subscribe", "token-errado", "abc123");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve retornar unauthorized quando assinatura estiver ausente")
    void deveRetornarUnauthorizedQuandoAssinaturaEstiverAusente() {
        ResponseEntity<Void> response = controller.receberWebhook("{}", null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(webhookService, never()).processar(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Deve retornar bad request quando payload for invalido")
    void deveRetornarBadRequestQuandoPayloadForInvalido() {
        String rawBody = "{";
        String assinatura = assinar(rawBody, "segredo-teste");

        ResponseEntity<Void> response = controller.receberWebhook(rawBody, assinatura);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(webhookService, never()).processar(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Deve processar webhook quando assinatura e payload forem validos")
    void deveProcessarWebhookQuandoAssinaturaEPayloadForemValidos() {
        String rawBody = """
                {
                  "object": "whatsapp_business_account",
                  "entry": []
                }
                """;
        String assinatura = assinar(rawBody, "segredo-teste");

        ResponseEntity<Void> response = controller.receberWebhook(rawBody, assinatura);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(webhookService).processar(org.mockito.ArgumentMatchers.any());
    }

    private String assinar(String body, String segredo) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(segredo.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
            return "sha256=" + HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
