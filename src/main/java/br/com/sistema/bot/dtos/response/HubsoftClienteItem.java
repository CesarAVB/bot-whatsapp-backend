package br.com.sistema.bot.dtos.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HubsoftClienteItem(
        @JsonProperty("nome_razaosocial") String nomeRazaosocial,
        @JsonProperty("cpf_cnpj") String cpfCnpj,
        List<HubsoftServicoItem> servicos
) {}
