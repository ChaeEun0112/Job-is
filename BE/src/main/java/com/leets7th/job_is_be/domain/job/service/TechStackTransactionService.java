package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.domain.job.entity.TechStack;
import com.leets7th.job_is_be.domain.job.repository.TechStackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class TechStackTransactionService {

    private final TechStackRepository techStackRepository;

    public TechStackTransactionService(TechStackRepository techStackRepository) {
        this.techStackRepository = techStackRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TechStack create(String name, String normalizedName) {
        return techStackRepository.saveAndFlush(TechStack.builder()
                .name(name)
                .normalizedName(normalizedName)
                .build());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Optional<TechStack> findByNormalizedName(String normalizedName) {
        return techStackRepository.findByNormalizedName(normalizedName);
    }
}
