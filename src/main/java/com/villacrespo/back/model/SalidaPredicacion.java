package com.villacrespo.back.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "salidas_predicacion")
@Getter
@Setter
public class SalidaPredicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(name = "punto_encuentro", nullable = false)
    private String puntoEncuentro;

    @Column(name = "grupos")
    private String grupos;

    @ManyToOne
    @JoinColumn(name = "conductor_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Usuario conductor;

    @ManyToOne
    @JoinColumn(name = "territorio_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "manzanas", "asignaciones"})
    private Territorio territorio;
}
