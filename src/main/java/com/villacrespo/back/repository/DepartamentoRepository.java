package com.villacrespo.back.repository;

import com.villacrespo.back.model.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {
    List<Departamento> findByPublicadorIdAndEstado(Long publicadorId, String estado);
}
