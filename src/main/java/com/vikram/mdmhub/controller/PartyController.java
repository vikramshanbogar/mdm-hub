package com.vikram.mdmhub.controller;

import com.vikram.mdmhub.domain.Party;
import com.vikram.mdmhub.dto.*;
import com.vikram.mdmhub.service.PartyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/parties")
@RequiredArgsConstructor
@Tag(name = "Parties", description = "Golden/master records and merge (survivorship) operations")
public class PartyController {

    private final PartyService partyService;

    @PostMapping
    public ResponseEntity<PartyResponse> create(@Valid @RequestBody PartyRequest request) {
        Party created = partyService.create(request);
        return ResponseEntity.created(URI.create("/api/parties/" + created.getId()))
                .body(PartyResponse.from(created));
    }

    @GetMapping
    public Page<PartyResponse> findAll(Pageable pageable) {
        return partyService.findGoldenRecords(pageable).map(PartyResponse::from);
    }

    @GetMapping("/{id}")
    public PartyResponse findById(@PathVariable UUID id) {
        return PartyResponse.from(partyService.findById(id));
    }

    @GetMapping("/lookup")
    public PartyResponse lookup(@RequestParam String sourceSystem, @RequestParam String sourceRecordId) {
        return PartyResponse.from(partyService.lookupBySourceReference(sourceSystem, sourceRecordId));
    }

    @PutMapping("/{id}")
    public PartyResponse update(@PathVariable UUID id, @Valid @RequestBody PartyRequest request) {
        return PartyResponse.from(partyService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        partyService.delete(id);
    }

    @PostMapping("/{id}/cross-references")
    public ResponseEntity<CrossReferenceResponse> addCrossReference(
            @PathVariable UUID id, @Valid @RequestBody CrossReferenceRequest request) {
        var created = partyService.addCrossReference(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CrossReferenceResponse.from(created));
    }

    @GetMapping("/{id}/cross-references")
    public List<CrossReferenceResponse> crossReferences(@PathVariable UUID id) {
        return partyService.findCrossReferences(id).stream().map(CrossReferenceResponse::from).toList();
    }

    @PostMapping("/{survivorId}/merge/{duplicateId}")
    public PartyResponse merge(@PathVariable UUID survivorId, @PathVariable UUID duplicateId) {
        return PartyResponse.from(partyService.merge(survivorId, duplicateId));
    }
}
