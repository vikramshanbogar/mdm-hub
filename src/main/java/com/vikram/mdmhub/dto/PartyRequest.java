package com.vikram.mdmhub.dto;

import com.vikram.mdmhub.domain.PartyType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PartyRequest(

        // --- New Fields for Cross Reference ---
        @NotBlank
        String sourceSystemCode,

        @NotBlank
        String sourceRecordId,
        // --------------------------------------

        @NotNull
        PartyType partyType,

        String firstName,
        String lastName,
        String organizationName,

        @Email
        String email,

        String phone,
        String addressLine1,
        String city,
        String state,
        String postalCode,
        String country
) {
}