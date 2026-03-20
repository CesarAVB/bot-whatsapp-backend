package br.com.sistema.bot.controller;

import br.com.sistema.bot.config.WhatsAppProperties;
import br.com.sistema.bot.dtos.request.WhatsAppWebhookRequest;
import br.com.sistema.bot.service.WebhookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@Slf4j
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Tag(name = "Webhook", description = "Recebimento de eventos da WhatsApp Cloud API (Meta)")
public class WebhookController {

    private final WebhookService webhookService;
    private final WhatsAppProperties whatsAppProperties;
    private final ObjectMapper objectMapper;

    // ====================================================
    // verificarWebhook - GET exigido pela Meta para registrar o webhook
    // Responde com hub.challenge se o verify_token bater
    // ====================================================
    @Operation(summary = "Verificação do webhook pela Meta",
            description = "Endpoint GET chamado pela Meta no momento do registro do webhook. Deve retornar hub.challenge.")
    @GetMapping("/whatsapp")
    public ResponseEntity<String> verificarWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String verifyToken,
            @RequestParam("hub.challenge") String challenge) {

        if ("subscribe".equals(mode) && whatsAppProperties.getVerifyToken().equals(verifyToken)) {
            log.info("Webhook WhatsApp verificado com sucesso");
            return ResponseEntity.ok(challenge);
        }

        log.warn("Falha na verificação do webhook. mode={}, token recebido={}", mode, verifyToken);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    // ====================================================
    // receberWebhook - POST com eventos de mensagem da WhatsApp Cloud API
    // Valida assinatura HMAC-SHA256 antes de processar
    // ====================================================
    @Operation(summary = "Receber evento da WhatsApp Cloud API",
            description = "Recebe mensagens enviadas por clientes via WhatsApp. Valida assinatura X-Hub-Signature-256 com o App Secret.")
    @PostMapping("/whatsapp")
    public ResponseEntity<Void> receberWebhook(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature) {

        if (!verificarAssinatura(rawBody, signature)) {
            log.warn("Assinatura HMAC inválida ou ausente. signature={}", signature);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            WhatsAppWebhookRequest payload = objectMapper.readValue(rawBody, WhatsAppWebhookRequest.class);
            webhookService.processar(payload);
        } catch (Exception e) {
            log.error("Erro ao deserializar payload do webhook WhatsApp", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Meta exige 200 OK imediato — processamento é assíncrono do ponto de vista da Meta
        return ResponseEntity.ok().build();
    }

    // ====================================================
    // verificarAssinatura - Valida X-Hub-Signature-256 com HMAC-SHA256 + App Secret
    // ====================================================
    private boolean verificarAssinatura(String rawBody, String signature) {
        if (signature == null || signature.isBlank()) return false;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    whatsAppProperties.getAppSecret().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            ));
            byte[] hash = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            String esperada = "sha256=" + HexFormat.of().formatHex(hash);
            return esperada.equals(signature);
        } catch (Exception e) {
            log.error("Erro ao verificar assinatura HMAC", e);
            return false;
        }
    }
}
