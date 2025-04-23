package com.ciudaddeportiva.api.controller;

import com.ciudaddeportiva.api.model.LoginRequest;
import com.ciudaddeportiva.api.model.Rol;
import com.ciudaddeportiva.api.model.Usuario;
import com.ciudaddeportiva.api.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

//Escucha peticiones POST en:
//http://localhost:8080/api/usuarios/register

//Recibe un objeto Usuario en formato JSON
//Llama al UsuarioService para registrar al usuario
//Devuelve un mensaje de éxito o error

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> loginUsuario(@RequestBody Map<String, String> loginData) {
        try {
            String email = loginData.get("email");
            String password = loginData.get("password");

            // Validar contraseña ≥ 6 caracteres (además de la lógica de usuarioService)
            if (password == null || password.length() < 6) {
                return ResponseEntity.badRequest().body(
                        Collections.singletonMap("error", "La contraseña debe tener al menos 6 caracteres")
                );
            }

            Usuario usuario = usuarioService.login(email, password);

            return ResponseEntity.ok(Map.of(
                    "message", "Login correcto",
                    "rol", usuario.getRol().toString(),
                    "id", usuario.getId()

            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // REGISTRO
    @PostMapping("/register")
    public ResponseEntity<?> registrarUsuario(@RequestBody LoginRequest request) {

        // 1) Validar contraseña ≥ 6
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", "La contraseña debe tener al menos 6 caracteres"));
        }

        // 2) Comprobar si ya existe
        Optional<Usuario> existente = usuarioService.buscarPorEmail(request.getEmail());
        if (existente.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("message", "El usuario ya existe"));
        }

        // 3) Rol auto si es admin
        String rol = request.getEmail().equalsIgnoreCase("admin@cdcaballero.com") ? "admin" : request.getRol();

        // 4) Crear nuevo usuario
        Usuario nuevo = new Usuario();
        nuevo.setEmail(request.getEmail());
        nuevo.setPassword(request.getPassword());
        nuevo.setRol(Rol.valueOf(rol));

        // 5) Guardar
        usuarioService.guardar(nuevo);

        return ResponseEntity.ok(Collections.singletonMap("message", "Usuario registrado con éxito"));
    }

}
