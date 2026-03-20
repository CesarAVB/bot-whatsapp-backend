package br.com.sistema.bot.service;

import br.com.sistema.bot.entity.BotTemplate;
import br.com.sistema.bot.entity.BotTemplateAuditoria;
import br.com.sistema.bot.repository.BotTemplateAuditoriaRepository;
import br.com.sistema.bot.repository.BotTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotTemplateService {

    private final BotTemplateRepository templateRepository;
    private final BotTemplateAuditoriaRepository auditoriaRepository;

    /**
     * Busca o texto do template pela chave.
     * Se não encontrar, retorna a própria chave entre colchetes como fallback seguro.
     */
    public String buscarTexto(String chave) {
        return templateRepository.findByChave(chave)
                .filter(BotTemplate::isAtivo)
                .map(BotTemplate::getTexto)
                .orElseGet(() -> {
                    log.warn("Template não encontrado para chave: {}", chave);
                    return "[" + chave + "]";
                });
    }

    /**
     * Busca o texto do template e substitui placeholders no formato {variavel}.
     */
    public String buscarTexto(String chave, Map<String, String> variaveis) {
        String texto = buscarTexto(chave);
        for (Map.Entry<String, String> entrada : variaveis.entrySet()) {
            texto = texto.replace("{" + entrada.getKey() + "}", entrada.getValue() != null ? entrada.getValue() : "");
        }
        return texto;
    }

    public List<BotTemplate> buscarTodos() {
        return templateRepository.findAll();
    }

    public Optional<BotTemplate> buscarPorChave(String chave) {
        return templateRepository.findByChave(chave);
    }

    public List<BotTemplateAuditoria> buscarAuditoria(String chave) {
        return auditoriaRepository.findByTemplateChaveOrderByAlteradoEmDesc(chave);
    }

    @Transactional
    public BotTemplate atualizar(String chave, String novoTexto, String alteradoPor) {
        BotTemplate template = templateRepository.findByChave(chave)
                .orElseThrow(() -> new IllegalArgumentException("Template não encontrado: " + chave));

        BotTemplateAuditoria auditoria = BotTemplateAuditoria.builder()
                .templateChave(chave)
                .textoAnterior(template.getTexto())
                .textoNovo(novoTexto)
                .alteradoPor(alteradoPor)
                .build();
        auditoriaRepository.save(auditoria);

        template.setTexto(novoTexto);
        return templateRepository.save(template);
    }

    @Transactional
    public void salvarSeNaoExistir(String chave, String texto, String descricao) {
        if (!templateRepository.existsByChave(chave)) {
            templateRepository.save(BotTemplate.builder()
                    .chave(chave)
                    .texto(texto)
                    .descricao(descricao)
                    .build());
        }
    }
}
