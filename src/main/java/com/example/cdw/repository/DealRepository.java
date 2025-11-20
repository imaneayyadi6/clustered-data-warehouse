package com.example.cdw.repository;

import com.example.cdw.model.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DealRepository extends JpaRepository<Deal, Long> {

    // Used to prevent duplicates
    Optional<Deal> findByDealUniqueId(String dealUniqueId);
}
