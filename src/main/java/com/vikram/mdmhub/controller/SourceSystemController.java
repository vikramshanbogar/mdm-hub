package com.vikram.mdmhub.controller;

import com.vikram.mdmhub.domain.SourceSystem;
import com.vikram.mdmhub.dto.SourceSystemRequest;
import com.vikram.mdmhub.dto.SourceSystemResponse;
import com.vikram.mdmhub.service.SourceSystemService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/source-systems")
@RequiredArgsConstructor
@Tag(name = "Source Systems", description = "Upstream systems that feed records into the hub")
public class SourceSystemController {

    private final SourceSystemService sourceSystemService;

    @PostMapping
    public ResponseEntity<SourceSystemResponse> create(@Valid @RequestBody SourceSystemRequest request) {
        SourceSystem created = sourceSystemService.create(request);
        return ResponseEntity.created(URI.create("/api/source-systems/" + created.getId()))
                .body(SourceSystemResponse.from(created));
    }

    @GetMapping
    public List<SourceSystemResponse> findAll() {
        return sourceSystemService.findAll().stream().map(SourceSystemResponse::from).toList();
    }

    @GetMapping("/{id}")
    public SourceSystemResponse findById(@PathVariable Long id) {
        return SourceSystemResponse.from(sourceSystemService.findById(id));
    }

    @PutMapping("/{id}")
    public SourceSystemResponse update(@PathVariable Long id, @Valid @RequestBody SourceSystemRequest request) {
        return SourceSystemResponse.from(sourceSystemService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        sourceSystemService.delete(id);
    }
}
