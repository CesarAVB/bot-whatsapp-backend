package br.com.sistema.bot.handler;

import br.com.sistema.bot.model.ConversationContext;
import br.com.sistema.bot.service.BotTemplateService;
import br.com.sistema.bot.service.ConversationStateService;
import br.com.sistema.bot.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EncerrarHandler implements MessageHandler {

    private final WhatsAppService whatsAppService;
    private final ConversationStateService conversationStateService;
    private final BotTemplateService templateService;

    @Override
    public boolean canHandle(ConversationContext ctx) {
        String content = ctx.content() != null ? ctx.content().trim().toLowerCase() : "";
        return "sair".equals(content) || "cancelar".equals(content);
    }

    @Override
    public void handle(ConversationContext ctx) {
        // ====================================================
        // Encerrar - envia despedida e volta ao estado inicial
        // Próxima mensagem do cliente recomeça o fluxo do zero
        // ====================================================
        conversationStateService.resetar(ctx.phone());
        whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("encerrar.despedida"));
        log.info("Atendimento encerrado para {}", ctx.phone());
    }
}
