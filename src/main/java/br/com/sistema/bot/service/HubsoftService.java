package br.com.sistema.bot.service;

import br.com.sistema.bot.client.HubsoftClient;
import br.com.sistema.bot.dtos.response.HubsoftClienteResponse;
import br.com.sistema.bot.dtos.response.HubsoftDesbloqueioResponse;
import br.com.sistema.bot.dtos.response.HubsoftFaturaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubsoftService {

    private final HubsoftClient hubsoftClient;

    // ====================================================
    // buscarClientePorCpfCnpj - Consulta dados do cliente no Hubsoft
    // ====================================================
    public HubsoftClienteResponse buscarClientePorCpfCnpj(String cpfCnpj) {
        log.debug("Consultando cliente no Hubsoft. CPF/CNPJ: {}", cpfCnpj);
        return hubsoftClient.buscarCliente(cpfCnpj);
    }

    // ====================================================
    // buscarFaturas - Consulta faturas em aberto do cliente
    // ====================================================
    public HubsoftFaturaResponse buscarFaturas(String cpfCnpj) {
        log.debug("Consultando faturas no Hubsoft. CPF/CNPJ: {}", cpfCnpj);
        return hubsoftClient.buscarFaturas(cpfCnpj);
    }

    // ====================================================
    // desbloquear - Solicita desbloqueio de confiança (limite: 1x a cada 25 dias)
    // ====================================================
    public HubsoftDesbloqueioResponse desbloquear(long idClienteServico) {
        log.info("Solicitando desbloqueio de confiança. Serviço: {}", idClienteServico);
        return hubsoftClient.desbloquear(idClienteServico);
    }
}
