package br.com.sistema.bot.client;

import br.com.sistema.bot.config.HubsoftProperties;
import br.com.sistema.bot.dtos.response.HubsoftClienteResponse;
import br.com.sistema.bot.dtos.response.HubsoftDesbloqueioResponse;
import br.com.sistema.bot.dtos.response.HubsoftFaturaResponse;
import br.com.sistema.bot.service.HubsoftAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubsoftClient {

    private final HubsoftProperties props;
    private final HubsoftAuthService authService;
    private final RestClient.Builder restClientBuilder;

    // ====================================================
    // buscarCliente - Busca cliente por CPF ou CNPJ
    // ====================================================
    public HubsoftClienteResponse buscarCliente(String cpfCnpj) {
        log.debug("Buscando cliente no Hubsoft. CPF/CNPJ: {}", cpfCnpj);

        return restClientBuilder.build()
                .get()
                .uri(props.getBaseUrl() + "/api/v1/integracao/cliente"
                        + "?busca=cpf_cnpj&termo_busca=" + cpfCnpj)
                .header("Authorization", "Bearer " + authService.getToken())
                .retrieve()
                .body(HubsoftClienteResponse.class);
    }

    // ====================================================
    // buscarFaturas - Busca faturas em aberto até o último dia do mês atual
    // ====================================================
    public HubsoftFaturaResponse buscarFaturas(String cpfCnpj) {
        log.debug("Buscando faturas no Hubsoft. CPF/CNPJ: {}", cpfCnpj);

        String ultimoDiaMes = YearMonth.now()
                .atEndOfMonth()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        return restClientBuilder.build()
                .get()
                .uri(props.getBaseUrl() + "/api/v1/integracao/cliente/financeiro"
                        + "?busca=cpf_cnpj"
                        + "&termo_busca=" + cpfCnpj
                        + "&data_fim=" + ultimoDiaMes
                        + "&tipo_data=data_vencimento")
                .header("Authorization", "Bearer " + authService.getToken())
                .retrieve()
                .body(HubsoftFaturaResponse.class);
    }

    // ====================================================
    // desbloquear - Realiza desbloqueio de confiança por 3 dias
    // ====================================================
    public HubsoftDesbloqueioResponse desbloquear(long idClienteServico) {
        log.info("Solicitando desbloqueio de confiança para serviço {}", idClienteServico);

        Map<String, String> body = Map.of(
                "id_cliente_servico", String.valueOf(idClienteServico),
                "dias_desbloqueio", "3"
        );

        return restClientBuilder.build()
                .post()
                .uri(props.getBaseUrl() + "/api/v1/integracao/cliente/desbloqueio_confianca")
                .header("Authorization", "Bearer " + authService.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(HubsoftDesbloqueioResponse.class);
    }
}
