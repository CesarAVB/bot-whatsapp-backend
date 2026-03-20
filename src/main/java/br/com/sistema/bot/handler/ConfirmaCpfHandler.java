package br.com.sistema.bot.handler;

import br.com.sistema.bot.dtos.response.HubsoftClienteItem;
import br.com.sistema.bot.dtos.response.HubsoftClienteResponse;
import br.com.sistema.bot.dtos.response.HubsoftDesbloqueioResponse;
import br.com.sistema.bot.dtos.response.HubsoftFaturaItem;
import br.com.sistema.bot.dtos.response.HubsoftFaturaResponse;
import br.com.sistema.bot.enums.BotState;
import br.com.sistema.bot.model.ConversationContext;
import br.com.sistema.bot.service.ConversationStateService;
import br.com.sistema.bot.service.HubsoftService;
import br.com.sistema.bot.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConfirmaCpfHandler implements MessageHandler {

    private final WhatsAppService whatsAppService;
    private final ConversationStateService conversationStateService;
    private final HubsoftService hubsoftService;

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
                whatsAppService.enviarTexto(ctx.phone(),
                        "Entendido! Verifique seus dados e tente novamente, ou entre em contato com o suporte.");
            }

            default -> whatsAppService.enviarTexto(ctx.phone(),
                    "Por favor, responda:\n1️⃣ Sim\n2️⃣ Não");
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
            whatsAppService.enviarTexto(ctx.phone(),
                    "Ocorreu um erro. Por favor, inicie o processo novamente.");
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
            whatsAppService.enviarTexto(ctx.phone(),
                    "Ocorreu um erro ao consultar suas faturas. Tente novamente.");
            return;
        }

        if (faturas.faturas() == null || faturas.faturas().isEmpty()) {
            whatsAppService.enviarTexto(ctx.phone(),
                    "Não encontramos faturas em aberto para o seu cadastro. ✅\n\n" +
                    "Se precisar de ajuda, é só chamar!");
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
                    "📄 *Segunda Via de Boleto*\n\n" +
                    "Vencimento: " + fatura.dataVencimento() + "\n" +
                    "Linha digitável:\n`" + fatura.linhaDigitavel() + "`");
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
            whatsAppService.enviarTexto(ctx.phone(),
                    "Ocorreu um erro ao consultar o cadastro. Tente novamente.");
            return;
        }

        if (clienteResponse.clientes() == null || clienteResponse.clientes().isEmpty()) {
            whatsAppService.enviarTexto(ctx.phone(), "Cadastro não encontrado para desbloqueio.");
            return;
        }

        HubsoftClienteItem cliente = clienteResponse.clientes().get(0);

        if (cliente.servicos() == null || cliente.servicos().isEmpty()) {
            whatsAppService.enviarTexto(ctx.phone(),
                    "Nenhum serviço ativo encontrado para realizar o desbloqueio.");
            return;
        }

        long idServico = cliente.servicos().get(0).id();

        HubsoftDesbloqueioResponse resultado;
        try {
            resultado = hubsoftService.desbloquear(idServico);
        } catch (Exception e) {
            log.error("Erro ao desbloquear serviço {}", idServico, e);
            whatsAppService.enviarTexto(ctx.phone(),
                    "Ocorreu um erro ao realizar o desbloqueio. Tente novamente.");
            return;
        }

        if ("success".equalsIgnoreCase(resultado.status())) {
            whatsAppService.enviarTexto(ctx.phone(),
                    "✅ *Desbloqueio realizado com sucesso!*\n\n" +
                    "Sua conexão será restabelecida em breve. 🌐");
        } else {
            whatsAppService.enviarTexto(ctx.phone(),
                    "❌ Não foi possível realizar o desbloqueio.\n\n" +
                    "Isso pode ocorrer se já houve um desbloqueio recente (limite: 1x a cada 25 dias).\n\n" +
                    "Se precisar de ajuda, entre em contato com nosso suporte técnico.");
        }
    }
}
