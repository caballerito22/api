package com.ciudaddeportiva.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "convocatorias")
public class Convocatoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "partido_id", nullable = false)
    private Partido partido;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario jugador;

    @Column(nullable = false)
    private LocalDateTime fechaConvocatoria;

    public Convocatoria() {
    }

    public Convocatoria(Partido partido, Usuario jugador, LocalDateTime fechaConvocatoria) {
        this.partido = partido;
        this.jugador = jugador;
        this.fechaConvocatoria = fechaConvocatoria;
    }

    public Long getId() {
        return id;
    }

    public Partido getPartido() {
        return partido;
    }

    public void setPartido(Partido partido) {
        this.partido = partido;
    }

    public Usuario getJugador() {
        return jugador;
    }

    public void setJugador(Usuario jugador) {
        this.jugador = jugador;
    }

    public LocalDateTime getFechaConvocatoria() {
        return fechaConvocatoria;
    }

    public void setFechaConvocatoria(LocalDateTime fechaConvocatoria) {
        this.fechaConvocatoria = fechaConvocatoria;
    }

    @Override
    public String toString() {
        return "Convocatoria{" +
                "id=" + id +
                ", partido=" + partido.getId() +
                ", jugador=" + jugador.getId() +
                ", fechaConvocatoria=" + fechaConvocatoria +
                '}';
    }
}
