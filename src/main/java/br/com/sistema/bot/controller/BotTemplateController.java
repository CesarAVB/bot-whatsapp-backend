package br.com.sistema.bot.controller;

import br.com.sistema.bot.dtos.request.AtualizarTemplateRequest;
import br.com.sistema.bot.dtos.response.BotTemplateAuditoriaResponse;
import br.com.sistema.bot.dtos.response.BotTemplateResponse;
import br.com.sistema.bot.service.BotTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
public class BotTemplateController {

    private final BotTemplateService templateService;

    @GetMapping
    public List<BotTemplateResponse> listarTodos() {
        return templateService.buscarTodos().stream()
                .map(BotTemplateResponse::from)
                .toList();
    }

    @GetMapping("/{chave}")
    public ResponseEntity<BotTemplateResponse> buscarPorChave(@PathVariable String chave) {
        return templateService.buscarPorChave(chave)
                .map(BotTemplateResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{chave}")
    public ResponseEntity<BotTemplateResponse> atualizar(
            @PathVariable String chave,
            @Valid @RequestBody AtualizarTemplateRequest request) {
        try {
            var atualizado = templateService.atualizar(chave, request.texto(), request.alteradoPor());
            return ResponseEntity.ok(BotTemplateResponse.from(atualizado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{chave}/auditoria")
    public List<BotTemplateAuditoriaResponse> buscarAuditoria(@PathVariable String chave) {
        return templateService.buscarAuditoria(chave).stream()
                .map(BotTemplateAuditoriaResponse::from)
                .toList();
    }
}
