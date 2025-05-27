package com.ciudaddeportiva.api.service;

import com.ciudaddeportiva.api.model.Convocatoria;
import com.ciudaddeportiva.api.model.Partido;
import com.ciudaddeportiva.api.model.Usuario;
import com.ciudaddeportiva.api.repository.ConvocatoriaRepository;
import com.ciudaddeportiva.api.repository.PartidoRepository;
import com.ciudaddeportiva.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

//conv. jugadores a partidos: añadir, quitar y consultar convocados.

@Service
public class ConvocatoriaService {

    @Autowired
    private ConvocatoriaRepository convocatoriaRepository;

    @Autowired
    private PartidoRepository partidoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    //para conv.
    public Convocatoria convocarJugador(Long partidoId, Long jugadorId) throws Exception {
        if (convocatoriaRepository.existsByPartidoIdAndJugadorId(partidoId, jugadorId)) {
            throw new Exception("El jugador ya está convocado para este partido.");
        }

        Optional<Partido> partidoOpt = partidoRepository.findById(partidoId);
        Optional<Usuario> jugadorOpt = usuarioRepository.findById(jugadorId);

        //si está vaiío (no aarece nunca)
        if (partidoOpt.isEmpty() || jugadorOpt.isEmpty()) {
            throw new Exception("Partido o jugador no encontrado.");
        }

        Partido partido = partidoOpt.get();
        Usuario jugador = jugadorOpt.get();

        Convocatoria convocatoria = new Convocatoria(partido, jugador, LocalDateTime.now());
        return convocatoriaRepository.save(convocatoria);
    }

    //futuro
    public List<Convocatoria> obtenerConvocatoriasPorPartido(Long partidoId) {
        return convocatoriaRepository.findByPartidoId(partidoId);
    }

    //para que el jug pueda ver los partidos a los que está conv.
    public List<Convocatoria> obtenerConvocatoriasPorJugador(Long jugadorId) {
        return convocatoriaRepository.findByJugadorId(jugadorId);
    }

}
