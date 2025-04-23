package com.ciudaddeportiva.api.service;

import com.ciudaddeportiva.api.model.Usuario;
import com.ciudaddeportiva.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

//Comprueba si el correo ya existe
//Guarda el usuario si no está registrado
//Devuelve mensajes de éxito o error
//Está preparado para usarse en el controlador (@Service)

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Registro de nuevo usuario
    public String registrar(Usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            return "El email ya está en uso";
        }

        usuarioRepository.save(usuario);
        return "Usuario registrado correctamente como " + usuario.getRol();
    }



    public Usuario login(String email, String password) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (usuario.getPassword().equals(password)) {
                return usuario;
            } else {
                throw new RuntimeException("Contraseña incorrecta");
            }
        } else {
            throw new RuntimeException("Usuario no encontrado");
        }
    }

    //para registrar y para el login
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public void guardar(Usuario usuario) {
        usuarioRepository.save(usuario);
    }

    public Usuario findById(int id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }







}
