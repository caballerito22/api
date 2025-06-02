package com.ciudaddeportiva.api.service;

import com.ciudaddeportiva.api.model.Convocatoria;
import com.ciudaddeportiva.api.estado.EstadoPartido;
import com.ciudaddeportiva.api.model.Partido;
import com.ciudaddeportiva.api.model.Usuario;
import com.ciudaddeportiva.api.repository.ConvocatoriaRepository;
import com.ciudaddeportiva.api.repository.PartidoRepository;
import com.ciudaddeportiva.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

//lógica crear,consultar,validar reservas. validación de solapamientos...

@Service
public class PartidoService {

    @Autowired private PartidoRepository       partidoRepo;
    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private ConvocatoriaRepository convocatoriaRepo;
    @Autowired private NotificacionService     notificacionService;

    //descanos entre reservas para recojer
    private static final int BUFFER = 15;

    //crea el partido con los convocados y la noti
    public Partido crearPartido(LocalDate fecha,
                                LocalTime hora,
                                String campo,
                                String equipoLocal,
                                String equipoVisitante,
                                Usuario creadoPor,
                                String tipoReserva,
                                List<Long> idsConvocados) {


        //val. fecha y hora
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();
        if (fecha.isBefore(hoy) || (fecha.isEqual(hoy) && hora.isBefore(ahora))) {
            throw new RuntimeException("No puedes crear reservas en el pasado");
        }

        //horario de la cd
        DayOfWeek dia = fecha.getDayOfWeek();
        boolean esFinDeSemana = (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY);
        LocalTime apertura = esFinDeSemana ? LocalTime.of(9, 0) : LocalTime.of(16, 0);
        LocalTime cierre = LocalTime.of(21, 0);

        int duracion = tipoReserva.equalsIgnoreCase("entrenamiento") ? 90 : getDuracionPartido(campo);
        int duracionConBuffer = duracion + BUFFER;
        LocalTime ultimaHoraInicio = cierre.minusMinutes(duracionConBuffer);

        //no entrene fds
        if (esFinDeSemana && tipoReserva.equalsIgnoreCase("entrenamiento")) {
            throw new RuntimeException("No se permiten entrenamientos en fin de semana (Sáb/Dom)");
        }
        //out horario
        if (hora.isBefore(apertura) || hora.isAfter(ultimaHoraInicio)) {
            throw new RuntimeException("Hora inválida. Debe estar entre " + apertura + " y " + ultimaHoraInicio);
        }

        //si se solapa...
        if (haySolapamiento(fecha, hora, campo, tipoReserva)) {
            throw new RuntimeException("Ya existe una reserva que se solapa con ese horario y campo");
        }

        //se guarda la reserva
        Partido p = new Partido();
        p.setFecha(fecha); p.setHora(hora); p.setCampo(campo);
        p.setEquipoLocal(equipoLocal); p.setEquipoVisitante(equipoVisitante);
        p.setEstado(EstadoPartido.pendiente); p.setCreadoPor(creadoPor);
        p.setTipoReserva(tipoReserva);
        Partido guardado = partidoRepo.save(p);

        //para la convocatoria
        if (idsConvocados != null && !idsConvocados.isEmpty()) {
            List<Convocatoria> lot = new ArrayList<>();
            for (Long idJugador : idsConvocados) {
                Usuario jugador = usuarioRepo.findById(idJugador)
                        //nunca aparece si all va bien
                        .orElseThrow(() -> new RuntimeException("No hay jugador"));

                //se añade toodo y se guarda
                Convocatoria c = new Convocatoria();
                c.setPartido(guardado);
                c.setJugador(jugador);
                c.setFechaConvocatoria(LocalDateTime.now());
                lot.add(c);
            }
            convocatoriaRepo.saveAll(lot);
        }

        //se encarga de notificar
        String titulo = "Nuevo " + tipoReserva;
        String mensaje = equipoLocal + " vs " + equipoVisitante + " • " + fecha + " " + hora;
        try {
            notificacionService.enviarNotificacion(titulo, mensaje, null);
            System.out.println("=== DEBUG === Notificación enviada con éxito");
        } catch (Exception e) {
            System.out.println("=== ERROR  === Falló el envío de la notificación: " + e.getMessage());
            e.printStackTrace();
        }

        return guardado;
    }

    //cambia estado reserva, se notif, y se cambia el balance automatic.
    public void cambiarEstado(Long id, EstadoPartido nuevo) {
        Partido p = partidoRepo.findById(id).orElseThrow(() -> new RuntimeException("Partido no encontrado"));
        p.setEstado(nuevo);
        partidoRepo.save(p);

        String titulo;
        String msg;
        switch (nuevo) {
            case jugado:
                titulo = "Partido jugado";
                msg = "Partido del " + p.getFecha() + " a las " + p.getHora() + " jugado.";
                break;
            case cancelado:
                titulo = "Partido cancelado";
                msg = "Cancrelado el partido " + p.getEquipoLocal() + " vs " + p.getEquipoVisitante();
                break;
            default:
                return;
        }

        try {
            notificacionService.enviarNotificacion(titulo, msg, null);
            System.out.println("=== DEBUG === Notificación de cambio de estado enviada");
        } catch (Exception e) {
            System.out.println("=== ERROR  === Falló la notificación de cambio de estado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //mira si se solapa (ya hay otra)
    public boolean haySolapamiento(LocalDate fecha, LocalTime horaInicioNuevo, String campoNuevo, String tipoReserva) {
        int duracionNuevo = tipoReserva.equalsIgnoreCase("entrenamiento") ? 90 : getDuracionPartido(campoNuevo);
        LocalTime horaFinNuevo = horaInicioNuevo.plusMinutes(duracionNuevo);
        LocalTime horaFinNuevoBuffer = horaFinNuevo.plusMinutes(BUFFER);

        for (Partido existente : partidoRepo.findByFecha(fecha)) {
            if (!camposEnConflicto(campoNuevo, existente.getCampo())) continue;

            int duracionExist = existente.getTipoReserva().equalsIgnoreCase("entrenamiento") ? 90 : getDuracionPartido(existente.getCampo());
            LocalTime iniExist = existente.getHora();
            LocalTime finExist = iniExist.plusMinutes(duracionExist);
            LocalTime finExistBuf = finExist.plusMinutes(BUFFER);

            boolean solapan = horaInicioNuevo.isBefore(finExist) && horaFinNuevo.isAfter(iniExist);
            boolean sinMargenDespues = horaFinNuevoBuffer.isAfter(iniExist) && horaFinNuevo.isBefore(iniExist);
            boolean sinMargenAntes = !horaInicioNuevo.isBefore(finExist) && horaInicioNuevo.isBefore(finExistBuf);
            if (solapan || sinMargenDespues || sinMargenAntes) return true;
        }
        return false;
    }

    //lo que duran los partidos
    private int getDuracionPartido(String campo) {
        campo = campo.toLowerCase();
        if (campo.contains("f11")) return 120; //2h
        if (campo.contains("f8")) return 80;  //1h20
        return 90; //entrenamiento
    }

    //si el nuevo ya existe..
    private boolean camposEnConflicto(String nuevo, String existente) {
        nuevo = nuevo.toLowerCase().trim();
        existente = existente.toLowerCase().trim();
        if (nuevo.equals("f11 - campo abajo") && existente.equals("f11 - campo abajo")) return true;
        if (nuevo.equals("f8 - campo arriba a") && existente.equals("f8 - campo arriba a")) return true;
        if (nuevo.equals("f8 - campo arriba b") && existente.equals("f8 - campo arriba b")) return true;
        if (nuevo.equals("f11 - campo arriba") || existente.equals("f11 - campo arriba")) {
            return nuevo.contains("campo arriba") && existente.contains("campo arriba");
        }
        return false;
    }

    //los partidos creado por x mistre...
    public List<Partido> getPartidosByUsuario(Long userId) {
        return partidoRepo.findByCreadoPor_Id(userId);
    }

    //solo todos los partidos
    public List<Partido> getAllPartidos() {
        return partidoRepo.findAll();
    }

    //solo todos los partidos con la fecha
    public List<Partido> getPartidosPorFecha(LocalDate fecha) {
        return partidoRepo.findByFecha(fecha);
    }

    //todos las reservas por fecha y campo
    public List<Partido> getPartidosPorFechaYCampo(LocalDate fecha, String campo) {
        return partidoRepo.findByFechaAndCampoIgnoreCase(fecha, campo);
    }
    //hay 3 metod. diferentes para obtener reservas,porque dependiendo el caso se usa uno u otro

}
