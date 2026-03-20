package br.com.sistema.bot.client;

import br.com.sistema.bot.config.WhatsAppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WhatsAppClient {

    private final WhatsAppProperties props;
    private final RestClient.Builder restClientBuilder;

    // ====================================================
    // enviarTexto - Envia mensagem de texto via WhatsApp Cloud API
    // ====================================================
    public void enviarTexto(String telefone, String mensagem) {
        log.debug("Enviando mensagem de texto para {}", telefone);

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", telefone,
                "type", "text",
                "text", Map.of("body", mensagem)
        );

        restClientBuilder.build()
                .post()
                .uri(props.getMensagensUrl())
                .header("Authorization", "Bearer " + props.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    // ====================================================
    // enviarDocumento - Envia documento (PDF de boleto) via WhatsApp Cloud API
    // ====================================================
    public void enviarDocumento(String telefone, String linkPdf, String nomeArquivo, String caption) {
        log.debug("Enviando documento para {}: {}", telefone, nomeArquivo);

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", telefone,
                "type", "document",
                "document", Map.of(
                        "link", linkPdf,
                        "filename", nomeArquivo,
                        "caption", caption
                )
        );

        restClientBuilder.build()
                .post()
                .uri(props.getMensagensUrl())
                .header("Authorization", "Bearer " + props.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
