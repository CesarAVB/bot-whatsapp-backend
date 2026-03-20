package br.com.sistema.bot.handler;

import br.com.sistema.bot.enums.BotState;
import br.com.sistema.bot.enums.TeamId;
import br.com.sistema.bot.model.ConversationContext;
import br.com.sistema.bot.service.BotTemplateService;
import br.com.sistema.bot.service.ChatwootService;
import br.com.sistema.bot.service.ConversationStateService;
import br.com.sistema.bot.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuInicialHandler implements MessageHandler {

    private final WhatsAppService whatsAppService;
    private final ConversationStateService conversationStateService;
    private final ChatwootService chatwootService;
    private final BotTemplateService templateService;

    @Override
    public boolean canHandle(ConversationContext ctx) {
        return ctx.currentState() == BotState.MENU_INICIAL;
    }

    @Override
    public void handle(ConversationContext ctx) {
        String content = ctx.content() != null ? ctx.content().trim() : "";

        // ====================================================
        // Novo contato ou retorno ao menu — exibe boas-vindas e aguarda opção
        // ====================================================
        if (content.isEmpty()) {
            whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("menu.inicial.boas_vindas"));
            return;
        }

        switch (content) {
            case "1" -> {
                conversationStateService.setState(ctx.phone(), BotState.SOU_CLIENTE);
                whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("menu.inicial.opcoes_cliente"));
            }
            case "2" -> {
                long chatwootId = chatwootService.transferir(
                        ctx.phone(), ctx.senderName(),
                        "Cliente " + ctx.senderName() + " quer se tornar cliente ASB.",
                        TeamId.DUVIDAS_COMERCIAL.getId()
                );
                conversationStateService.setState(ctx.phone(), BotState.TRANSFERIDO);
                if (chatwootId > 0) conversationStateService.setChatwootConversationId(ctx.phone(), chatwootId);
                whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("menu.inicial.comercial_transfer"));
            }
            default -> whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("menu.inicial.boas_vindas"));
        }
    }
}
