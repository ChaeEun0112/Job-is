package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.domain.job.entity.TechStack;
import com.leets7th.job_is_be.domain.job.repository.TechStackRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class TechStackRegistrationService {

    private final TechStackRepository techStackRepository;
    private final TechStackTransactionService transactionService;

    public TechStackRegistrationService(
            TechStackRepository techStackRepository,
            TechStackTransactionService transactionService
    ) {
        this.techStackRepository = techStackRepository;
        this.transactionService = transactionService;
    }

    public TechStack resolve(String name, String normalizedName) {
        return techStackRepository.findByNormalizedName(normalizedName)
                .orElseGet(() -> createOrFindExisting(name, normalizedName));
    }

    private TechStack createOrFindExisting(String name, String normalizedName) {
        try {
            return transactionService.create(name, normalizedName);
        } catch (DataIntegrityViolationException exception) {
            return transactionService.findByNormalizedName(normalizedName)
                    .orElseThrow(() -> exception);
        }
    }
}
