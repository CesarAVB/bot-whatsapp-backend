package br.com.sistema.bot.service;

import br.com.sistema.bot.client.ChatwootClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatwootService {

    private final ChatwootClient chatwootClient;

    // ====================================================
    // transferir - Cria contato + conversa + nota + atribui equipe no Chatwoot
    // Chamado apenas quando o cliente solicita atendimento humano
    // Retorna o ID da conversa criada (-1 em caso de falha)
    // ====================================================
    public long transferir(String phone, String nome, String notaResumo, int teamId) {
        try {
            Long contactId = chatwootClient.criarContato(phone, nome);
            if (contactId == null) {
                log.error("Falha ao criar contato no Chatwoot para {}", phone);
                return -1L;
            }

            Long conversationId = chatwootClient.criarConversa(contactId);
            if (conversationId == null) {
                log.error("Falha ao criar conversa no Chatwoot para contato {}", contactId);
                return -1L;
            }

            if (notaResumo != null && !notaResumo.isBlank()) {
                chatwootClient.adicionarNota(conversationId, notaResumo);
            }

            chatwootClient.transferirParaEquipe(conversationId, teamId);

            log.info("Transferência realizada. Conversa Chatwoot: {} | Equipe: {} | Cliente: {}",
                    conversationId, teamId, phone);

            return conversationId;
        } catch (Exception e) {
            log.error("Erro ao realizar transferência no Chatwoot para {} equipe {}", phone, teamId, e);
            return -1L;
        }
    }
}
