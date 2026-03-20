package br.com.sistema.bot.engine.executor;

import br.com.sistema.bot.engine.FluxoEngine;
import br.com.sistema.bot.engine.FluxoExecucaoCtx;
import br.com.sistema.bot.engine.NodeExecutor;
import br.com.sistema.bot.entity.FluxoNode;
import br.com.sistema.bot.enums.TipoNode;
import br.com.sistema.bot.service.ConversationStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EncerramentoNodeExecutor implements NodeExecutor {

    private final ConversationStateService stateService;

    @Override
    public TipoNode getTipo() {
        return TipoNode.ENCERRAMENTO;
    }

    @Override
    public void executar(FluxoExecucaoCtx ctx, FluxoNode node, FluxoEngine engine) {
        engine.enviarMensagensDoNo(ctx, node);
        stateService.resetar(ctx.phone());
        log.info("Atendimento encerrado para {}", ctx.phone());
    }
}
