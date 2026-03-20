package br.com.sistema.bot.client;

import br.com.sistema.bot.config.ChatwootProperties;
import br.com.sistema.bot.dtos.response.ChatwootContactResponse;
import br.com.sistema.bot.dtos.response.ChatwootConversationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatwootClient {

    private final ChatwootProperties props;
    private final RestClient.Builder restClientBuilder;

    // ====================================================
    // criarContato - Cria ou atualiza contato no Chatwoot com telefone e nome
    // ====================================================
    public Long criarContato(String phone, String nome) {
        log.debug("Criando contato no Chatwoot: {} ({})", nome, phone);

        ChatwootContactResponse response = restClientBuilder.build()
                .post()
                .uri(props.getBaseUrl() + "/contacts")
                .header("api_access_token", props.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "name", nome != null ? nome : phone,
                        "phone_number", phone
                ))
                .retrieve()
                .body(ChatwootContactResponse.class);

        return response != null ? response.id() : null;
    }

    // ====================================================
    // criarConversa - Abre nova conversa no inbox configurado para o contato
    // ====================================================
    public Long criarConversa(Long contactId) {
        log.debug("Criando conversa no Chatwoot para contato {}", contactId);

        ChatwootConversationResponse response = restClientBuilder.build()
                .post()
                .uri(props.getBaseUrl() + "/conversations")
                .header("api_access_token", props.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "inbox_id", props.getInboxId(),
                        "contact_id", contactId
                ))
                .retrieve()
                .body(ChatwootConversationResponse.class);

        return response != null ? response.id() : null;
    }

    // ====================================================
    // adicionarNota - Adiciona nota privada na conversa com resumo do atendimento do bot
    // ====================================================
    public void adicionarNota(long conversationId, String nota) {
        restClientBuilder.build()
                .post()
                .uri(props.getBaseUrl() + "/conversations/" + conversationId + "/messages")
                .header("api_access_token", props.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "content", nota,
                        "message_type", "outgoing",
                        "private", true
                ))
                .retrieve()
                .toBodilessEntity();
    }

    // ====================================================
    // transferirParaEquipe - Atribui a conversa a uma equipe de atendimento humano
    // ====================================================
    public void transferirParaEquipe(long conversationId, int teamId) {
        log.info("Transferindo conversa {} para equipe {}", conversationId, teamId);

        restClientBuilder.build()
                .post()
                .uri(props.getBaseUrl() + "/conversations/" + conversationId + "/assignments")
                .header("api_access_token", props.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("team_id", teamId))
                .retrieve()
                .toBodilessEntity();
    }
}
