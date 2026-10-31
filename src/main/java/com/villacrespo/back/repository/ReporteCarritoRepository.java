package com.villacrespo.back.repository;

import com.villacrespo.back.model.ReporteCarrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReporteCarritoRepository extends JpaRepository<ReporteCarrito, Long> {
}
