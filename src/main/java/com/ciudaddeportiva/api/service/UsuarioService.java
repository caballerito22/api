package com.ciudaddeportiva.api.service;

import com.ciudaddeportiva.api.model.Rol;
import com.ciudaddeportiva.api.model.Usuario;
import com.ciudaddeportiva.api.model.UsuarioStatsDTO;
import com.ciudaddeportiva.api.repository.ConvocatoriaRepository;
import com.ciudaddeportiva.api.repository.PartidoRepository;
import com.ciudaddeportiva.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

//lógica de usuarios,login,registro,validación...

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PartidoRepository partidoRepository;

    @Autowired
    private ConvocatoriaRepository convocatoriaRepository;

    //login
    //si no existe el email
    public Usuario login(String email, String password) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();
        if (!usuario.getPassword().equals(password)) {
            throw new RuntimeException("Contraseña incorrecta");
        }
        return usuario;
    }

    //busca x email para el registro
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    //guarda si se registra
    public void guardar(Usuario usuario) {
        usuarioRepository.save(usuario);
    }

    //busca los usuarios
    public Usuario findById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    //stats usuarios
    public List<UsuarioStatsDTO> obtenerEstadisticasUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();

        return usuarios.stream()
                .filter(u -> u.getRol() != Rol.admin)
                .map(u -> {
                    int reservas = partidoRepository.findByCreadoPor_Id(u.getId()).size();
                    return new UsuarioStatsDTO(u.getId(), u.getEmail(), u.getRol().toString(), reservas);
                })
                .collect(Collectors.toList());
    }

    //intento al convocar
    public List<Usuario> jugadoresDisponibles(LocalDate fecha, LocalTime hora) {
        List<Long> ocupados = convocatoriaRepository.findJugadoresOcupados(fecha, hora);
        return usuarioRepository.findJugadoresLibres(Rol.jugador,
                ocupados.isEmpty() ? null : ocupados);
    }

    //para borrar el usuario -si no está convocado o tiene partidos creados con jugadores convocados
    public void deleteById(Long id) { usuarioRepository.deleteById(id); }

    //muestra las cosas al gestionar usu para el admin
    public List<UsuarioStatsDTO> listadoAdmin() {
        return usuarioRepository.findAll().stream()
                .map(u -> {
                    Integer total = null;
                    if (u.getRol() == Rol.entrenador || u.getRol() == Rol.admin) {
                        total = partidoRepository.countByCreadoPor_Id(u.getId());
                    }
                    return new UsuarioStatsDTO(
                            u.getId(),
                            u.getEmail(),
                            u.getRol().toString(),
                            total
                    );
                })
                .toList();
    }







}
