package com.ciudaddeportiva.api.estado;

import com.ciudaddeportiva.api.model.EstadoPartido;   //  ←  IMPORT CORRECTO
import com.ciudaddeportiva.api.model.Partido;
import com.ciudaddeportiva.api.repository.PartidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class PartidoScheduler {

    @Autowired
    private PartidoRepository repo;

    /**  Cada 5 min marca como «jugado» lo que ya terminó  */
    @Scheduled(fixedRate = 300_000)
    public void cerrarPartidosPendientes() {

        LocalDate hoy   = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        // --- usamos el enum, NO la DTO ---
        List<Partido> pendientes = repo.findByEstado(EstadoPartido.pendiente);

        for (Partido p : pendientes) {

            int duracion = p.getTipoReserva().equalsIgnoreCase("entrenamiento")
                    ? 90
                    : (p.getCampo().toLowerCase().contains("f11") ? 120 : 80);

            LocalTime fin = p.getHora().plusMinutes(duracion);

            if (p.getFecha().isBefore(hoy) ||
                    (p.getFecha().isEqual(hoy) && fin.isBefore(ahora))) {

                p.setEstado(EstadoPartido.jugado);   // ← enum
                repo.save(p);
            }
        }
    }
}
