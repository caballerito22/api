package com.ciudaddeportiva.api.controller;

import com.ciudaddeportiva.api.model.*;
import com.ciudaddeportiva.api.service.PartidoService;
import com.ciudaddeportiva.api.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static com.ciudaddeportiva.api.model.Rol.entrenador;

@RestController
@RequestMapping("/api/partidos")
@CrossOrigin(origins = "*")
public class PartidoController {

    @Autowired private PartidoService partidoService;
    @Autowired private UsuarioService usuarioService;

    /* ---------- crear partido / entrenamiento ---------- */
    @PostMapping("/crear")
    public ResponseEntity<?> crearPartido(@RequestBody PartidoRequest req) {
        try {
            // 1) Parsear fecha y hora
            LocalDate fecha = LocalDate.parse(req.getFecha());   // "2025-05-23"
            LocalTime hora  = LocalTime.parse(req.getHora());    // "17:30"

            // 2) Recuperar el usuario creador
            Usuario creador = usuarioService.findById(req.getUsuarioId());

            // 3) Crear el partido (con posibles convocados)
            Partido partido = partidoService.crearPartido(
                    fecha,
                    hora,
                    req.getCampo(),
                    req.getEquipoLocal(),
                    req.getEquipoVisitante(),
                    creador,                       // <-- Usuario, no Rol
                    req.getTipoReserva(),
                    req.getConvocados()            // lista de Long con IDs de jugadores
            );

            // 4) Respuesta OK
            return ResponseEntity.ok(partido);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    /* ---------- mis partidos (entrenador / jugador) ---------- */
    @GetMapping("/mis/{userId}")
    public List<Partido> obtenerMisPartidos(@PathVariable Long userId) {
        return partidoService.getPartidosByUsuario(userId);
    }

    /* ---------- todos los partidos (solo admin) ---------- */
    @GetMapping("/todos")
    public ResponseEntity<?> todosPartidos(@RequestParam Long userId) {
        Usuario u = usuarioService.findById(userId);
        if (u.getRol() != Rol.admin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error", "Solo el admin puede ver todos los partidos"));
        }
        return ResponseEntity.ok(partidoService.getAllPartidos());
    }


    /* ---------- cambiar estado ---------- */
    @PatchMapping("/estado")
    public ResponseEntity<?> cambiarEstado(@RequestBody CambiarEstadoRequest r){
        partidoService.cambiarEstado(r.getPartidoId(),
                EstadoPartido.valueOf(r.getNuevoEstado()));
        return ResponseEntity.ok(Collections.singletonMap("message","Estado actualizado"));
    }

    // ✅ Nuevo método para jugadores: lista pública
    @GetMapping("/publicos")
    public List<Partido> partidosPublicos() {
        return partidoService.getAllPartidos();
    }

    /* ---------- Horarios ocupados para un día específico ---------- */
    @GetMapping("/ocupados")
    public ResponseEntity<?> obtenerHorariosOcupados(
            @RequestParam
            @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha) {

        try {
            System.out.println("✔ Fecha recibida: " + fecha);

            List<Partido> partidos = partidoService.getPartidosPorFecha(fecha);

            List<HorarioOcupadoResponse> horariosOcupados = partidos.stream()
                    .map(p -> {
                        var ini = p.getHora().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
                        int duracion =
                                p.getTipoReserva().equalsIgnoreCase("F11") ? 120 :
                                        p.getTipoReserva().equalsIgnoreCase("F8")  ?  80 : 90;
                        var fin = ini.plusMinutes(duracion);
                        return new HorarioOcupadoResponse(ini.toString(), fin.toString());
                    })
                    .toList();

            return ResponseEntity.ok(horariosOcupados);

        } catch (Exception e) {
            e.printStackTrace();   // ⬅  verás la excepción real en la consola
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error",
                            "Error procesando la petición: " + e.getMessage()));
        }
    }



}
