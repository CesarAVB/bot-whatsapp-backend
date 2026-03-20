package br.com.sistema.bot.repository;

import br.com.sistema.bot.entity.FluxoConexao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FluxoConexaoRepository extends JpaRepository<FluxoConexao, UUID> {

    List<FluxoConexao> findByDeNodeIdOrderByOrdem(UUID deNodeId);
}
