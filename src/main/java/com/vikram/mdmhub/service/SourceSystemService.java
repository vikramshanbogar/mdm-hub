package com.vikram.mdmhub.service;

import com.vikram.mdmhub.domain.SourceSystem;
import com.vikram.mdmhub.dto.SourceSystemRequest;
import com.vikram.mdmhub.exception.ConflictException;
import com.vikram.mdmhub.exception.ResourceNotFoundException;
import com.vikram.mdmhub.repository.SourceSystemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SourceSystemService {

    private final SourceSystemRepository sourceSystemRepository;

    public SourceSystem create(SourceSystemRequest request) {
        if (sourceSystemRepository.existsByCode(request.code())) {
            throw new ConflictException("A source system with code '" + request.code() + "' already exists");
        }
        SourceSystem system = SourceSystem.builder()
                .code(request.code())
                .name(request.name())
                .description(request.description())
                .build();
        return sourceSystemRepository.save(system);
    }

    @Transactional(readOnly = true)
    public List<SourceSystem> findAll() {
        return sourceSystemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public SourceSystem findById(Long id) {
        return sourceSystemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Source system " + id + " not found"));
    }

    public SourceSystem update(Long id, SourceSystemRequest request) {
        SourceSystem system = findById(id);
        system.setName(request.name());
        system.setDescription(request.description());
        return sourceSystemRepository.save(system);
    }

    public void delete(Long id) {
        SourceSystem system = findById(id);
        sourceSystemRepository.delete(system);
    }
}
