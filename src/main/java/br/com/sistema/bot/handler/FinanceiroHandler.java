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

@Slf4j
@Component
@RequiredArgsConstructor
public class FinanceiroHandler implements MessageHandler {

    private final WhatsAppService whatsAppService;
    private final ConversationStateService conversationStateService;
    private final ChatwootService chatwootService;
    private final HorarioAtendimentoService horarioAtendimentoService;
    private final EncerrarHandler encerrarHandler;
    private final BotTemplateService templateService;

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
                whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("financeiro.solicitar_cpf"));
            }

            // ====================================================
            // Opção 2 — Desbloqueio: solicitar CPF, estado muda para AGUARDA_CPF_DESBLOQUEIO
            // ====================================================
            case "2" -> {
                conversationStateService.setState(ctx.phone(), BotState.AGUARDA_CPF_DESBLOQUEIO);
                whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("financeiro.solicitar_cpf"));
            }

            // ====================================================
            // Opção 3 — Comprovante: instrução para anexar imagem/PDF
            // ====================================================
            case "3" -> whatsAppService.enviarTexto(ctx.phone(),
                    templateService.buscarTexto("financeiro.comprovante"));

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
                    whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("financeiro.transfer"));
                } else {
                    whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("financeiro.fora_horario"));
                    conversationStateService.setState(ctx.phone(), BotState.MENU_INICIAL);
                }
            }

            // ====================================================
            // Opção 5 — Encerrar atendimento
            // ====================================================
            case "5" -> encerrarHandler.handle(ctx);

            default -> whatsAppService.enviarTexto(ctx.phone(),
                    templateService.buscarTexto("financeiro.opcao_invalida"));
        }
    }
}
