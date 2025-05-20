package com.ciudaddeportiva.api.repository;

import com.ciudaddeportiva.api.model.Rol;
import com.ciudaddeportiva.api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

//permite guardar, buscar y comprobar si existe un usuario

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    //para el login
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);

    //para la convocatoria
    List<Usuario> findByRol(Rol rol);

}
