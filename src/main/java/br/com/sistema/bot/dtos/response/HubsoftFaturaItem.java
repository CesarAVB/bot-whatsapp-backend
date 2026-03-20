package br.com.sistema.bot.dtos.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HubsoftFaturaItem(
        @JsonProperty("data_vencimento") String dataVencimento,
        @JsonProperty("linha_digitavel") String linhaDigitavel,
        String link,
        String detalhamento
) {}
