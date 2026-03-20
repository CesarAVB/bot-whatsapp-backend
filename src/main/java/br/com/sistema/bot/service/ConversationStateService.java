package br.com.sistema.bot.service;

import br.com.sistema.bot.entity.ConversationState;
import br.com.sistema.bot.enums.BotState;
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
    // buscarOuCriar - Retorna o estado da conversa ou cria um novo em MENU_INICIAL
    // ====================================================
    @Transactional
    public ConversationState buscarOuCriar(String phone) {
        return repository.findByWhatsappPhone(phone)
                .orElseGet(() -> {
                    log.info("Novo contato: {}. Criando estado inicial.", phone);
                    ConversationState state = ConversationState.builder()
                            .whatsappPhone(phone)
                            .currentState(BotState.MENU_INICIAL)
                            .build();
                    return repository.save(state);
                });
    }

    // ====================================================
    // setState - Transita para novo estado e limpa contextData
    // ====================================================
    @Transactional
    public void setState(String phone, BotState novoEstado) {
        ConversationState state = buscarOuCriar(phone);
        state.setCurrentState(novoEstado);
        state.setContextData(null);
        repository.save(state);
        log.debug("Estado da conversa {} → {}", phone, novoEstado);
    }

    // ====================================================
    // setStateComContexto - Transita para novo estado e persiste dado temporário
    // Usado para guardar o CPF/CNPJ entre mensagens sem depender do histórico do Chatwoot
    // ====================================================
    @Transactional
    public void setStateComContexto(String phone, BotState novoEstado, String contextData) {
        ConversationState state = buscarOuCriar(phone);
        state.setCurrentState(novoEstado);
        state.setContextData(contextData);
        repository.save(state);
        log.debug("Estado da conversa {} → {} (contextData definido)", phone, novoEstado);
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
    // resetar - Volta ao estado inicial (usado após encerramento)
    // ====================================================
    @Transactional
    public void resetar(String phone) {
        repository.findByWhatsappPhone(phone).ifPresent(state -> {
            state.setCurrentState(BotState.MENU_INICIAL);
            state.setContextData(null);
            state.setChatwootConversationId(null);
            repository.save(state);
        });
    }
}
