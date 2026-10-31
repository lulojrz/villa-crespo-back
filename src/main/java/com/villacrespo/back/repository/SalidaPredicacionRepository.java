package com.villacrespo.back.repository;

import com.villacrespo.back.model.SalidaPredicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalidaPredicacionRepository extends JpaRepository<SalidaPredicacion, Long> {
}
