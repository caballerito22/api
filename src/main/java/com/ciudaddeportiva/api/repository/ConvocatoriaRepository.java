package com.ciudaddeportiva.api.repository;

import com.ciudaddeportiva.api.model.Convocatoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ConvocatoriaRepository extends JpaRepository<Convocatoria, Long> {
    List<Convocatoria> findByPartidoId(Long partidoId);
    List<Convocatoria> findByJugadorId(Long jugadorId);
    boolean existsByPartidoIdAndJugadorId(Long partidoId, Long jugadorId);
    @Query("""
       SELECT c.jugador.id
       FROM   Convocatoria c
       WHERE  c.partido.fecha = :fecha
       AND    c.partido.hora  = :hora
       """)
    List<Long> findJugadoresOcupados(
            @Param("fecha") LocalDate fecha,
            @Param("hora") LocalTime hora);
}
