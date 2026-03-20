package br.com.sistema.bot.handler;

import br.com.sistema.bot.enums.BotState;
import br.com.sistema.bot.enums.TeamId;
import br.com.sistema.bot.model.ConversationContext;
import br.com.sistema.bot.service.BotTemplateService;
import br.com.sistema.bot.service.ChatwootService;
import br.com.sistema.bot.service.ConversationStateService;
import br.com.sistema.bot.service.HorarioAtendimentoService;
import br.com.sistema.bot.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SouClienteHandler implements MessageHandler {

    private final WhatsAppService whatsAppService;
    private final ConversationStateService conversationStateService;
    private final ChatwootService chatwootService;
    private final HorarioAtendimentoService horarioAtendimentoService;
    private final EncerrarHandler encerrarHandler;
    private final BotTemplateService templateService;

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
            case "1" -> transferir(ctx, TeamId.SUPORTE, "Suporte Técnico",
                    "domingo a domingo, das 09h às 21h", true);

            // ====================================================
            // Opção 2 — Financeiro: exibir submenu
            // ====================================================
            case "2" -> {
                conversationStateService.setState(ctx.phone(), BotState.FINANCEIRO);
                whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("sou_cliente.menu_financeiro"));
            }

            // ====================================================
            // Opção 3 — Dúvidas/Sugestões: horário seg-sáb 09h-18h
            // ====================================================
            case "3" -> transferir(ctx, TeamId.DUVIDAS_COMERCIAL, "Dúvidas/Sugestões",
                    "segunda a sábado, das 09h às 18h", false);

            // ====================================================
            // Opção 4 — Cancelamento: horário seg-sáb 09h-18h
            // ====================================================
            case "4" -> transferir(ctx, TeamId.CANCELAMENTO, "Cancelamento",
                    "segunda a sábado, das 09h às 18h", false);

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
    private void transferir(ConversationContext ctx, TeamId team, String nomeEquipe,
                            String horarios, boolean ehSuporte) {
        boolean disponivel = ehSuporte
                ? horarioAtendimentoService.isSuporteTecnicoDisponivel()
                : horarioAtendimentoService.isFinanceiroComercialDisponivel();

        if (disponivel) {
            String nota = "Cliente solicita: " + nomeEquipe;
            long chatwootId = chatwootService.transferir(ctx.phone(), ctx.senderName(), nota, team.getId());
            conversationStateService.setState(ctx.phone(), BotState.TRANSFERIDO);
            if (chatwootId > 0) conversationStateService.setChatwootConversationId(ctx.phone(), chatwootId);
            whatsAppService.enviarTexto(ctx.phone(),
                    templateService.buscarTexto("sou_cliente.transfer", Map.of("nomeEquipe", nomeEquipe)));
        } else {
            whatsAppService.enviarTexto(ctx.phone(),
                    templateService.buscarTexto("sou_cliente.fora_horario",
                            Map.of("nomeEquipe", nomeEquipe, "horarios", horarios)));
            conversationStateService.setState(ctx.phone(), BotState.MENU_INICIAL);
        }
    }
}
