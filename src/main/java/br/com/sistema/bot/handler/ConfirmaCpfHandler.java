package br.com.sistema.bot.handler;

import br.com.sistema.bot.dtos.response.HubsoftClienteItem;
import br.com.sistema.bot.dtos.response.HubsoftClienteResponse;
import br.com.sistema.bot.dtos.response.HubsoftDesbloqueioResponse;
import br.com.sistema.bot.dtos.response.HubsoftFaturaItem;
import br.com.sistema.bot.dtos.response.HubsoftFaturaResponse;
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
public class ConfirmaCpfHandler implements MessageHandler {

    private final WhatsAppService whatsAppService;
    private final ConversationStateService conversationStateService;
    private final HubsoftService hubsoftService;
    private final BotTemplateService templateService;

    @Override
    public boolean canHandle(ConversationContext ctx) {
        return ctx.currentState() == BotState.CONFIRMA_IDENTIDADE_FATURA
                || ctx.currentState() == BotState.CONFIRMA_IDENTIDADE_DESBLOQUEIO;
    }

    @Override
    public void handle(ConversationContext ctx) {
        String content = ctx.content() != null ? ctx.content().trim() : "";

        switch (content) {
            // ====================================================
            // "1" — Confirmação positiva: prosseguir com o fluxo
            // ====================================================
            case "1" -> confirmar(ctx);

            // ====================================================
            // "2" — Confirmação negativa: encerrar
            // ====================================================
            case "2" -> {
                conversationStateService.setState(ctx.phone(), BotState.MENU_INICIAL);
                whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("confirma.nao_titular"));
            }

            default -> whatsAppService.enviarTexto(ctx.phone(),
                    templateService.buscarTexto("confirma.opcao_invalida"));
        }
    }

    // ====================================================
    // confirmar - O CPF está em ctx.contextData() — sem round-trip ao Chatwoot
    // ====================================================
    private void confirmar(ConversationContext ctx) {
        String cpfCnpj = ctx.contextData();

        if (cpfCnpj == null || cpfCnpj.isBlank()) {
            log.error("contextData (CPF) ausente para {}", ctx.phone());
            conversationStateService.setState(ctx.phone(), BotState.MENU_INICIAL);
            whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("confirma.erro"));
            return;
        }

        if (ctx.currentState() == BotState.CONFIRMA_IDENTIDADE_FATURA) {
            processarFatura(ctx, cpfCnpj);
        } else {
            processarDesbloqueio(ctx, cpfCnpj);
        }
    }

    // ====================================================
    // processarFatura - Busca faturas em aberto e envia o boleto
    // ====================================================
    private void processarFatura(ConversationContext ctx, String cpfCnpj) {
        conversationStateService.setState(ctx.phone(), BotState.MENU_INICIAL);

        HubsoftFaturaResponse faturas;
        try {
            faturas = hubsoftService.buscarFaturas(cpfCnpj);
        } catch (Exception e) {
            log.error("Erro ao buscar faturas. CPF/CNPJ: {}", cpfCnpj, e);
            whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("confirma.erro"));
            return;
        }

        if (faturas.faturas() == null || faturas.faturas().isEmpty()) {
            whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("confirma.sem_fatura"));
            return;
        }

        HubsoftFaturaItem fatura = faturas.faturas().get(0);

        if (fatura.link() != null && !fatura.link().isBlank()) {
            whatsAppService.enviarDocumento(
                    ctx.phone(),
                    fatura.link(),
                    "boleto-" + fatura.dataVencimento() + ".pdf",
                    "Vencimento: " + fatura.dataVencimento() + "\nLinha digitável: " + fatura.linhaDigitavel()
            );
        } else {
            whatsAppService.enviarTexto(ctx.phone(),
                    templateService.buscarTexto("confirma.fatura",
                            Map.of("data", fatura.dataVencimento(), "linha", fatura.linhaDigitavel())));
        }
    }

    // ====================================================
    // processarDesbloqueio - Realiza desbloqueio de confiança via Hubsoft
    // ====================================================
    private void processarDesbloqueio(ConversationContext ctx, String cpfCnpj) {
        conversationStateService.setState(ctx.phone(), BotState.MENU_INICIAL);

        HubsoftClienteResponse clienteResponse;
        try {
            clienteResponse = hubsoftService.buscarClientePorCpfCnpj(cpfCnpj);
        } catch (Exception e) {
            log.error("Erro ao buscar cliente para desbloqueio. CPF/CNPJ: {}", cpfCnpj, e);
            whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("confirma.erro"));
            return;
        }

        if (clienteResponse.clientes() == null || clienteResponse.clientes().isEmpty()) {
            whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("confirma.sem_cadastro_desbloqueio"));
            return;
        }

        HubsoftClienteItem cliente = clienteResponse.clientes().get(0);

        if (cliente.servicos() == null || cliente.servicos().isEmpty()) {
            whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("confirma.sem_servico"));
            return;
        }

        long idServico = cliente.servicos().get(0).id();

        HubsoftDesbloqueioResponse resultado;
        try {
            resultado = hubsoftService.desbloquear(idServico);
        } catch (Exception e) {
            log.error("Erro ao desbloquear serviço {}", idServico, e);
            whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("confirma.erro"));
            return;
        }

        if ("success".equalsIgnoreCase(resultado.status())) {
            whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("confirma.desbloqueio_sucesso"));
        } else {
            whatsAppService.enviarTexto(ctx.phone(), templateService.buscarTexto("confirma.desbloqueio_falha"));
        }
    }
}
