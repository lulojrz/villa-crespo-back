package com.villacrespo.back.repository;

import com.villacrespo.back.model.PuntoPredicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PuntoPredicacionRepository extends JpaRepository<PuntoPredicacion, Long> {
}
