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

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PartidoRepository partidoRepository;

    @Autowired                          // 🔹 añade la inyección que faltaba
    private ConvocatoriaRepository convocatoriaRepository;



    // Login
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

    // Buscar por email
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public void guardar(Usuario usuario) {
        usuarioRepository.save(usuario);
    }

    // --- CORREGIDO: usar Long como tipo de ID ---
    public Usuario findById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    // Estadísticas globales de usuarios
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

    public List<Usuario> jugadoresDisponibles(LocalDate fecha, LocalTime hora) {
        List<Long> ocupados = convocatoriaRepository.findJugadoresOcupados(fecha, hora);
        return usuarioRepository.findJugadoresLibres(Rol.jugador,
                ocupados.isEmpty() ? null : ocupados);
    }

    //para borrar el usuario
    public void deleteById(Long id) { usuarioRepository.deleteById(id); }

    // UsuarioService.java   (método nuevo)
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
