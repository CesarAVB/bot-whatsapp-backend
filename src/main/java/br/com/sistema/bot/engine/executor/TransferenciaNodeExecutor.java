package br.com.sistema.bot.engine.executor;

import br.com.sistema.bot.engine.FluxoEngine;
import br.com.sistema.bot.engine.FluxoExecucaoCtx;
import br.com.sistema.bot.engine.NodeExecutor;
import br.com.sistema.bot.entity.FluxoConexao;
import br.com.sistema.bot.entity.FluxoNode;
import br.com.sistema.bot.enums.TeamId;
import br.com.sistema.bot.enums.TipoNode;
import br.com.sistema.bot.service.ChatwootService;
import br.com.sistema.bot.service.ConversationStateService;
import br.com.sistema.bot.service.HorarioAtendimentoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Handles TRANSFERENCIA nodes.
 * Verifica disponibilidade de horário, cria conversa no Chatwoot e transfere.
 * Em caso de fora do horário, segue a conexão "fora_horario".
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransferenciaNodeExecutor implements NodeExecutor {

    private final FluxoEngine engine;
    private final ChatwootService chatwootService;
    private final ConversationStateService stateService;
    private final HorarioAtendimentoService horarioAtendimentoService;

    @Override
    public TipoNode getTipo() {
        return TipoNode.TRANSFERENCIA;
    }

    @Override
    public void executar(FluxoExecucaoCtx ctx, FluxoNode node) {
        boolean disponivel = verificarDisponibilidade(node.getTipoHorario());

        if (!disponivel) {
            // Fora do horário: segue conexão "fora_horario" → volta ao menu_inicial
            FluxoConexao conexao = engine.encontrarConexao(node, "fora_horario");
            if (conexao != null) {
                engine.transicionarPara(ctx, conexao.getParaNode());
            }
            return;
        }

        // Transferência efetiva para humano
        TeamId teamId = resolverEquipe(node.getEquipeTransferencia());
        String nomeEquipe = node.getEquipeTransferencia() != null ? node.getEquipeTransferencia() : "Atendimento";

        String nota = "Cliente solicita: " + nomeEquipe;
        long chatwootId = chatwootService.transferir(ctx.phone(), ctx.senderName(), nota, teamId.getId());

        stateService.marcarTransferido(ctx.phone());
        if (chatwootId > 0) stateService.setChatwootConversationId(ctx.phone(), chatwootId);

        // Envia a mensagem de transferência do nó
        engine.enviarMensagensDoNo(ctx, node, Map.of("nomeEquipe", nomeEquipe));

        log.info("Conversa {} transferida para equipe {} (Chatwoot #{})", ctx.phone(), nomeEquipe, chatwootId);
    }

    private boolean verificarDisponibilidade(String tipoHorario) {
        if (tipoHorario == null) return true;
        return switch (tipoHorario) {
            case "SUPORTE"              -> horarioAtendimentoService.isSuporteTecnicoDisponivel();
            case "FINANCEIRO_COMERCIAL" -> horarioAtendimentoService.isFinanceiroComercialDisponivel();
            default -> true;
        };
    }

    private TeamId resolverEquipe(String equipeTransferencia) {
        if (equipeTransferencia == null) return TeamId.SUPORTE;
        return switch (equipeTransferencia) {
            case "FINANCEIRO"       -> TeamId.FINANCEIRO;
            case "SUPORTE"          -> TeamId.SUPORTE;
            case "DUVIDAS_COMERCIAL"-> TeamId.DUVIDAS_COMERCIAL;
            case "CANCELAMENTO"     -> TeamId.CANCELAMENTO;
            default -> TeamId.SUPORTE;
        };
    }
}
