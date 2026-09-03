package com.vikram.mdmhub.repository;

import com.vikram.mdmhub.domain.Party;
import com.vikram.mdmhub.domain.PartyCrossReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PartyCrossReferenceRepository extends JpaRepository<PartyCrossReference, Long> {

    Optional<PartyCrossReference> findBySourceSystem_CodeAndSourceRecordId(String sourceSystemCode, String sourceRecordId);

    List<PartyCrossReference> findByParty(Party party);


    // Use JOIN FETCH to grab the SourceSystem in the same SQL query
    @Query("SELECT x FROM PartyCrossReference x JOIN FETCH x.sourceSystem WHERE x.party = :party")
    List<PartyCrossReference> findByPartyWithSourceSystem(@Param("party") Party party);

}
