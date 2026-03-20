package br.com.sistema.bot.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class ChatwootProperties {

    @Value("${CHATWOOT_BASE_URL}")
    private String baseUrl;

    @Value("${CHATWOOT_API_KEY}")
    private String apiKey;

    // ID do inbox WhatsApp no Chatwoot — necessário para criar conversas na transferência
    @Value("${CHATWOOT_INBOX_ID}")
    private Integer inboxId;
}
