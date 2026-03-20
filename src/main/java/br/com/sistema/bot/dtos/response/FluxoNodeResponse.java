package br.com.sistema.bot.dtos.response;

import br.com.sistema.bot.enums.TipoNode;

import java.util.List;

public record FluxoNodeResponse(
        /** Identificador único do nó no grafo (ex: "menu_inicial", "transfer_suporte"). */
        String id,
        /** Rótulo legível exibido no editor visual. */
        String label,
        /** Nome do BotState correspondente, ou null para nós virtuais. */
        String estado,
        /** Tipo visual do nó. */
        TipoNode tipo,
        /** Posição X sugerida para renderização no canvas. */
        int posX,
        /** Posição Y sugerida para renderização no canvas. */
        int posY,
        /** Mensagens (templates) enviadas neste nó, com texto atual do BD. */
        List<FluxoTemplateResponse> mensagens,
        /** Nome da equipe Chatwoot de destino (somente tipo TRANSFERENCIA). */
        String equipeTransferencia,
        /** Descrição do horário de atendimento (somente TRANSFERENCIA com restrição). */
        String horarioAtendimento
) {}
