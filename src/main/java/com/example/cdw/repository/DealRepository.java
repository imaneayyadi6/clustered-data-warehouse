package com.example.cdw.repository;

import com.example.cdw.model.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DealRepository extends JpaRepository<Deal, Long> {
    Optional<Deal> findByDealUniqueId(String dealUniqueId);
}
