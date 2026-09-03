package com.vikram.mdmhub.repository;

import com.vikram.mdmhub.domain.SourceSystem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SourceSystemRepository extends JpaRepository<SourceSystem, Long> {
    Optional<SourceSystem> findByCode(String code);
    boolean existsByCode(String code);
}
