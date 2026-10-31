package com.villacrespo.back.repository;

import com.villacrespo.back.model.Campana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampanaRepository extends JpaRepository<Campana, Long> {
}
