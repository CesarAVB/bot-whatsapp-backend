package br.com.sistema.bot.repository;

import br.com.sistema.bot.entity.ConversationState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationStateRepository extends JpaRepository<ConversationState, UUID> {

    Optional<ConversationState> findByWhatsappPhone(String whatsappPhone);
}
