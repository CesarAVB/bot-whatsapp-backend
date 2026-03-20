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
public class FinanceiroHandler implements MessageHandler {

    private final WhatsAppService whatsAppService;
    private final ConversationStateService conversationStateService;
    private final ChatwootService chatwootService;
    private final HorarioAtendimentoService horarioAtendimentoService;
    private final EncerrarHandler encerrarHandler;

    @Override
    public boolean canHandle(ConversationContext ctx) {
        return ctx.currentState() == BotState.FINANCEIRO;
    }

    @Override
    public void handle(ConversationContext ctx) {
        String content = ctx.content() != null ? ctx.content().trim() : "";

        switch (content) {
            // ====================================================
            // Opção 1 — Segunda via: solicitar CPF, estado muda para AGUARDA_CPF_FATURA
            // ====================================================
            case "1" -> {
                conversationStateService.setState(ctx.phone(), BotState.AGUARDA_CPF_FATURA);
                whatsAppService.enviarTexto(ctx.phone(),
                        "Por favor, informe seu *CPF* ou *CNPJ* (somente números):");
            }

            // ====================================================
            // Opção 2 — Desbloqueio: solicitar CPF, estado muda para AGUARDA_CPF_DESBLOQUEIO
            // ====================================================
            case "2" -> {
                conversationStateService.setState(ctx.phone(), BotState.AGUARDA_CPF_DESBLOQUEIO);
                whatsAppService.enviarTexto(ctx.phone(),
                        "Por favor, informe seu *CPF* ou *CNPJ* (somente números):");
            }

            // ====================================================
            // Opção 3 — Comprovante: instrução para anexar imagem/PDF
            // ====================================================
            case "3" -> whatsAppService.enviarTexto(ctx.phone(),
                    "Para enviar seu comprovante, basta *anexar a imagem ou PDF* nesta conversa.\n\n" +
                    "Nossa equipe irá verificar e dar retorno em breve. ✅");

            // ====================================================
            // Opção 4 — Falar com atendente financeiro
            // ====================================================
            case "4" -> {
                if (horarioAtendimentoService.isFinanceiroComercialDisponivel()) {
                    long chatwootId = chatwootService.transferir(
                            ctx.phone(), ctx.senderName(),
                            "Cliente solicita atendimento financeiro.",
                            TeamId.FINANCEIRO.getId()
                    );
                    conversationStateService.setState(ctx.phone(), BotState.TRANSFERIDO);
                    if (chatwootId > 0) conversationStateService.setChatwootConversationId(ctx.phone(), chatwootId);
                    whatsAppService.enviarTexto(ctx.phone(),
                            "Transferindo para a equipe *Financeira*. Em breve um atendente irá lhe atender! 😊");
                } else {
                    whatsAppService.enviarTexto(ctx.phone(),
                            "Nossa equipe financeira atende *segunda a sábado, das 09h às 18h*.\n" +
                            "No momento estamos fora do horário. Por favor, retorne dentro do horário. 🙏");
                    conversationStateService.setState(ctx.phone(), BotState.MENU_INICIAL);
                }
            }

            // ====================================================
            // Opção 5 — Encerrar atendimento
            // ====================================================
            case "5" -> encerrarHandler.handle(ctx);

            default -> whatsAppService.enviarTexto(ctx.phone(),
                    "Opção inválida. Por favor, escolha entre 1 e 5:");
        }
    }
}
