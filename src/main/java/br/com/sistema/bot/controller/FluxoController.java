package br.com.sistema.bot.controller;

import br.com.sistema.bot.dtos.request.AdicionarMensagemRequest;
import br.com.sistema.bot.dtos.request.AtualizarConexaoRequest;
import br.com.sistema.bot.dtos.request.AtualizarNodePosicaoRequest;
import br.com.sistema.bot.dtos.request.CriarConexaoRequest;
import br.com.sistema.bot.dtos.response.FluxoBotResponse;
import br.com.sistema.bot.service.FluxoBotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fluxo")
@RequiredArgsConstructor
public class FluxoController {

    private final FluxoBotService fluxoBotService;

    // ── Leitura ───────────────────────────────────────────────────────────────

    @GetMapping
    public FluxoBotResponse obterFluxo() {
        return fluxoBotService.construirFluxo();
    }

    // ── Nós ──────────────────────────────────────────────────────────────────

    /** Atualiza posição (posX/posY) e opcionalmente o label de um nó. */
    @PatchMapping("/nodes/{id}")
    public ResponseEntity<Void> atualizarNode(
            @PathVariable String id,
            @Valid @RequestBody AtualizarNodePosicaoRequest request) {
        try {
            fluxoBotService.atualizarPosicao(id, request.posX(), request.posY(), request.label());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ── Mensagens (balões) ────────────────────────────────────────────────────

    /** Adiciona um balão de mensagem a um nó. */
    @PostMapping("/nodes/{nodeId}/mensagens")
    public ResponseEntity<Void> adicionarMensagem(
            @PathVariable String nodeId,
            @Valid @RequestBody AdicionarMensagemRequest request) {
        try {
            fluxoBotService.adicionarMensagem(nodeId, request.templateChave(), request.ordem());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** Remove um balão de mensagem. */
    @DeleteMapping("/mensagens/{id}")
    public ResponseEntity<Void> removerMensagem(@PathVariable String id) {
        fluxoBotService.removerMensagem(id);
        return ResponseEntity.noContent().build();
    }

    /** Atualiza a ordem de um balão dentro do nó. */
    @PatchMapping("/mensagens/{id}/ordem")
    public ResponseEntity<Void> atualizarOrdemMensagem(
            @PathVariable String id,
            @RequestParam int ordem) {
        try {
            fluxoBotService.atualizarOrdemMensagem(id, ordem);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ── Conexões (setas) ──────────────────────────────────────────────────────

    /** Cria uma seta entre dois nós. */
    @PostMapping("/conexoes")
    public ResponseEntity<Void> criarConexao(@Valid @RequestBody CriarConexaoRequest request) {
        try {
            fluxoBotService.criarConexao(
                    request.deNodeId(), request.paraNodeId(),
                    request.condicao(), request.label(), request.ordem());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** Atualiza condição, label e ordem de uma seta existente. */
    @PutMapping("/conexoes/{id}")
    public ResponseEntity<Void> atualizarConexao(
            @PathVariable String id,
            @Valid @RequestBody AtualizarConexaoRequest request) {
        try {
            fluxoBotService.atualizarConexao(id, request.condicao(), request.label(), request.ordem());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** Remove uma seta. */
    @DeleteMapping("/conexoes/{id}")
    public ResponseEntity<Void> removerConexao(@PathVariable String id) {
        fluxoBotService.removerConexao(id);
        return ResponseEntity.noContent().build();
    }
}
