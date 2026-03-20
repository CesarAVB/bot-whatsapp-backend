package br.com.sistema.bot.controller;

import br.com.sistema.bot.dtos.response.FluxoBotResponse;
import br.com.sistema.bot.service.FluxoBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fluxo")
@RequiredArgsConstructor
public class FluxoController {

    private final FluxoBotService fluxoBotService;

    @GetMapping
    public FluxoBotResponse obterFluxo() {
        return fluxoBotService.construirFluxo();
    }
}
