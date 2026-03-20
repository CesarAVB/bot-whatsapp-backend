package br.com.sistema.bot.service;

import br.com.sistema.bot.entity.ConversationState;
import br.com.sistema.bot.enums.BotState;
import br.com.sistema.bot.repository.ConversationStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ConversationStateRepository conversationStateRepository;

    public Map<BotState, Long> buscarContagemPorEstado() {
        List<ConversationState> todas = conversationStateRepository.findAll();

        Map<BotState, Long> contagem = todas.stream()
                .collect(Collectors.groupingBy(ConversationState::getCurrentState, Collectors.counting()));

        // Garante que todos os estados aparecem no resultado, mesmo com zero
        Arrays.stream(BotState.values()).forEach(state -> contagem.putIfAbsent(state, 0L));

        return contagem;
    }

    public long buscarTotal() {
        return conversationStateRepository.count();
    }
}
