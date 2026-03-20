package br.com.sistema.bot.repository;

import br.com.sistema.bot.entity.FluxoMensagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FluxoMensagemRepository extends JpaRepository<FluxoMensagem, UUID> {

    List<FluxoMensagem> findByNodeIdOrderByOrdem(UUID nodeId);
}
