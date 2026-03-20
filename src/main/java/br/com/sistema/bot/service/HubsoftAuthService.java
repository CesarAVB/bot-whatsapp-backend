package br.com.sistema.bot.service;

import br.com.sistema.bot.config.HubsoftProperties;
import br.com.sistema.bot.dtos.response.HubsoftTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Serviço responsável pelo gerenciamento do token OAuth2 do Hubsoft.
 * Separado em bean próprio para garantir que @Cacheable funcione via proxy Spring
 * (evita o problema de self-invocation).
 * TTL do cache: 50 minutos (configurado em CacheConfig).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HubsoftAuthService {

    private final HubsoftProperties props;
    private final RestClient.Builder restClientBuilder;

    // ====================================================
    // getToken - Obtém ou retorna do cache o token de acesso do Hubsoft
    // Cache "hubsoft_token" com TTL de 50 minutos (expires_in = 3600s)
    // ====================================================
    @Cacheable(value = "hubsoft_token")
    public String getToken() {
        log.info("Obtendo novo token de autenticação do Hubsoft");

        Map<String, String> body = Map.of(
                "client_id", props.getClientId(),
                "client_secret", props.getClientSecret(),
                "username", props.getUsername(),
                "password", props.getPassword(),
                "grant_type", props.getGrantType()
        );

        HubsoftTokenResponse response = restClientBuilder.build()
                .post()
                .uri(props.getBaseUrl() + "/oauth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(HubsoftTokenResponse.class);

        if (response == null || response.accessToken() == null) {
            throw new IllegalStateException("Token Hubsoft não retornado pela API");
        }

        log.info("Token Hubsoft obtido com sucesso. Expira em {}s", response.expiresIn());
        return response.accessToken();
    }
}
