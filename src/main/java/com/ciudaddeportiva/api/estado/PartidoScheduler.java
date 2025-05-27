package com.ciudaddeportiva.api.estado;

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

    //esta clse se encarga de marcar el partido como jugado si asi ha sido
    @Scheduled(fixedRate = 300_000)
    public void cerrarPartidosPendientes() {

        LocalDate hoy   = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        //se usa la clase enum
        List<Partido> pendientes = repo.findByEstado(EstadoPartido.pendiente);

        //para los partidos pendientes, si se han jugado se cambia
        for (Partido p : pendientes) {

            int duracion = p.getTipoReserva().equalsIgnoreCase("entrenamiento")
                    ? 90
                    : (p.getCampo().toLowerCase().contains("f11") ? 120 : 80);

            LocalTime fin = p.getHora().plusMinutes(duracion);

            if (p.getFecha().isBefore(hoy) ||
                    (p.getFecha().isEqual(hoy) && fin.isBefore(ahora))) {

                p.setEstado(EstadoPartido.jugado);
                repo.save(p);
            }
        }
    }
}
