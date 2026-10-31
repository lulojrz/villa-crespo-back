package com.villacrespo.back.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "reporte_carritos")
@Getter
@Setter
public class ReporteCarrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    @JsonIgnoreProperties({"contrasena", "privilegio", "authorities", "enabled", "accountNonExpired", "accountNonLocked", "credentialsNonExpired"})
    private Usuario usuario;

    @Column(name = "fecha", nullable = false, updatable = false)
    private LocalDateTime fecha;

    @Column(name = "falta_literatura")
    private Boolean faltaLiteratura;

    @Column(name = "literatura_detalle")
    private String literaturaDetalle;

    @Column(name = "necesita_limpieza")
    private Boolean necesitaLimpieza;

    private String observaciones;
}
