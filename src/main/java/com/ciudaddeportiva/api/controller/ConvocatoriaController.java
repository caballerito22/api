package com.ciudaddeportiva.api.controller;

import com.ciudaddeportiva.api.model.Convocatoria;
import com.ciudaddeportiva.api.service.ConvocatoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/convocatorias")
public class ConvocatoriaController {

    @Autowired
    private ConvocatoriaService convocatoriaService;

    @PostMapping("/convocar")
    public ResponseEntity<?> convocarJugador(@RequestParam Long partidoId, @RequestParam Long jugadorId) {
        try {
            Convocatoria convocatoria = convocatoriaService.convocarJugador(partidoId, jugadorId);
            return ResponseEntity.ok(convocatoria);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/partido/{partidoId}")
    public ResponseEntity<List<Convocatoria>> obtenerConvocatoriasPorPartido(@PathVariable Long partidoId) {
        return ResponseEntity.ok(convocatoriaService.obtenerConvocatoriasPorPartido(partidoId));
    }

    @GetMapping("/jugador/{jugadorId}")
    public ResponseEntity<List<Convocatoria>> obtenerConvocatoriasPorJugador(@PathVariable Long jugadorId) {
        return ResponseEntity.ok(convocatoriaService.obtenerConvocatoriasPorJugador(jugadorId));
    }

    @DeleteMapping("/{convocatoriaId}")
    public ResponseEntity<?> eliminarConvocatoria(@PathVariable Long convocatoriaId) {
        convocatoriaService.eliminarConvocatoria(convocatoriaId);
        return ResponseEntity.ok().build();
    }
}
