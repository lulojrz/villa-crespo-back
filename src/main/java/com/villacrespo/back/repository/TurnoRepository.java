package com.villacrespo.back.repository;

import com.villacrespo.back.model.Turno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {

    @EntityGraph(attributePaths = {"punto", "publicador1", "publicador2", "publicador3", "publicador4"})
    List<Turno> findByFechaBetweenOrderByFechaAscHoraInicioAsc(LocalDate inicio, LocalDate fin);

    @EntityGraph(attributePaths = {"punto", "publicador1", "publicador2", "publicador3", "publicador4"})
    Page<Turno> findByFechaBetween(LocalDate inicio, LocalDate fin, Pageable pageable);

    @EntityGraph(attributePaths = {"punto", "publicador1", "publicador2", "publicador3", "publicador4"})
    @Query("""
        SELECT t FROM Turno t
        WHERE t.fecha >= :desde
          AND (LOWER(t.publicador1.usuario) = LOWER(:usuario)
            OR LOWER(t.publicador2.usuario) = LOWER(:usuario)
            OR LOWER(t.publicador3.usuario) = LOWER(:usuario)
            OR LOWER(t.publicador4.usuario) = LOWER(:usuario))
        ORDER BY t.fecha ASC, t.horaInicio ASC
    """)
    List<Turno> findTurnosDeUsuarioDesde(@Param("usuario") String usuario, @Param("desde") LocalDate desde);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM Turno t WHERE t.fecha >= :inicio AND t.fecha <= :fin")
    void deleteByFechaBetween(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);
}