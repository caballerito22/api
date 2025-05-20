package com.ciudaddeportiva.api.repository;

import com.ciudaddeportiva.api.model.Convocatoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConvocatoriaRepository extends JpaRepository<Convocatoria, Long> {
    List<Convocatoria> findByPartidoId(Long partidoId);
    List<Convocatoria> findByJugadorId(Long jugadorId);
    boolean existsByPartidoIdAndJugadorId(Long partidoId, Long jugadorId);
}
