package br.com.sistema.bot.service;

import br.com.sistema.bot.entity.ConversationState;
import br.com.sistema.bot.repository.ConversationStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ConversationStateRepository conversationStateRepository;

    /** Retorna contagem de conversas agrupadas por nodeKey atual. */
    public Map<String, Long> buscarContagemPorEstado() {
        List<ConversationState> todas = conversationStateRepository.findAll();
        return todas.stream()
                .collect(Collectors.groupingBy(ConversationState::getCurrentNodeKey, Collectors.counting()));
    }

    public long buscarTotal() {
        return conversationStateRepository.count();
    }
}
