package com.vikram.mdmhub.dto;

import com.vikram.mdmhub.domain.SourceSystem;

import java.time.Instant;

public record SourceSystemResponse(
        Long id,
        String code,
        String name,
        String description,
        boolean active,
        Instant createdAt
) {
    public static SourceSystemResponse from(SourceSystem s) {
        return new SourceSystemResponse(s.getId(), s.getCode(), s.getName(), s.getDescription(), s.isActive(), s.getCreatedAt());
    }
}
