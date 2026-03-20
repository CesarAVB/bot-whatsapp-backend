package br.com.sistema.bot.handler;

import br.com.sistema.bot.enums.BotState;
import br.com.sistema.bot.enums.TeamId;
import br.com.sistema.bot.model.ConversationContext;
import br.com.sistema.bot.service.ChatwootService;
import br.com.sistema.bot.service.ConversationStateService;
import br.com.sistema.bot.service.HorarioAtendimentoService;
import br.com.sistema.bot.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SouClienteHandler implements MessageHandler {

    private final WhatsAppService whatsAppService;
    private final ConversationStateService conversationStateService;
    private final ChatwootService chatwootService;
    private final HorarioAtendimentoService horarioAtendimentoService;
    private final EncerrarHandler encerrarHandler;

    private static final String MENU_FINANCEIRO = """
            💰 *Menu Financeiro*

            1️⃣ Segunda via de boleto
            2️⃣ Desbloqueio de confiança
            3️⃣ Enviar comprovante de pagamento
            4️⃣ Falar com atendente financeiro
            5️⃣ Encerrar atendimento

            Digite o número da opção desejada:""";

    @Override
    public boolean canHandle(ConversationContext ctx) {
        return ctx.currentState() == BotState.SOU_CLIENTE;
    }

    @Override
    public void handle(ConversationContext ctx) {
        String content = ctx.content() != null ? ctx.content().trim() : "";

        switch (content) {
            // ====================================================
            // Opção 1 — Assistência Técnica: horário domingo a domingo 09h-21h
            // ====================================================
            case "1" -> transferir(ctx, TeamId.SUPORTE, "Suporte Técnico", true);

            // ====================================================
            // Opção 2 — Financeiro: exibir submenu
            // ====================================================
            case "2" -> {
                conversationStateService.setState(ctx.phone(), BotState.FINANCEIRO);
                whatsAppService.enviarTexto(ctx.phone(), MENU_FINANCEIRO);
            }

            // ====================================================
            // Opção 3 — Dúvidas/Sugestões: horário seg-sáb 09h-18h
            // ====================================================
            case "3" -> transferir(ctx, TeamId.DUVIDAS_COMERCIAL, "Dúvidas/Sugestões", false);

            // ====================================================
            // Opção 4 — Cancelamento: horário seg-sáb 09h-18h
            // ====================================================
            case "4" -> transferir(ctx, TeamId.CANCELAMENTO, "Cancelamento", false);

            // ====================================================
            // Opção 5 — Encerrar
            // ====================================================
            case "5" -> encerrarHandler.handle(ctx);

            default -> whatsAppService.enviarTexto(ctx.phone(),
                    "Opção inválida. Por favor, escolha entre 1 e 5:");
        }
    }

    // ====================================================
    // transferir - Verifica horário, cria conversa no Chatwoot e transfere
    // ====================================================
    private void transferir(ConversationContext ctx, TeamId team, String nomeEquipe, boolean ehSuporte) {
        boolean disponivel = ehSuporte
                ? horarioAtendimentoService.isSuporteTecnicoDisponivel()
                : horarioAtendimentoService.isFinanceiroComercialDisponivel();

        if (disponivel) {
            String nota = "Cliente solicita: " + nomeEquipe;
            long chatwootId = chatwootService.transferir(ctx.phone(), ctx.senderName(), nota, team.getId());
            conversationStateService.setState(ctx.phone(), BotState.TRANSFERIDO);
            if (chatwootId > 0) conversationStateService.setChatwootConversationId(ctx.phone(), chatwootId);
            whatsAppService.enviarTexto(ctx.phone(),
                    "Transferindo para a equipe de *" + nomeEquipe + "*. Em breve um atendente irá lhe atender! 😊");
        } else {
            String horarios = ehSuporte ? "domingo a domingo, das 09h às 21h" : "segunda a sábado, das 09h às 18h";
            whatsAppService.enviarTexto(ctx.phone(),
                    "Nossa equipe de *" + nomeEquipe + "* atende " + horarios + ".\n" +
                    "No momento estamos fora do horário. Retorne dentro do horário de atendimento. 🙏");
            conversationStateService.setState(ctx.phone(), BotState.MENU_INICIAL);
        }
    }
}
