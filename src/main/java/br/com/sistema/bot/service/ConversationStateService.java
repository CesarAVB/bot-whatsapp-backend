package br.com.sistema.bot.service;

import br.com.sistema.bot.entity.ConversationState;
import br.com.sistema.bot.repository.ConversationStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationStateService {

    private final ConversationStateRepository repository;

    // ====================================================
    // buscarOuCriar - Retorna o estado da conversa ou cria um novo em menu_inicial
    // ====================================================
    @Transactional
    public ConversationState buscarOuCriar(String phone) {
        return repository.findByWhatsappPhone(phone)
                .orElseGet(() -> {
                    log.info("Novo contato: {}. Criando estado inicial.", phone);
                    ConversationState state = ConversationState.builder()
                            .whatsappPhone(phone)
                            .currentNodeKey("menu_inicial")
                            .build();
                    return repository.save(state);
                });
    }

    // ====================================================
    // setNodeKey - Transita para novo nó e limpa contextData
    // ====================================================
    @Transactional
    public void setNodeKey(String phone, String nodeKey) {
        ConversationState state = buscarOuCriar(phone);
        state.setCurrentNodeKey(nodeKey);
        state.setContextData(null);
        repository.save(state);
        log.debug("Conversa {} → nó '{}'", phone, nodeKey);
    }

    // ====================================================
    // setNodeKeyComContexto - Transita para novo nó e persiste dado temporário
    // ====================================================
    @Transactional
    public void setNodeKeyComContexto(String phone, String nodeKey, String contextData) {
        ConversationState state = buscarOuCriar(phone);
        state.setCurrentNodeKey(nodeKey);
        state.setContextData(contextData);
        repository.save(state);
        log.debug("Conversa {} → nó '{}' (contextData definido)", phone, nodeKey);
    }

    // ====================================================
    // marcarTransferido - Bot para de processar mensagens deste número
    // ====================================================
    @Transactional
    public void marcarTransferido(String phone) {
        ConversationState state = buscarOuCriar(phone);
        state.setTransferidoParaHumano(true);
        repository.save(state);
        log.info("Conversa {} marcada como transferida para humano.", phone);
    }

    // ====================================================
    // setChatwootConversationId - Guarda o ID da conversa criada no Chatwoot
    // ====================================================
    @Transactional
    public void setChatwootConversationId(String phone, long conversationId) {
        repository.findByWhatsappPhone(phone).ifPresent(state -> {
            state.setChatwootConversationId(conversationId);
            repository.save(state);
        });
    }

    // ====================================================
    // resetar - Volta ao nó inicial (menu_inicial)
    // ====================================================
    @Transactional
    public void resetar(String phone) {
        repository.findByWhatsappPhone(phone).ifPresent(state -> {
            state.setCurrentNodeKey("menu_inicial");
            state.setContextData(null);
            state.setChatwootConversationId(null);
            state.setTransferidoParaHumano(false);
            repository.save(state);
        });
    }
}
