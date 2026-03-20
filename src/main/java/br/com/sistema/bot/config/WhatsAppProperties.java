package br.com.sistema.bot.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class WhatsAppProperties {

    @Value("${WHATSAPP_PHONE_NUMBER_ID}")
    private String phoneNumberId;

    @Value("${WHATSAPP_ACCESS_TOKEN}")
    private String accessToken;

    // App Secret da aplicação Meta — usado para verificar assinatura HMAC dos webhooks
    @Value("${WHATSAPP_APP_SECRET}")
    private String appSecret;

    // Token de verificação configurado no painel Meta Developers
    @Value("${WHATSAPP_VERIFY_TOKEN}")
    private String verifyToken;

    private static final String BASE_URL = "https://graph.facebook.com/v18.0";

    public String getMensagensUrl() {
        return BASE_URL + "/" + phoneNumberId + "/messages";
    }
}
