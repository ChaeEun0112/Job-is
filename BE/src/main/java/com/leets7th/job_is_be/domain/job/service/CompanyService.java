package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.domain.job.dto.CrawledCompanyDto;
import com.leets7th.job_is_be.domain.job.entity.Company;
import com.leets7th.job_is_be.domain.job.repository.CompanyRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Service
@Builder
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;

    private static final String ENRICHED = "enriched";
    private static final String NOT_FOUND = "not_found";

    @Transactional
    public Company getOrCreateCompany(CrawledCompanyDto companyDto) {
        if (companyDto == null || companyDto.name() == null || companyDto.normalizedName() == null) return null;

        return companyRepository.findByNormalizedName(companyDto.normalizedName())
                .map(existing -> {
                    // 이전 크롤링에선 not_found/name_mismatch였다가 이번엔 enriched로 보강된 경우에만 갱신.
                    // 공고 처리 루프에서 오는 얕은 회사 정보(enrichmentStatus=null)로는 절대 덮어쓰지 않는다.
                    if (!ENRICHED.equals(existing.getEnrichmentStatus()) && ENRICHED.equals(companyDto.enrichmentStatus())) {
                        existing.enrich(
                                companyDto.jobkoreaGnoRef(),
                                companyDto.employeeCount(),
                                companyDto.companyType(),
                                companyDto.industry(),
                                companyDto.stockStatus(),
                                companyDto.hqAddress(),
                                companyDto.enrichmentStatus(),
                                companyDto.nameMatch(),
                                companyDto.rejectedName(),
                                companyDto.rawJobkorea(),
                                OffsetDateTime.now()
                        );
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    log.info("저장할 회사 정보: name={}, source={}, logo={}",
                            companyDto.name(), "wanted", companyDto.logoUrl());

                    // 공고 처리 루프에서 오는 얕은 회사 정보(잡코리아 보강 전 단계)는 enrichmentStatus가 없어서,
                    // 크롤러(run_pipeline.py)가 companies.jsonl에 쓸 때와 동일한 기본값으로 맞춘다.
                    String enrichmentStatus = companyDto.enrichmentStatus() != null ? companyDto.enrichmentStatus() : NOT_FOUND;

                    return companyRepository.saveAndFlush(
                            Company.builder()
                                    .name(companyDto.name())
                                    .normalizedName(companyDto.normalizedName())
                                    .logoUrl(companyDto.logoUrl())
                                    .description(companyDto.description())
                                    .registrationNumber(companyDto.registrationNumber())
                                    .wantedCompanyId(companyDto.wantedCompanyId())
                                    .jobkoreaGnoRef(companyDto.jobkoreaGnoRef())
                                    .employeeCount(companyDto.employeeCount())
                                    .companyType(companyDto.companyType())
                                    .industry(companyDto.industry())
                                    .stockStatus(companyDto.stockStatus())
                                    .hqAddress(companyDto.hqAddress())
                                    .enrichmentStatus(enrichmentStatus)
                                    .nameMatch(companyDto.nameMatch())
                                    .rejectedName(companyDto.rejectedName())
                                    .rawJobkorea(companyDto.rawJobkorea())
                                    .enrichedAt(ENRICHED.equals(companyDto.enrichmentStatus()) ? OffsetDateTime.now() : null)
                                    .source("wanted")
                                    .build()
                    );
                });
    }
}
