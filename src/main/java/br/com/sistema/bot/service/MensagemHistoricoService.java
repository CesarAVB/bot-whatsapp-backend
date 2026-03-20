package br.com.sistema.bot.service;

import br.com.sistema.bot.entity.MensagemHistorico;
import br.com.sistema.bot.enums.Direcao;
import br.com.sistema.bot.repository.MensagemHistoricoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MensagemHistoricoService {

    private final MensagemHistoricoRepository repository;

    @Async
    public void registrarRecebida(String whatsappPhone, String conteudo) {
        registrar(whatsappPhone, Direcao.RECEBIDA, conteudo);
    }

    @Async
    public void registrarEnviada(String whatsappPhone, String conteudo) {
        registrar(whatsappPhone, Direcao.ENVIADA, conteudo);
    }

    private void registrar(String whatsappPhone, Direcao direcao, String conteudo) {
        try {
            repository.save(MensagemHistorico.builder()
                    .whatsappPhone(whatsappPhone)
                    .direcao(direcao)
                    .conteudo(conteudo)
                    .build());
        } catch (Exception e) {
            log.error("Erro ao registrar histórico de mensagem [{}] para {}: {}", direcao, whatsappPhone, e.getMessage());
        }
    }

    public List<MensagemHistorico> buscarPorPhone(String whatsappPhone) {
        return repository.findByWhatsappPhoneOrderByCreatedAtAsc(whatsappPhone);
    }
}
