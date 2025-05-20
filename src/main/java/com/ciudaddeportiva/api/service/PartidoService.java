package com.ciudaddeportiva.api.service;

import com.ciudaddeportiva.api.model.EstadoPartido;
import com.ciudaddeportiva.api.model.Partido;
import com.ciudaddeportiva.api.model.Usuario;
import com.ciudaddeportiva.api.repository.PartidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class PartidoService {

    @Autowired
    private PartidoRepository partidoRepository;

    @Autowired // 👉 inyectamos el servicio de notificaciones
    private NotificacionService notificacionService;

    // minutos de margen antes/después de cada reserva
    private static final int BUFFER = 15;

    /**
     * Crea un nuevo partido o entrenamiento y dispara la notificación correspondiente.
     */
    public Partido crearPartido(LocalDate fecha,
                                LocalTime hora,
                                String campo,
                                String equipoLocal,
                                String equipoVisitante,
                                Usuario creadoPor,
                                String tipoReserva) {

        System.out.println("=== DEBUG === Creando " + tipoReserva + " – " + equipoLocal + " vs " + equipoVisitante);

        /* 1️⃣ Validaciones de fecha y hora */
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();
        if (fecha.isBefore(hoy) || (fecha.isEqual(hoy) && hora.isBefore(ahora))) {
            throw new RuntimeException("No puedes crear reservas en el pasado");
        }

        DayOfWeek dia = fecha.getDayOfWeek();
        boolean esFinDeSemana = (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY);
        LocalTime apertura = esFinDeSemana ? LocalTime.of(9, 0) : LocalTime.of(16, 0);
        LocalTime cierre = LocalTime.of(21, 0);

        int duracion = tipoReserva.equalsIgnoreCase("entrenamiento") ? 90 : getDuracionPartido(campo);
        int duracionConBuffer = duracion + BUFFER;
        LocalTime ultimaHoraInicio = cierre.minusMinutes(duracionConBuffer);

        if (esFinDeSemana && tipoReserva.equalsIgnoreCase("entrenamiento")) {
            throw new RuntimeException("No se permiten entrenamientos en fin de semana (Sáb/Dom)");
        }
        if (hora.isBefore(apertura) || hora.isAfter(ultimaHoraInicio)) {
            throw new RuntimeException("Hora inválida. Debe estar entre " + apertura + " y " + ultimaHoraInicio);
        }

        /* 2️⃣ Solapamientos */
        if (haySolapamiento(fecha, hora, campo, tipoReserva)) {
            throw new RuntimeException("Ya existe una reserva que se solapa con ese horario y campo");
        }

        /* 3️⃣ Persistencia */
        Partido partido = new Partido();
        partido.setFecha(fecha);
        partido.setHora(hora);
        partido.setCampo(campo);
        partido.setEquipoLocal(equipoLocal);
        partido.setEquipoVisitante(equipoVisitante);
        partido.setEstado(EstadoPartido.pendiente);
        partido.setCreadoPor(creadoPor);
        partido.setTipoReserva(tipoReserva);
        Partido guardado = partidoRepository.save(partido);

        /* 4️⃣ Notificación */
        String titulo = "Nueva " + tipoReserva;
        String mensaje = equipoLocal + " vs " + equipoVisitante + " • " + fecha + " " + hora;
        try {
            System.out.println("=== DEBUG === Enviando notificación: " + titulo + " – " + mensaje);
            notificacionService.enviarNotificacion(titulo, mensaje, null);
            System.out.println("=== DEBUG === Notificación enviada con éxito");
        } catch (Exception e) {
            System.out.println("=== ERROR  === Falló el envío de la notificación: " + e.getMessage());
            e.printStackTrace();
        }

        return guardado;
    }

    /**
     * Cambia el estado de un partido y notifica a los implicados.
     */
    public void cambiarEstado(Long id, EstadoPartido nuevo) {
        Partido p = partidoRepository.findById(id).orElseThrow(() -> new RuntimeException("Partido no encontrado"));
        p.setEstado(nuevo);
        partidoRepository.save(p);

        String titulo;
        String msg;
        switch (nuevo) {
            case jugado:
                titulo = "Partido jugado";
                msg = "Tu partido del " + p.getFecha() + " a las " + p.getHora() + " ha sido marcado como jugado.";
                break;
            case cancelado:
                titulo = "Partido cancelado";
                msg = "Se canceló el partido " + p.getEquipoLocal() + " vs " + p.getEquipoVisitante();
                break;
            default:
                return; // otros estados no generan notificación
    }

        try {
        System.out.println("=== DEBUG === Enviando notificación de cambio de estado: " + titulo);
        notificacionService.enviarNotificacion(titulo, msg, null);
        System.out.println("=== DEBUG === Notificación de cambio de estado enviada");
    } catch (Exception e) {
        System.out.println("=== ERROR  === Falló la notificación de cambio de estado: " + e.getMessage());
        e.printStackTrace();
    }
}

/**
 * Comprueba si la nueva reserva se solapa con otras o no deja margen suficiente.
 */
public boolean haySolapamiento(LocalDate fecha, LocalTime horaInicioNuevo, String campoNuevo, String tipoReserva) {
    int duracionNuevo = tipoReserva.equalsIgnoreCase("entrenamiento") ? 90 : getDuracionPartido(campoNuevo);
    LocalTime horaFinNuevo = horaInicioNuevo.plusMinutes(duracionNuevo);
    LocalTime horaFinNuevoBuffer = horaFinNuevo.plusMinutes(BUFFER);

    for (Partido existente : partidoRepository.findByFecha(fecha)) {
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

    private int getDuracionPartido(String campo) {
        campo = campo.toLowerCase();
        if (campo.contains("f11")) return 120; // 2h
        if (campo.contains("f8")) return 80;  // 1h20
        return 90; // default
    }

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

    public List<Partido> getPartidosByUsuario(Long userId) {
        return partidoRepository.findByCreadoPor_Id(userId);
    }

    public List<Partido> getAllPartidos() {
        return partidoRepository.findAll();
    }

    public List<Partido> getPartidosPorFecha(LocalDate fecha) {
        return partidoRepository.findByFecha(fecha);
    }

}
