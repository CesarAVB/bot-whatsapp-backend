package br.com.sistema.bot.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class HubsoftProperties {

    @Value("${HUBSOFT_BASE_URL}")
    private String baseUrl;

    @Value("${HUBSOFT_CLIENT_ID}")
    private String clientId;

    @Value("${HUBSOFT_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${HUBSOFT_USERNAME}")
    private String username;

    @Value("${HUBSOFT_PASSWORD}")
    private String password;

    @Value("${HUBSOFT_GRANT_TYPE}")
    private String grantType;
}
