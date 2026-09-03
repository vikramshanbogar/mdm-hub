package com.vikram.mdmhub.dto;

import com.vikram.mdmhub.domain.PartyCrossReference;

import java.time.Instant;
import java.util.UUID;

public record CrossReferenceResponse(
        Long id,
        UUID partyId,
        String sourceSystemCode,
        String sourceRecordId,
        Instant lastSyncedAt
) {
    public static CrossReferenceResponse from(PartyCrossReference x) {
        return new CrossReferenceResponse(
                x.getId(),
                x.getParty().getId(),
                x.getSourceSystem().getCode(),
                x.getSourceRecordId(),
                x.getLastSyncedAt()
        );
    }
}
