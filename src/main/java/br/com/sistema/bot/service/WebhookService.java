package br.com.sistema.bot.service;

import br.com.sistema.bot.dtos.request.WhatsAppWebhookChange;
import br.com.sistema.bot.dtos.request.WhatsAppWebhookContact;
import br.com.sistema.bot.dtos.request.WhatsAppWebhookMessage;
import br.com.sistema.bot.dtos.request.WhatsAppWebhookRequest;
import br.com.sistema.bot.dtos.request.WhatsAppWebhookValue;
import br.com.sistema.bot.engine.FluxoEngine;
import br.com.sistema.bot.engine.FluxoExecucaoCtx;
import br.com.sistema.bot.entity.ConversationState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final FluxoEngine fluxoEngine;
    private final ConversationStateService conversationStateService;
    private final MessageLogService messageLogService;
    private final MensagemHistoricoService mensagemHistoricoService;

    // ====================================================
    // processar - Ponto de entrada: itera entries/changes do payload da WhatsApp Cloud API
    // ====================================================
    public void processar(WhatsAppWebhookRequest payload) {
        if (payload.entry() == null) return;

        for (var entry : payload.entry()) {
            if (entry.changes() == null) continue;
            for (WhatsAppWebhookChange change : entry.changes()) {
                if (!"messages".equals(change.field())) continue;
                processarValue(change.value());
            }
        }
    }

    // ====================================================
    // processarValue - Extrai mensagem do value e aplica filtros
    // ====================================================
    private void processarValue(WhatsAppWebhookValue value) {
        if (value == null || value.messages() == null || value.messages().isEmpty()) return;

        WhatsAppWebhookMessage msg = value.messages().get(0);

        // Somente mensagens de texto — ignorar áudio, imagem, status etc.
        if (!"text".equals(msg.type()) || msg.text() == null) {
            log.debug("Mensagem não-texto ignorada. type={}", msg.type());
            return;
        }

        // Idempotência — WhatsApp pode reenviar o mesmo webhook
        if (messageLogService.jaProcessado(msg.id())) {
            log.info("Mensagem {} já processada, ignorando duplicata", msg.id());
            return;
        }

        String phone      = msg.from();
        String content    = msg.text().body();
        String senderName = extrairNome(value.contacts());

        mensagemHistoricoService.registrarRecebida(phone, content);

        ConversationState state = conversationStateService.buscarOuCriar(phone);

        // Conversa transferida para humano — bot não interfere
        if (state.isTransferidoParaHumano()) {
            log.debug("Conversa {} com humano. Bot ignorando mensagem.", phone);
            messageLogService.registrar(msg.id(), phone);
            return;
        }

        FluxoExecucaoCtx ctx = new FluxoExecucaoCtx(
                phone,
                msg.id(),
                senderName,
                content,
                state.getContextData()
        );

        log.info("Processando msg {} | {} | nó='{}' | conteúdo='{}'",
                msg.id(), phone, state.getCurrentNodeKey(), content);

        try {
            fluxoEngine.processar(ctx);
            messageLogService.registrar(msg.id(), phone);
        } catch (Exception e) {
            log.error("Erro ao processar mensagem do cliente {}", phone, e);
        }
    }

    // ====================================================
    // extrairNome - Obtém nome do remetente a partir dos contacts do payload
    // ====================================================
    private String extrairNome(List<WhatsAppWebhookContact> contacts) {
        if (contacts == null || contacts.isEmpty()) return "Cliente";
        var contact = contacts.get(0);
        if (contact.profile() != null && contact.profile().name() != null) {
            return contact.profile().name();
        }
        return "Cliente";
    }
}
