package com.villacrespo.back.repository;

import com.villacrespo.back.model.Numero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NumeroRepository extends JpaRepository<Numero, Long> {
}
