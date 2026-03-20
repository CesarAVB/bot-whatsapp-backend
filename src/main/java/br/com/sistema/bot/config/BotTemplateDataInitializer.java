package br.com.sistema.bot.config;

import br.com.sistema.bot.service.BotTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Popula a tabela bot_template com os textos padrão na primeira inicialização.
 * Só insere se a chave ainda não existir — nunca sobrescreve alterações feitas pelo painel.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BotTemplateDataInitializer implements ApplicationRunner {

    private final BotTemplateService templateService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Inicializando templates do bot...");

        // ── MenuInicialHandler ──────────────────────────────────────────────
        templateService.salvarSeNaoExistir(
                "menu.inicial.boas_vindas",
                "Olá! Seja bem-vindo(a) à *ASB Telecom*! 🌐\n\nComo podemos ajudar você hoje?\n\n1️⃣ Sou cliente ASB\n2️⃣ Quero me tornar cliente\n\nDigite o número da opção desejada:",
                "Mensagem de boas-vindas e menu principal"
        );

        templateService.salvarSeNaoExistir(
                "menu.inicial.opcoes_cliente",
                "Ótimo! Como posso ajudar?\n\n1️⃣ Assistência Técnica\n2️⃣ Financeiro\n3️⃣ Dúvidas/Sugestões\n4️⃣ Cancelamento\n5️⃣ Encerrar atendimento\n\nDigite o número da opção desejada:",
                "Submenu do cliente cadastrado"
        );

        templateService.salvarSeNaoExistir(
                "menu.inicial.comercial_transfer",
                "Aguarde! Vamos conectar você com nossa equipe comercial. Em breve um atendente irá lhe chamar! 😊",
                "Mensagem de transferência para equipe comercial (opção 2 - não cliente)"
        );

        // ── SouClienteHandler ───────────────────────────────────────────────
        templateService.salvarSeNaoExistir(
                "sou_cliente.menu_financeiro",
                "💰 *Menu Financeiro*\n\n1️⃣ Segunda via de boleto\n2️⃣ Desbloqueio de confiança\n3️⃣ Enviar comprovante de pagamento\n4️⃣ Falar com atendente financeiro\n5️⃣ Encerrar atendimento\n\nDigite o número da opção desejada:",
                "Menu financeiro do cliente"
        );

        templateService.salvarSeNaoExistir(
                "sou_cliente.transfer",
                "Transferindo para a equipe de *{nomeEquipe}*. Em breve um atendente irá lhe atender! 😊",
                "Mensagem de transferência com nome da equipe. Placeholder: {nomeEquipe}"
        );

        templateService.salvarSeNaoExistir(
                "sou_cliente.fora_horario",
                "Nossa equipe de *{nomeEquipe}* atende {horarios}.\nNo momento estamos fora do horário. Retorne dentro do horário de atendimento. 🙏",
                "Mensagem de fora do horário. Placeholders: {nomeEquipe}, {horarios}"
        );

        // ── FinanceiroHandler ───────────────────────────────────────────────
        templateService.salvarSeNaoExistir(
                "financeiro.solicitar_cpf",
                "Por favor, informe seu *CPF* ou *CNPJ* (somente números):",
                "Solicita CPF ou CNPJ para consulta financeira"
        );

        templateService.salvarSeNaoExistir(
                "financeiro.comprovante",
                "Para enviar seu comprovante, basta *anexar a imagem ou PDF* nesta conversa.\n\nNossa equipe irá verificar e dar retorno em breve. ✅",
                "Instrução para envio de comprovante de pagamento"
        );

        templateService.salvarSeNaoExistir(
                "financeiro.transfer",
                "Transferindo para a equipe *Financeira*. Em breve um atendente irá lhe atender! 😊",
                "Transferência para equipe financeira"
        );

        templateService.salvarSeNaoExistir(
                "financeiro.fora_horario",
                "Nossa equipe financeira atende *segunda a sábado, das 09h às 18h*.\nNo momento estamos fora do horário. Por favor, retorne dentro do horário. 🙏",
                "Fora do horário financeiro"
        );

        templateService.salvarSeNaoExistir(
                "financeiro.opcao_invalida",
                "Opção inválida. Por favor, escolha entre 1 e 5:",
                "Opção inválida no menu financeiro"
        );

        // ── ProcessaCpfCnpjHandler ──────────────────────────────────────────
        templateService.salvarSeNaoExistir(
                "cpf.invalido",
                "CPF/CNPJ inválido. ❌\n\nInforme apenas números:\n• CPF: 11 dígitos\n• CNPJ: 14 dígitos",
                "CPF ou CNPJ com formato inválido"
        );

        templateService.salvarSeNaoExistir(
                "cpf.erro_consulta",
                "Ocorreu um erro ao consultar o cadastro. Tente novamente em instantes.",
                "Erro na consulta ao Hubsoft"
        );

        templateService.salvarSeNaoExistir(
                "cpf.nao_encontrado",
                "Não encontramos cadastro com esse CPF/CNPJ. ❌\n\nVerifique e tente novamente:",
                "CPF/CNPJ não encontrado no Hubsoft"
        );

        templateService.salvarSeNaoExistir(
                "cpf.confirma_titular",
                "Encontramos o cadastro: *{nome}*\n\nVocê é o titular desta conta?\n\n1️⃣ Sim\n2️⃣ Não",
                "Confirma se o usuário é o titular. Placeholder: {nome}"
        );

        // ── ConfirmaCpfHandler ──────────────────────────────────────────────
        templateService.salvarSeNaoExistir(
                "confirma.nao_titular",
                "Entendido! Verifique seus dados e tente novamente, ou entre em contato com o suporte.",
                "Resposta quando o cliente informa que não é o titular"
        );

        templateService.salvarSeNaoExistir(
                "confirma.opcao_invalida",
                "Por favor, responda:\n1️⃣ Sim\n2️⃣ Não",
                "Opção inválida na confirmação de identidade"
        );

        templateService.salvarSeNaoExistir(
                "confirma.erro",
                "Ocorreu um erro. Por favor, inicie o processo novamente.",
                "Erro genérico no fluxo de confirmação"
        );

        templateService.salvarSeNaoExistir(
                "confirma.sem_fatura",
                "Não encontramos faturas em aberto para o seu cadastro. ✅\n\nSe precisar de ajuda, é só chamar!",
                "Nenhuma fatura em aberto encontrada"
        );

        templateService.salvarSeNaoExistir(
                "confirma.fatura",
                "📄 *Segunda Via de Boleto*\n\nVencimento: {data}\nLinha digitável:\n`{linha}`",
                "Segunda via de boleto. Placeholders: {data}, {linha}"
        );

        templateService.salvarSeNaoExistir(
                "confirma.desbloqueio_sucesso",
                "✅ *Desbloqueio realizado com sucesso!*\n\nSua conexão será restabelecida em breve. 🌐",
                "Desbloqueio de confiança realizado com sucesso"
        );

        templateService.salvarSeNaoExistir(
                "confirma.desbloqueio_falha",
                "❌ Não foi possível realizar o desbloqueio.\n\nIsso pode ocorrer se já houve um desbloqueio recente (limite: 1x a cada 25 dias).\n\nSe precisar de ajuda, entre em contato com nosso suporte técnico.",
                "Falha no desbloqueio de confiança"
        );

        templateService.salvarSeNaoExistir(
                "confirma.sem_servico",
                "Nenhum serviço ativo encontrado para realizar o desbloqueio.",
                "Nenhum serviço ativo para desbloqueio"
        );

        templateService.salvarSeNaoExistir(
                "confirma.sem_cadastro_desbloqueio",
                "Cadastro não encontrado para desbloqueio.",
                "Cadastro não encontrado ao tentar desbloqueio"
        );

        // ── EncerrarHandler ─────────────────────────────────────────────────
        templateService.salvarSeNaoExistir(
                "encerrar.despedida",
                "Obrigado por entrar em contato com a *ASB Telecom*! 😊\n\nSeu atendimento foi encerrado. Se precisar de ajuda novamente, é só nos chamar!\n\nTenha um ótimo dia! 🌟",
                "Mensagem de encerramento do atendimento"
        );

        log.info("Templates inicializados com sucesso.");
    }
}
