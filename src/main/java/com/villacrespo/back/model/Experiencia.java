package com.villacrespo.back.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "experiencias")
@Getter
@Setter
public class Experiencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha", nullable = false, updatable = false)
    private LocalDateTime fecha;

    @ManyToOne
    @JoinColumn(name = "usuario")
    @JsonIgnoreProperties({"contrasena", "privilegio", "authorities", "enabled", "accountNonExpired", "accountNonLocked", "credentialsNonExpired"})
    private Usuario usuario;

    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private String categoria;
}
