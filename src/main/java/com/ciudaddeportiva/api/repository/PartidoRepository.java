package com.ciudaddeportiva.api.repository;

import com.ciudaddeportiva.api.estado.EstadoPartido;
import com.ciudaddeportiva.api.model.Partido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

//JpaRepository gestiona reservas. FILTRA Y COMPRUEBA SOLAPAMIENTOS

public interface PartidoRepository extends JpaRepository<Partido, Long> {

    //ver los partidos del día y comprobar solapamientos
    List<Partido> findByFecha(LocalDate fecha);

    //todos los partidos creados x usuario
    List<Partido> findByCreadoPor_Id(Long userId);

    // usando el scheduler mira el estado
    List<Partido> findByEstado(EstadoPartido estado);

    //encuentra PartidosPorFechaYCampo
    List<Partido> findByFechaAndCampoIgnoreCase(LocalDate fecha, String campo);

    //cuenta los partidos creados por ese mnister
    int countByCreadoPor_Id(Long id);


}
