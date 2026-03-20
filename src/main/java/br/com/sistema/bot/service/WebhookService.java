package br.com.sistema.bot.service;

import br.com.sistema.bot.dtos.request.WhatsAppWebhookChange;
import br.com.sistema.bot.dtos.request.WhatsAppWebhookContact;
import br.com.sistema.bot.dtos.request.WhatsAppWebhookMessage;
import br.com.sistema.bot.dtos.request.WhatsAppWebhookRequest;
import br.com.sistema.bot.dtos.request.WhatsAppWebhookValue;
import br.com.sistema.bot.entity.ConversationState;
import br.com.sistema.bot.enums.BotState;
import br.com.sistema.bot.handler.ConfirmaCpfHandler;
import br.com.sistema.bot.handler.EncerrarHandler;
import br.com.sistema.bot.handler.FinanceiroHandler;
import br.com.sistema.bot.handler.MenuInicialHandler;
import br.com.sistema.bot.handler.MessageHandler;
import br.com.sistema.bot.handler.ProcessaCpfCnpjHandler;
import br.com.sistema.bot.handler.SouClienteHandler;
import br.com.sistema.bot.model.ConversationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final MenuInicialHandler menuInicialHandler;
    private final SouClienteHandler souClienteHandler;
    private final FinanceiroHandler financeiroHandler;
    private final ProcessaCpfCnpjHandler processaCpfCnpjHandler;
    private final ConfirmaCpfHandler confirmaCpfHandler;
    private final EncerrarHandler encerrarHandler;
    private final ConversationStateService conversationStateService;
    private final MessageLogService messageLogService;

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
        messageLogService.registrar(msg.id(), msg.from());

        String phone = msg.from();
        String content = msg.text().body();
        String senderName = extrairNome(value.contacts());

        ConversationState state = conversationStateService.buscarOuCriar(phone);

        // Conversa transferida para humano — bot não interfere
        if (state.getCurrentState() == BotState.TRANSFERIDO) {
            log.debug("Conversa {} com humano. Bot ignorando mensagem.", phone);
            return;
        }

        // Estado ENCERRADO reinicia o fluxo como novo contato
        if (state.getCurrentState() == BotState.ENCERRADO) {
            conversationStateService.setState(phone, BotState.MENU_INICIAL);
            state.setCurrentState(BotState.MENU_INICIAL);
        }

        ConversationContext ctx = new ConversationContext(
                phone,
                msg.id(),
                senderName,
                content,
                state.getCurrentState(),
                state.getContextData()
        );

        log.info("Processando msg {} | {} | estado={} | conteúdo='{}'",
                msg.id(), phone, ctx.currentState(), content);

        rotearEProcessar(ctx);
    }

    // ====================================================
    // rotearEProcessar - Comando global + roteamento por estado
    // ====================================================
    private void rotearEProcessar(ConversationContext ctx) {
        String content = ctx.content() != null ? ctx.content().trim().toLowerCase() : "";

        // Comando global: "sair" ou "cancelar" encerra em qualquer estado
        if ("sair".equals(content) || "cancelar".equals(content)) {
            encerrarHandler.handle(ctx);
            return;
        }

        MessageHandler handler = selecionarHandler(ctx);
        try {
            handler.handle(ctx);
        } catch (Exception e) {
            log.error("Erro ao processar mensagem do cliente {}", ctx.phone(), e);
        }
    }

    // ====================================================
    // selecionarHandler - Roteamento por BotState (ordem de prioridade)
    // ====================================================
    private MessageHandler selecionarHandler(ConversationContext ctx) {
        return switch (ctx.currentState()) {
            case AGUARDA_CPF_FATURA, AGUARDA_CPF_DESBLOQUEIO          -> processaCpfCnpjHandler;
            case CONFIRMA_IDENTIDADE_FATURA, CONFIRMA_IDENTIDADE_DESBLOQUEIO -> confirmaCpfHandler;
            case FINANCEIRO                                             -> financeiroHandler;
            case SOU_CLIENTE                                           -> souClienteHandler;
            default                                                    -> menuInicialHandler;
        };
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
