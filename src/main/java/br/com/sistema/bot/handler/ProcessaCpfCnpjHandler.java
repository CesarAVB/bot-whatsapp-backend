package br.com.sistema.bot.handler;

import br.com.sistema.bot.dtos.response.HubsoftClienteResponse;
import br.com.sistema.bot.enums.BotState;
import br.com.sistema.bot.model.ConversationContext;
import br.com.sistema.bot.service.BotTemplateService;
import br.com.sistema.bot.service.ConversationStateService;
import br.com.sistema.bot.service.HubsoftService;
import br.com.sistema.bot.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessaCpfCnpjHandler implements MessageHandler {

    private final WhatsAppService whatsAppService;
    private final ConversationStateService conversationStateService;
    private final HubsoftService hubsoftService;
    private final BotTemplateService templateService;

    @Override
    public boolean canHandle(ConversationContext ctx) {
        return ctx.currentState() == BotState.AGUARDA_CPF_FATURA
                || ctx.currentState() == BotState.AGUARDA_CPF_DESBLOQUEIO;
    }

    @Override
    public void handle(ConversationContext ctx) {
        String cpfCnpj = ctx.content() != null
                ? ctx.content().replaceAll("[^0-9]", "")
                : "";

        // ====================================================
        // Validação: somente dígitos, 11 (CPF) ou 14 (CNPJ)
        // ====================================================
        if (!isCpfCnpjValido(cpfCnpj)) {
            whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("cpf.invalido"));
            return;
        }

        // ====================================================
        // Consulta no Hubsoft para obter o nome do titular
        // ====================================================
        HubsoftClienteResponse resposta;
        try {
            resposta = hubsoftService.buscarClientePorCpfCnpj(cpfCnpj);
        } catch (Exception e) {
            log.error("Erro ao buscar cliente no Hubsoft. CPF/CNPJ: {}", cpfCnpj, e);
            whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("cpf.erro_consulta"));
            return;
        }

        if (resposta.clientes() == null || resposta.clientes().isEmpty()) {
            whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("cpf.nao_encontrado"));
            return;
        }

        // ====================================================
        // Transição: persiste CPF no contextData e avança para confirmação
        // Estado depende do fluxo em curso (fatura ou desbloqueio)
        // ====================================================
        String nome = resposta.clientes().get(0).nomeRazaosocial();
        BotState proximoEstado = ctx.currentState() == BotState.AGUARDA_CPF_FATURA
                ? BotState.CONFIRMA_IDENTIDADE_FATURA
                : BotState.CONFIRMA_IDENTIDADE_DESBLOQUEIO;

        conversationStateService.setStateComContexto(ctx.phone(), proximoEstado, cpfCnpj);

        whatsAppService.enviarTexto(ctx.phone(),
                templateService.buscarTexto("cpf.confirma_titular", Map.of("nome", nome)));
    }

    private boolean isCpfCnpjValido(String digits) {
        return digits.length() == 11 || digits.length() == 14;
    }
}
