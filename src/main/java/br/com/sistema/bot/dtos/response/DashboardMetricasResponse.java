package br.com.sistema.bot.dtos.response;

import br.com.sistema.bot.enums.BotState;

import java.util.Map;

public record DashboardMetricasResponse(
        Map<BotState, Long> totalPorEstado,
        long totalConversas
) {}
