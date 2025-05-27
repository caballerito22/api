package com.ciudaddeportiva.api.repository;

import com.ciudaddeportiva.api.model.Convocatoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

//JpaRepository que gestiona jugadores convocados por reserva, y ver convocados por partido

@Repository
public interface ConvocatoriaRepository extends JpaRepository<Convocatoria, Long> {
    List<Convocatoria> findByPartidoId(Long partidoId);
    List<Convocatoria> findByJugadorId(Long jugadorId);
    boolean existsByPartidoIdAndJugadorId(Long partidoId, Long jugadorId);
    // ID jugadores ya ocupados en el intervalo -INTENTO-
    @Query("""
           select distinct c.jugador.id
           from Convocatoria c
           where c.partido.fecha = :fecha
             and c.partido.hora   = :hora
           """)
    List<Long> findJugadoresOcupados(
            @Param("fecha") LocalDate fecha,
            @Param("hora")  LocalTime hora);
}
