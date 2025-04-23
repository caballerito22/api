package com.ciudaddeportiva.api.repository;

import com.ciudaddeportiva.api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

//permite guardar, buscar y comprobar si existe un usuario

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    //para el login
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
}
