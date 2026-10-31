package com.villacrespo.back.repository;

import com.villacrespo.back.model.PlantillaTurno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlantillaTurnoRepository extends JpaRepository<PlantillaTurno, Long> {
}
