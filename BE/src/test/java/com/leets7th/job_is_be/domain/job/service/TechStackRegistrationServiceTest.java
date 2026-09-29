package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.domain.job.entity.TechStack;
import com.leets7th.job_is_be.domain.job.repository.TechStackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TechStackRegistrationServiceTest {

    @Mock
    private TechStackRepository techStackRepository;
    @Mock
    private TechStackTransactionService transactionService;

    private TechStackRegistrationService registrationService;

    @BeforeEach
    void setUp() {
        registrationService = new TechStackRegistrationService(
                techStackRepository,
                transactionService
        );
    }

    @Test
    void returnsExistingStackAfterConcurrentInsertConflict() {
        TechStack existing = TechStack.builder()
                .name("Java")
                .normalizedName("java")
                .build();
        DataIntegrityViolationException conflict =
                new DataIntegrityViolationException("duplicate normalized name");

        when(techStackRepository.findByNormalizedName("java")).thenReturn(Optional.empty());
        when(transactionService.create("Java", "java")).thenThrow(conflict);
        when(transactionService.findByNormalizedName("java")).thenReturn(Optional.of(existing));

        TechStack result = registrationService.resolve("Java", "java");

        assertThat(result).isSameAs(existing);
    }
}
