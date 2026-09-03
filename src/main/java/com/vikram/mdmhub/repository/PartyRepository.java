package com.vikram.mdmhub.repository;

import com.vikram.mdmhub.domain.Party;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PartyRepository extends JpaRepository<Party, UUID> {
    Page<Party> findByGoldenRecordTrue(Pageable pageable);
}
