package com.vikram.mdmhub.dto;

import jakarta.validation.constraints.NotBlank;

public record CrossReferenceRequest(

        @NotBlank
        String sourceSystemCode,

        @NotBlank
        String sourceRecordId
) {
}
