package com.ciudaddeportiva.api.repository;

import com.ciudaddeportiva.api.model.EstadoPartido;
import com.ciudaddeportiva.api.model.Partido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface PartidoRepository extends JpaRepository<Partido, Long> {

    // Para detectar un partido exactamente en la misma fecha y hora
    List<Partido> findByFechaAndHora(LocalDate fecha, LocalTime hora);

    // Para ver todos los partidos del día y comprobar solapamientos
    List<Partido> findByFecha(LocalDate fecha);

    // Devuelve todos los partidos que creó un usuario dado
    List<Partido> findByCreadoPor_Id(Long userId);

    /** usado por el scheduler */
    List<Partido> findByEstado(EstadoPartido estado);

    // PartidoRepository.java
    List<Partido> findByFechaAndCampoIgnoreCase(LocalDate fecha, String campo);


}
