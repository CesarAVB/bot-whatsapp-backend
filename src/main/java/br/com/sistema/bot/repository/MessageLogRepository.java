package br.com.sistema.bot.repository;

import br.com.sistema.bot.entity.MessageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MessageLogRepository extends JpaRepository<MessageLog, UUID> {

    boolean existsByMessageId(String messageId);
}
