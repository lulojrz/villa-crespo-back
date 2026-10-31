package com.villacrespo.back.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "numeros_telefonicos")
@Getter
@Setter
public class Numero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean contesta;

    private String direccion;
    private String territorio;
    private String edificio;
    private String numero;

    @Column(name = "ultima_fecha", nullable = false)
    private LocalDateTime ultimaFecha;

    private Boolean reservado;

    @ManyToOne
    @JoinColumn(name = "ultUsuario")
    private Usuario ultUsuario;

    private Boolean tocar;

    @ManyToOne
    @JoinColumn(name = "reservadoA")
    private Usuario reservadoA;

    private LocalDateTime fechaReserva;
}
