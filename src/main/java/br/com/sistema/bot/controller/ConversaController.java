package br.com.sistema.bot.controller;

import br.com.sistema.bot.dtos.response.ConversaDetalheResponse;
import br.com.sistema.bot.dtos.response.ConversaResumoResponse;
import br.com.sistema.bot.dtos.response.MensagemHistoricoResponse;
import br.com.sistema.bot.repository.ConversationStateRepository;
import br.com.sistema.bot.service.MensagemHistoricoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conversas")
@RequiredArgsConstructor
public class ConversaController {

    private final ConversationStateRepository conversationStateRepository;
    private final MensagemHistoricoService mensagemHistoricoService;

    @GetMapping
    public Page<ConversaResumoResponse> listar(
            @PageableDefault(size = 20, sort = "updatedAt") Pageable pageable) {
        return conversationStateRepository.findAll(pageable)
                .map(ConversaResumoResponse::from);
    }

    @GetMapping("/{phone}")
    public ResponseEntity<ConversaDetalheResponse> buscarPorPhone(@PathVariable String phone) {
        return conversationStateRepository.findByWhatsappPhone(phone)
                .map(state -> {
                    List<MensagemHistoricoResponse> historico =
                            mensagemHistoricoService.buscarPorPhone(phone).stream()
                                    .map(MensagemHistoricoResponse::from)
                                    .toList();
                    return ResponseEntity.ok(ConversaDetalheResponse.from(state, historico));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
