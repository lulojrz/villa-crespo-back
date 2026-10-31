package com.villacrespo.back.repository;

import com.villacrespo.back.model.Manzana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManzanaRepository extends JpaRepository<Manzana, Long> {
}
