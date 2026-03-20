package br.com.sistema.bot.dtos.response;

import java.util.Map;

public record DashboardMetricasResponse(
        /** Contagem de conversas por nodeKey atual (ex: "menu_inicial" → 42). */
        Map<String, Long> totalPorEstado,
        long totalConversas
) {}
