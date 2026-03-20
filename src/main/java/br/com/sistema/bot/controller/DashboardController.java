package br.com.sistema.bot.controller;

import br.com.sistema.bot.dtos.response.DashboardMetricasResponse;
import br.com.sistema.bot.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/metricas")
    public DashboardMetricasResponse buscarMetricas() {
        return new DashboardMetricasResponse(
                dashboardService.buscarContagemPorEstado(),
                dashboardService.buscarTotal()
        );
    }
}
