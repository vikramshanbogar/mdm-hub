package com.vikram.mdmhub.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Traceability record ("xref") linking a golden {@link Party} record
 * back to the specific record id it corresponds to in a given
 * {@link SourceSystem}. A single Party can have many cross-references
 * (one per contributing source system).
 */
@Entity
@Table(
    name = "party_cross_reference",
    uniqueConstraints = @UniqueConstraint(columnNames = {"source_system_id", "source_record_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyCrossReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_system_id", nullable = false)
    private SourceSystem sourceSystem;

    @Column(name = "source_record_id", nullable = false, length = 100)
    private String sourceRecordId;

    @Builder.Default
    @Column(name = "last_synced_at", nullable = false)
    private Instant lastSyncedAt = Instant.now();
}
