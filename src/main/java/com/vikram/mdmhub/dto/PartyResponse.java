package com.vikram.mdmhub.dto;

import com.vikram.mdmhub.domain.Party;
import com.vikram.mdmhub.domain.PartyStatus;
import com.vikram.mdmhub.domain.PartyType;

import java.time.Instant;
import java.util.UUID;

public record PartyResponse(
        UUID id,
        PartyType partyType,
        String firstName,
        String lastName,
        String organizationName,
        String email,
        String phone,
        String addressLine1,
        String city,
        String state,
        String postalCode,
        String country,
        boolean goldenRecord,
        UUID mergedIntoId,
        PartyStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static PartyResponse from(Party p) {
        return new PartyResponse(
                p.getId(), p.getPartyType(), p.getFirstName(), p.getLastName(), p.getOrganizationName(),
                p.getEmail(), p.getPhone(), p.getAddressLine1(), p.getCity(), p.getState(),
                p.getPostalCode(), p.getCountry(), p.isGoldenRecord(), p.getMergedIntoId(),
                p.getStatus(), p.getCreatedAt(), p.getUpdatedAt()
        );
    }
}
