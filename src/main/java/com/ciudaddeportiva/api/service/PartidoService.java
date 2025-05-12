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

    @Autowired               // 👉 inyectamos el servicio de notificaciones
    private NotificacionService notificacionService;

    //minutos antes de crear algo después
    private static final  int BUFFER =15;

    public Partido crearPartido(LocalDate fecha,
                                LocalTime hora,
                                String campo,
                                String equipoLocal,
                                String equipoVisitante,
                                Usuario creadoPor,
                                String tipoReserva) {

        System.out.println("=== DEBUG === Creando partido tipo=" + tipoReserva);

        // 1) Verificar horario de apertura/cierre y hora máxima de inicio según tipo.
        //    - Lunes-Viernes: abre 16:00, cierra 21:00
        //    - Sábados-Domingos: abre 9:00, cierra 21:00
        //    - Añadir buffer de 15min al final, para que todo acabe antes de las 21:00

        //para que no pueda crear partidos en el pasado
        LocalDate hoy   = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        if (fecha.isBefore(hoy) || (fecha.isEqual(hoy) && hora.isBefore(ahora))) {
            throw new RuntimeException("No puedes crear reservas en el pasado");
        }

        DayOfWeek dia = fecha.getDayOfWeek();
        boolean esFinDeSemana = (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY);

        LocalTime apertura = esFinDeSemana ? LocalTime.of(9, 0) : LocalTime.of(16, 0);
        LocalTime cierre = LocalTime.of(21, 0);

        // Duración base (si es entrenamiento => 90, si es partido => segun campo)
        int duracion = tipoReserva.equalsIgnoreCase("entrenamiento")
                ? 90 // 1h30
                : getDuracionPartido(campo); // 80 o 120
        // Sumamos 15 min de margen al final
        int duracionConBuffer = duracion + 15;

        // Hora máxima de inicio = cierre - duracionConBuffer
        LocalTime ultimaHoraInicio = cierre.minusMinutes(duracionConBuffer);

        // Comprobamos si es fin de semana Y tipoReserva=entrenamiento => no se permite
        if (esFinDeSemana && tipoReserva.equalsIgnoreCase("entrenamiento")) {
            throw new RuntimeException("No se permiten entrenamientos en fin de semana (Sáb/Dom)");
        }

        // Verificar si la hora está dentro de [apertura, ultimaHoraInicio]
        if (hora.isBefore(apertura) || hora.isAfter(ultimaHoraInicio)) {
            throw new RuntimeException("No se puede iniciar un " + tipoReserva
                    + " a las " + hora + ". Horario válido: de "
                    + apertura + " a " + ultimaHoraInicio
                    + " (con buffer 15min), cierra a las 21:00.");
        }

        // 2) Revisar solapamientos
        if (haySolapamiento(fecha, hora, campo, tipoReserva)) {
            throw new RuntimeException("Ya existe un partido/entrenamiento que se solapa con ese horario/campo.");
        }

        // 3) Crear y guardar el partido
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

        /* 🚀 Notificación */
        try {
            String titulo   = "Nueva " + tipoReserva;
            String mensaje  = equipoLocal + " vs " + equipoVisitante +
                    " • " + fecha + " " + hora;
            notificacionService.enviarNotificacion(titulo, mensaje, null);
        } catch (Exception e) {
            // evita que un fallo en OneSignal rompa la creación
            e.printStackTrace();
        }

        return guardado;
    }




    /**
     * Comprueba si la nueva reserva se solapa o no deja margen suficiente.
     */
    public boolean haySolapamiento(LocalDate fecha,
                                   LocalTime horaInicioNuevo,
                                   String campoNuevo,
                                   String tipoReserva) {

        int duracionNuevo = tipoReserva.equalsIgnoreCase("entrenamiento")
                ? 90
                : getDuracionPartido(campoNuevo);

        LocalTime horaFinNuevo        = horaInicioNuevo.plusMinutes(duracionNuevo);
        LocalTime horaFinNuevoBuffer  = horaFinNuevo.plusMinutes(BUFFER);
        LocalTime horaInicioNuevoBuff = horaInicioNuevo.minusMinutes(BUFFER);

        for (Partido existente : partidoRepository.findByFecha(fecha)) {

            if (!camposEnConflicto(campoNuevo, existente.getCampo())) continue;

            int duracionExist = existente.getTipoReserva().equalsIgnoreCase("entrenamiento")
                    ? 90
                    : getDuracionPartido(existente.getCampo());

            LocalTime iniExist   = existente.getHora();
            LocalTime finExist   = iniExist.plusMinutes(duracionExist);
            LocalTime finExistBuf= finExist.plusMinutes(BUFFER);
            LocalTime iniExistBuf= iniExist.minusMinutes(BUFFER);

            /* 1️⃣  Solapamiento real */
            boolean solapan = horaInicioNuevo.isBefore(finExist) &&
                    horaFinNuevo.isAfter(iniExist);

            /* 2️⃣  Sin margen DESPUÉS del nuevo */
            boolean sinMargenDespues = horaFinNuevoBuffer.isAfter(iniExist) &&
                    horaFinNuevo.isBefore(iniExist);

            /* 3️⃣  Sin margen ANTES del nuevo */
            boolean sinMargenAntes =
                    !horaInicioNuevo.isBefore(finExist)      // “igual o después”
                            &&  horaInicioNuevo.isBefore(finExistBuf); // pero dentro del buffer

            if (solapan || sinMargenDespues || sinMargenAntes) return true;
        }
        return false;
    }



    private int getDuracionPartido(String campo) {
        // Si quieres buffer extra añade min. p.e. + 10
        campo = campo.toLowerCase();
        if (campo.contains("f11")) {
            return 120; // 2h
        } else if (campo.contains("f8")) {
            return 80;  // 1h20
        }
        return 90; // default
    }

    private boolean camposEnConflicto(String nuevo, String existente) {
        nuevo = nuevo.toLowerCase().trim();
        existente = existente.toLowerCase().trim();

        // Campo Abajo -> "f11 - campo abajo"
        if (nuevo.equalsIgnoreCase("f11 - campo abajo") && existente.equalsIgnoreCase("f11 - campo abajo")) {
            return true;
        }

        // F11 - Campo Arriba
        if (nuevo.equalsIgnoreCase("f11 - campo arriba") && existente.contains("campo arriba")) return true;
        if (existente.equalsIgnoreCase("f11 - campo arriba") && nuevo.contains("campo arriba")) return true;

        // F8 - Campo Arriba A
        if (nuevo.equalsIgnoreCase("f8 - campo arriba a") && existente.equalsIgnoreCase("f8 - campo arriba a")) {
            return true;
        }
        // F8 - Campo Arriba B
        if (nuevo.equalsIgnoreCase("f8 - campo arriba b") && existente.equalsIgnoreCase("f8 - campo arriba b")) {
            return true;
        }

        // F11 no convive con F8
        if (nuevo.equalsIgnoreCase("f11 - campo arriba") && (existente.equalsIgnoreCase("f8 - campo arriba a") || existente.equalsIgnoreCase("f8 - campo arriba b"))) {
            return true;
        }
        if (existente.equalsIgnoreCase("f11 - campo arriba") && (nuevo.equalsIgnoreCase("f8 - campo arriba a") || nuevo.equalsIgnoreCase("f8 - campo arriba b"))) {
            return true;
        }

        return false;
    }

    /**
     * Devuelve todos los partidos de un usuario.
     */
    public List<Partido> getPartidosByUsuario(int userId) {
        return partidoRepository.findByCreadoPor_Id(userId);
    }

    /**
     * Devuelve todos los partidos (para admin).
     */
    public List<Partido> getAllPartidos() {
        return partidoRepository.findAll();
    }

    public void cambiarEstado(int id, EstadoPartido nuevo) {
        Partido p = partidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Partido no encontrado"));

        p.setEstado(nuevo);
        partidoRepository.save(p);

        /* 🚀 Notificación según el nuevo estado */
        try {
            String titulo, msg;
            switch (nuevo) {
                case jugado:
                    titulo = "Partido jugado";
                    msg    = "Tu partido del " + p.getFecha() + " a las " + p.getHora() + " ha sido jugado.";
                    break;
                case cancelado:
                    titulo = "Partido cancelado";
                    msg    = "Se canceló el partido " + p.getEquipoLocal() + " vs " + p.getEquipoVisitante();
                    break;
                default:
                    return; // otros estados no notifican
            }
            notificacionService.enviarNotificacion(titulo, msg, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
