package br.com.sistema.bot.dtos.response;

public record FluxoArestaResponse(
        /** Identificador único da aresta. */
        String id,
        /** ID do nó de origem. */
        String de,
        /** ID do nó de destino. */
        String para,
        /**
         * Condição que dispara esta transição.
         * Exemplos: "1", "2", "cpf_valido", "fora_horario", "auto".
         */
        String condicao,
        /** Rótulo legível exibido sobre a aresta no editor visual. */
        String label
) {}
