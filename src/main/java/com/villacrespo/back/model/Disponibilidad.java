package com.villacrespo.back.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "disponibilidades")
@Getter
@Setter
public class Disponibilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "dia_semana", nullable = false)
    private String diaSemana;

    @Column(nullable = false)
    private LocalTime hora;
}
