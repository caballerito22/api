package com.ciudaddeportiva.api.controller;

import com.ciudaddeportiva.api.model.*;
import com.ciudaddeportiva.api.service.PartidoService;
import com.ciudaddeportiva.api.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

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
            LocalDate fecha = LocalDate.parse(req.getFecha());
            LocalTime hora  = LocalTime.parse(req.getHora());
            Usuario   user  = usuarioService.findById(req.getUsuarioId());

            Partido nuevo = partidoService.crearPartido(
                    fecha, hora, req.getCampo(),
                    req.getEquipoLocal(), req.getEquipoVisitante(),
                    user, req.getTipoReserva());

            return ResponseEntity.ok(nuevo);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    /* ---------- mis partidos (entrenador / jugador) ---------- */
    @GetMapping("/mis/{userId}")
    public List<Partido> obtenerMisPartidos(@PathVariable int userId) {
        return partidoService.getPartidosByUsuario(userId);
    }

    /* ---------- todos los partidos (solo admin) ---------- */
    @GetMapping("/todos")
    public ResponseEntity<?> todosPartidos(@RequestParam int userId) {
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
        partidoService.cambiarEstado(r.getIdPartido(),
                EstadoPartido.valueOf(r.getNuevoEstado()));
        return ResponseEntity.ok(Collections.singletonMap("message","Estado actualizado"));
    }
}
