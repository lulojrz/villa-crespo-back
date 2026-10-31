package com.villacrespo.back.repository;

import com.villacrespo.back.model.Turno;
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
    List<Turno> findByFechaBetweenOrderByFechaAscHoraInicioAsc(LocalDate inicio, LocalDate fin);
    List<Turno> findByPublicador1_UsuarioOrPublicador2_Usuario(String usuario1, String usuario2);

    @Modifying
    @Transactional
    @Query("DELETE FROM Turno t WHERE t.fecha >= :inicio AND t.fecha <= :fin")
    void deleteByFechaBetween(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);
}
