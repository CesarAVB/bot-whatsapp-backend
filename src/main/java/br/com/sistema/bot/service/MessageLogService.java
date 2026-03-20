package br.com.sistema.bot.service;

import br.com.sistema.bot.entity.MessageLog;
import br.com.sistema.bot.repository.MessageLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageLogService {

    private final MessageLogRepository messageLogRepository;

    // ====================================================
    // jaProcessado - Verifica idempotência pelo ID da mensagem do WhatsApp (wamid.xxx)
    // WhatsApp pode reenviar o mesmo webhook em caso de timeout
    // ====================================================
    public boolean jaProcessado(String messageId) {
        return messageLogRepository.existsByMessageId(messageId);
    }

    // ====================================================
    // registrar - Persiste o messageId para evitar processamento duplicado
    // ====================================================
    @Transactional
    public void registrar(String messageId, String senderPhone) {
        try {
            MessageLog entrada = MessageLog.builder()
                    .messageId(messageId)
                    .senderPhone(senderPhone)
                    .build();
            messageLogRepository.save(entrada);
        } catch (Exception e) {
            // Violação de unique constraint em race condition — mensagem já registrada
            log.debug("Mensagem {} já registrada (race condition ignorada)", messageId);
        }
    }
}
