package com.ciudaddeportiva.api.controllerLogica;

import com.ciudaddeportiva.api.estado.EstadoPartido;
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

//gestiona toodo lo relacionado con las reservas (hasta las horas en rojo al crear partido)
@RestController
@RequestMapping("/api/partidos")
@CrossOrigin(origins = "*")
public class PartidoController {

    @Autowired private PartidoService partidoService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private PartidoRepository partidoRepository;


    //se crea partido o entrenamiento
    @PostMapping("/crear")
    public ResponseEntity<?> crearPartido(@RequestBody PartidoRequest req) {
        try {
            //se paresa la fecha y la hora
            LocalDate fecha = LocalDate.parse(req.getFecha());
            LocalTime hora  = LocalTime.parse(req.getHora());

            //para ver quien lo crea
            Usuario creador = usuarioService.findById(req.getUsuarioId());

            //crar partido (con posibles convocados)
            Partido partido = partidoService.crearPartido(
                    fecha,
                    hora,
                    req.getCampo(),
                    req.getEquipoLocal(),
                    req.getEquipoVisitante(),
                    creador,                       //usuario que lo crea
                    req.getTipoReserva(),
                    req.getConvocados()            //lista de los ID de los jugadores
            );

            //si esta bien
            return ResponseEntity.ok(partido);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    //función para ver los partidos propios de jug. o ent.
    @GetMapping("/mis/{userId}")
    public List<Partido> obtenerMisPartidos(@PathVariable Long userId) {
        return partidoService.getPartidosByUsuario(userId);
    }

    //todos los partidos solo para el administrador
    @GetMapping("/todos")
    public ResponseEntity<?> todosPartidos(@RequestParam Long userId) {
        Usuario u = usuarioService.findById(userId);
        if (u.getRol() != Rol.admin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error", "Solo el admin puede ver todos los partidos"));
        }
        return ResponseEntity.ok(partidoService.getAllPartidos());
    }


    //el administrador cabia el esztado
    @PatchMapping("/estado")
    public ResponseEntity<?> cambiarEstado(@RequestBody CambiarEstadoRequest r){
        partidoService.cambiarEstado(r.getPartidoId(),
                EstadoPartido.valueOf(r.getNuevoEstado()));
        return ResponseEntity.ok(Collections.singletonMap("message","Estado actualizado"));
    }

    //metodo para ver todos los partidos para los jugadores
    @GetMapping("/publicos")
    public List<Partido> partidosPublicos() {
        return partidoService.getAllPartidos();
    }

    //horas en rojo para un dia al crear partido
    @GetMapping("/ocupados")
    public ResponseEntity<?> obtenerHorariosOcupados(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha,
            @RequestParam(required = false) String campo) {
        try {
            //se cogen los partidos
            List<Partido> partidos = (campo == null || campo.isBlank())
                    ? partidoService.getPartidosPorFecha(fecha)
                    : partidoService.getPartidosPorFechaYCampo(fecha, campo);

            //es mapean las horas ocupadas
            List<HorarioOcupadoResponse> horarios = partidos.stream()
                    .map(p -> {
                        var ini = p.getHora().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);

                        //cada campo tiene una duración
                        int duracionMin;
                        String c = p.getCampo().toLowerCase();
                        if (c.contains("f11"))      duracionMin = 120;   //2 h los de F11
                        else if (c.contains("f8"))  duracionMin =  80;   //esto los F8 1 h 20 min
                        else                        duracionMin =  90;   //por defecto (entrene)

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

    //para que el admin elimine partido
    @DeleteMapping("/partido/{id}")
    public ResponseEntity<?> eliminarPartido(@PathVariable Long id) {
        if (!partidoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        partidoRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }




}
