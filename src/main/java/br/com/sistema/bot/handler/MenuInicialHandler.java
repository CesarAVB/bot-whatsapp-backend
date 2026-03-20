package br.com.sistema.bot.handler;

import br.com.sistema.bot.enums.BotState;
import br.com.sistema.bot.enums.TeamId;
import br.com.sistema.bot.model.ConversationContext;
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

    private static final String MENU_BOAS_VINDAS = """
            Olá! Seja bem-vindo(a) à *ASB Telecom*! 🌐

            Como podemos ajudar você hoje?

            1️⃣ Sou cliente ASB
            2️⃣ Quero me tornar cliente

            Digite o número da opção desejada:""";

    private static final String MENU_SOU_CLIENTE = """
            Ótimo! Como posso ajudar?

            1️⃣ Assistência Técnica
            2️⃣ Financeiro
            3️⃣ Dúvidas/Sugestões
            4️⃣ Cancelamento
            5️⃣ Encerrar atendimento

            Digite o número da opção desejada:""";

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
            whatsAppService.enviarTexto(ctx.phone(), MENU_BOAS_VINDAS);
            return;
        }

        switch (content) {
            case "1" -> {
                conversationStateService.setState(ctx.phone(), BotState.SOU_CLIENTE);
                whatsAppService.enviarTexto(ctx.phone(), MENU_SOU_CLIENTE);
            }
            case "2" -> {
                long chatwootId = chatwootService.transferir(
                        ctx.phone(), ctx.senderName(),
                        "Cliente " + ctx.senderName() + " quer se tornar cliente ASB.",
                        TeamId.DUVIDAS_COMERCIAL.getId()
                );
                conversationStateService.setState(ctx.phone(), BotState.TRANSFERIDO);
                if (chatwootId > 0) conversationStateService.setChatwootConversationId(ctx.phone(), chatwootId);
                whatsAppService.enviarTexto(ctx.phone(),
                        "Aguarde! Vamos conectar você com nossa equipe comercial. Em breve um atendente irá lhe chamar! 😊");
            }
            default -> whatsAppService.enviarTexto(ctx.phone(), MENU_BOAS_VINDAS);
        }
    }
}
