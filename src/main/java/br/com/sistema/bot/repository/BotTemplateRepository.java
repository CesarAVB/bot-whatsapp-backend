package br.com.sistema.bot.repository;

import br.com.sistema.bot.entity.BotTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BotTemplateRepository extends JpaRepository<BotTemplate, UUID> {

    Optional<BotTemplate> findByChave(String chave);

    boolean existsByChave(String chave);
}
