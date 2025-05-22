package com.ciudaddeportiva.api.controller;

import com.ciudaddeportiva.api.model.*;
import com.ciudaddeportiva.api.repository.PartidoRepository;
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
    @Autowired private PartidoRepository partidoRepository;


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
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha,
            @RequestParam(required = false) String campo) {          // ← nuevo parámetro opcional

        try {
            System.out.println("✔ Fecha recibida: " + fecha +
                    (campo != null ? " | campo=" + campo : ""));

            /* ── 1. Recuperar partidos ── */
            List<Partido> partidos = (campo == null || campo.isBlank())
                    ? partidoService.getPartidosPorFecha(fecha)
                    : partidoService.getPartidosPorFechaYCampo(fecha, campo);

            /* ── 2. Mapear a intervalos ocupados ── */
            List<HorarioOcupadoResponse> horarios = partidos.stream()
                    .map(p -> {
                        var ini = p.getHora().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);

                        /* Duración según campo */
                        int duracionMin;
                        String c = p.getCampo().toLowerCase();
                        if (c.contains("f11"))      duracionMin = 120;   // 2 h
                        else if (c.contains("f8"))  duracionMin =  80;   // 1 h 20 min
                        else                        duracionMin =  90;   // por defecto

                        var fin = ini.plusMinutes(duracionMin);
                        return new HorarioOcupadoResponse(ini.toString(), fin.toString());
                    })
                    .toList();

            return ResponseEntity.ok(horarios);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap(
                            "error", "Error procesando la petición: " + e.getMessage()));
        }
    }

    @DeleteMapping("/partido/{id}")
    public ResponseEntity<?> eliminarPartido(@PathVariable Long id) {
        if (!partidoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        partidoRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }




}
