package com.ciudaddeportiva.api.repository;

import com.ciudaddeportiva.api.model.Rol;
import com.ciudaddeportiva.api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

//permite guardar, buscar y comprobar si existe un usuario

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    //para el login
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);

    //para la convocatoria
    List<Usuario> findByRol(Rol rol);

    /* Jugadores cuyo id NO está en la lista de ocupados */
    @Query("""
           select u
           from Usuario u
           where u.rol = :rol
             and ( :ocupados is null or u.id not in :ocupados )
           """)
    List<Usuario> findJugadoresLibres(
            @Param("rol")      Rol rol,
            @Param("ocupados") List<Long> ocupados);


}
