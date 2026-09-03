package com.vikram.mdmhub.service;

import com.vikram.mdmhub.domain.Party;
import com.vikram.mdmhub.domain.PartyCrossReference;
import com.vikram.mdmhub.domain.PartyStatus;
import com.vikram.mdmhub.domain.SourceSystem;
import com.vikram.mdmhub.dto.CrossReferenceRequest;
import com.vikram.mdmhub.dto.PartyRequest;
import com.vikram.mdmhub.exception.ConflictException;
import com.vikram.mdmhub.exception.ResourceNotFoundException;
import com.vikram.mdmhub.repository.PartyCrossReferenceRepository;
import com.vikram.mdmhub.repository.PartyRepository;
import com.vikram.mdmhub.repository.SourceSystemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Core MDM operations: maintaining golden records, tracing them back
 * to source systems, and merging duplicates ("survivorship").
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PartyService {

    private final PartyRepository partyRepository;
    private final SourceSystemRepository sourceSystemRepository;
    private final PartyCrossReferenceRepository crossReferenceRepository;

    public Party create(PartyRequest request) {
        // 1. Validate the source system exists before creating anything
        SourceSystem sourceSystem = sourceSystemRepository.findByCode(request.sourceSystemCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Source system '" + request.sourceSystemCode() + "' not found"));

        // 2. Ensure this source record isn't already mapped to someone else
        crossReferenceRepository.findBySourceSystem_CodeAndSourceRecordId(
                        request.sourceSystemCode(), request.sourceRecordId())
                .ifPresent(existing -> {
                    throw new ConflictException("Source record '" + request.sourceRecordId() +
                            "' from '" + request.sourceSystemCode() + "' is already linked to a party");
                });

        // 3. Create and save the Party
        Party party = Party.builder()
                .partyType(request.partyType())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .organizationName(request.organizationName())
                .email(request.email())
                .phone(request.phone())
                .addressLine1(request.addressLine1())
                .city(request.city())
                .state(request.state())
                .postalCode(request.postalCode())
                .country(request.country())
                .build();

        Party savedParty = partyRepository.save(party);

        // 4. Create and save the Cross Reference
        PartyCrossReference xref = PartyCrossReference.builder()
                .party(savedParty)
                .sourceSystem(sourceSystem)
                .sourceRecordId(request.sourceRecordId())
                .build();

        crossReferenceRepository.save(xref);

        return savedParty;
    }

    @Transactional(readOnly = true)
    public Page<Party> findGoldenRecords(Pageable pageable) {
        return partyRepository.findByGoldenRecordTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Party findById(UUID id) {
        return partyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Party " + id + " not found"));
    }

    public Party update(UUID id, PartyRequest request) {
        Party party = findById(id);
        party.setPartyType(request.partyType());
        party.setFirstName(request.firstName());
        party.setLastName(request.lastName());
        party.setOrganizationName(request.organizationName());
        party.setEmail(request.email());
        party.setPhone(request.phone());
        party.setAddressLine1(request.addressLine1());
        party.setCity(request.city());
        party.setState(request.state());
        party.setPostalCode(request.postalCode());
        party.setCountry(request.country());
        return partyRepository.save(party);
    }

    public void delete(UUID id) {
        Party party = findById(id);
        partyRepository.delete(party);
    }

    /**
     * Records that a given source-system record maps to this golden Party.
     */
    public PartyCrossReference addCrossReference(UUID partyId, CrossReferenceRequest request) {
        Party party = findById(partyId);
        SourceSystem sourceSystem = sourceSystemRepository.findByCode(request.sourceSystemCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Source system '" + request.sourceSystemCode() + "' not found"));

        crossReferenceRepository.findBySourceSystem_CodeAndSourceRecordId(
                        request.sourceSystemCode(), request.sourceRecordId())
                .ifPresent(existing -> {
                    throw new ConflictException("Source record '" + request.sourceRecordId() +
                            "' from '" + request.sourceSystemCode() + "' is already linked to a party");
                });

        PartyCrossReference xref = PartyCrossReference.builder()
                .party(party)
                .sourceSystem(sourceSystem)
                .sourceRecordId(request.sourceRecordId())
                .build();
        return crossReferenceRepository.save(xref);
    }

    @Transactional(readOnly = true)
    public List<PartyCrossReference> findCrossReferences(UUID partyId) {
        // Use the new method that fetches the lazy relationship
        return crossReferenceRepository.findByPartyWithSourceSystem(findById(partyId));
    }

    @Transactional(readOnly = true)
    public Party lookupBySourceReference(String sourceSystemCode, String sourceRecordId) {
        PartyCrossReference xref = crossReferenceRepository
                .findBySourceSystem_CodeAndSourceRecordId(sourceSystemCode, sourceRecordId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No party linked to " + sourceSystemCode + "/" + sourceRecordId));
        return xref.getParty();
    }

    /**
     * Merges {@code duplicateId} into {@code survivorId}: all of the
     * duplicate's cross-references are re-pointed at the survivor, and the
     * duplicate is marked as MERGED and stops being a golden record.
     * This mirrors the "survivorship" step of a real MDM match/merge pipeline.
     */
    public Party merge(UUID survivorId, UUID duplicateId) {
        if (survivorId.equals(duplicateId)) {
            throw new ConflictException("A party cannot be merged into itself");
        }
        Party survivor = findById(survivorId);
        Party duplicate = findById(duplicateId);

        if (!duplicate.isGoldenRecord()) {
            throw new ConflictException("Party " + duplicateId + " has already been merged");
        }

        List<PartyCrossReference> duplicateXrefs = crossReferenceRepository.findByParty(duplicate);
        for (PartyCrossReference xref : duplicateXrefs) {
            xref.setParty(survivor);
            crossReferenceRepository.save(xref);
        }

        duplicate.setGoldenRecord(false);
        duplicate.setStatus(PartyStatus.MERGED);
        duplicate.setMergedIntoId(survivor.getId());
        partyRepository.save(duplicate);

        return survivor;
    }
}
