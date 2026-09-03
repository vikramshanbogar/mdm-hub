package com.vikram.mdmhub.service;

import com.vikram.mdmhub.domain.Party;
import com.vikram.mdmhub.domain.PartyStatus;
import com.vikram.mdmhub.domain.PartyType;
import com.vikram.mdmhub.dto.CrossReferenceRequest;
import com.vikram.mdmhub.dto.PartyRequest;
import com.vikram.mdmhub.dto.SourceSystemRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PartyServiceTest {

    @Autowired
    private PartyService partyService;

    @Autowired
    private SourceSystemService sourceSystemService;

    @Test
    void createLinkAndMergeParties() {
        sourceSystemService.create(new SourceSystemRequest("CRM", "CRM System", "Customer records"));

        Party survivor = partyService.create(new PartyRequest(
                PartyType.INDIVIDUAL, "Vikram", "Shanbogar", null,
                "vikram@example.com", "9999999999", null, "Bengaluru", "KA", "560001", "IN"));

        Party duplicate = partyService.create(new PartyRequest(
                PartyType.INDIVIDUAL, "Vikram", "S", null,
                "v.shanbogar@example.com", null, null, "Bengaluru", "KA", "560001", "IN"));

        partyService.addCrossReference(duplicate.getId(), new CrossReferenceRequest("CRM", "CRM-1001"));

        Party merged = partyService.merge(survivor.getId(), duplicate.getId());
        assertThat(merged.getId()).isEqualTo(survivor.getId());

        Party reloadedDuplicate = partyService.findById(duplicate.getId());
        assertThat(reloadedDuplicate.isGoldenRecord()).isFalse();
        assertThat(reloadedDuplicate.getStatus()).isEqualTo(PartyStatus.MERGED);
        assertThat(reloadedDuplicate.getMergedIntoId()).isEqualTo(survivor.getId());

        assertThat(partyService.findCrossReferences(survivor.getId())).hasSize(1);

        Party foundBySource = partyService.lookupBySourceReference("CRM", "CRM-1001");
        assertThat(foundBySource.getId()).isEqualTo(survivor.getId());
    }
}
