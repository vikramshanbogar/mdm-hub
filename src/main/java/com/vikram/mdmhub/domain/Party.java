package com.vikram.mdmhub.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * The master ("golden") record for a person or organization.
 * This is the core entity of the MDM hub: every downstream system's
 * view of a party is reconciled into one of these records.
 */
@Entity
@Table(name = "party")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Party {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PartyType partyType;

    private String firstName;

    private String lastName;

    private String organizationName;

    @Column(length = 150)
    private String email;

    @Column(length = 30)
    private String phone;

    private String addressLine1;

    private String city;

    private String state;

    private String postalCode;

    private String country;

    /**
     * True when this record is the current survivor/golden record.
     * Set to false once a record has been merged into another one.
     */
    @Builder.Default
    @Column(name = "golden_record", nullable = false)
    private boolean goldenRecord = true;

    /**
     * When this record has been merged, points at the surviving Party.
     */
    @Column(name = "merged_into_id")
    private UUID mergedIntoId;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private PartyStatus status = PartyStatus.ACTIVE;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
