package com.ciudaddeportiva.api.controller;

import com.ciudaddeportiva.api.model.LoginRequest;
import com.ciudaddeportiva.api.model.Rol;
import com.ciudaddeportiva.api.model.Usuario;
import com.ciudaddeportiva.api.model.UsuarioStatsDTO;
import com.ciudaddeportiva.api.repository.UsuarioRepository;
import com.ciudaddeportiva.api.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

//Recibe un objeto Usuario en formato JSON
//usa UsuarioService para registrar al usuario
//devuelve mensaje

//gestiona toodo lo relacionado con los usuarios
@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    //para ver los jugadores
    @GetMapping("/jugadores")
    public List<Usuario> getJugadores() {
        return usuarioRepository.findByRol(Rol.jugador);
    }

    //LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> loginUsuario(@RequestBody Map<String, String> loginData) {
        try {
            String email = loginData.get("email");
            String password = loginData.get("password");

            //buena contraseña,  +6 caracteres
            if (password == null || password.length() < 6) {
                return ResponseEntity.badRequest().body(
                        Collections.singletonMap("error", "La contraseña debe tener al menos 6 caracteres")
                );
            }

            Usuario usuario = usuarioService.login(email, password);

            return ResponseEntity.ok(Map.of(
                    "message", "Login correcto",
                    "rol", usuario.getRol().toString(),
                    "id", usuario.getId(),
                    "email", usuario.getEmail()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    //registro
    @PostMapping("/register")
    public ResponseEntity<?> registrarUsuario(@RequestBody LoginRequest request) {

        //contraseña larga (>6)
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", "La contraseña debe tener al menos 6 caracteres"));
        }

        //se mria si ya exixte
        Optional<Usuario> existente = usuarioService.buscarPorEmail(request.getEmail());
        if (existente.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("message", "El usuario ya existe"));
        }

        //si es admin
        String rol = request.getEmail().equalsIgnoreCase("admin@cdcaballero.com") ? "admin" : request.getRol();

        //para crear usuario
        Usuario nuevo = new Usuario();
        nuevo.setEmail(request.getEmail());
        nuevo.setPassword(request.getPassword());
        nuevo.setRol(Rol.valueOf(rol));

        //se guarda
        usuarioService.guardar(nuevo);

        return ResponseEntity.ok(Collections.singletonMap("message", "Usuario registrado con éxito"));
    }

    //estadísitcas
    @GetMapping("/stats")
    public List<UsuarioStatsDTO> estadisticasUsuarios() {
        return usuarioService.obtenerEstadisticasUsuarios();
    }

    //esto no funciona
    @GetMapping("/jugadores-disponibles")
    public List<Usuario> jugadoresDisponibles(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  LocalDate fecha,
            @RequestParam @DateTimeFormat(pattern = "HH:mm")LocalTime hora) {
        return usuarioService.jugadoresDisponibles(fecha, hora);
    }

    //para borrar el usuario
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id,
                                      @RequestParam(required = false) Long adminId) {
        // Si no se proporciona adminId, entendemos que es el propio usuario quien quiere borrarse
        if (adminId == null) {
            usuarioService.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Usuario eliminado por sí mismo"));
        }

        //si es admin, comprobamos su rol
        Usuario admin = usuarioService.findById(adminId);
        if (admin.getRol() != Rol.admin)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        usuarioService.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Usuario eliminado por admin"));
    }

    //para listarle todos los usuarios al admin
    @GetMapping("/admin/listado")
    public List<UsuarioStatsDTO> listadoAdmin(@RequestParam Long adminId) {
        if (usuarioService.findById(adminId).getRol() != Rol.admin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return usuarioService.listadoAdmin();
    }

}
