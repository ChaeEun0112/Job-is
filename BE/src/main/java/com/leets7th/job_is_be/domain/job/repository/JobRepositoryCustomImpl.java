package com.leets7th.job_is_be.domain.job.repository;

import com.leets7th.job_is_be.domain.job.dto.JobSearchRequest;
import com.leets7th.job_is_be.domain.job.dto.JobSummaryResponse;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.entity.QCompany;
import com.leets7th.job_is_be.domain.job.entity.QJobCategory;
import com.leets7th.job_is_be.domain.job.entity.QRegion;
import com.leets7th.job_is_be.domain.job.enums.CareerRange;
import com.leets7th.job_is_be.domain.job.enums.JobSortType;
import com.leets7th.job_is_be.domain.job.enums.JobStatus;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.leets7th.job_is_be.domain.job.entity.QJob.job;

@Repository
@RequiredArgsConstructor
public class JobRepositoryCustomImpl implements JobRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<JobSummaryResponse> searchJobs(JobSearchRequest request, Pageable pageable, Map<Long, Integer> pythonFitScores) {
        QCompany company = QCompany.company;
        QRegion region = QRegion.region;
        QJobCategory jobCategory = QJobCategory.jobCategory;

        OffsetDateTime now = OffsetDateTime.now();

        BooleanExpression[] conditions = new BooleanExpression[]{
                candidateUniverse(now),
                alwaysOpenIncluded(request),
                keywordContains(company, request.keyword()),
                categoryChildrenIn(request.categoryChildren()),
                citiesIn(request.cities()),
                districtsIn(request.districts()),
                careerRangesIn(request.careerRanges()),
                employmentTypesIn(request.employmentTypes()),
                remoteOnly(request)
        };

        // 전체 카운트 조회 (목록 조회와 동일한 LEFT JOIN 적용)
        Long total = queryFactory
                .select(job.count())
                .from(job)
                .leftJoin(job.company, company)
                .leftJoin(job.region, region)
                .leftJoin(job.jobCategory, jobCategory)
                .where(conditions)
                .fetchOne();
        long totalCount = total != null ? total : 0L;

        // 추천순 + 파이썬 매칭 엔진 점수가 있으면, SQL 정렬/페이징 대신 파이썬 점수 기준으로 정렬한다
        // (파이썬 엔진은 검색 필터·페이지네이션을 모르는 전체 후보 목록을 주기 때문).
        if (request.sortOrDefault() == JobSortType.FIT && !CollectionUtils.isEmpty(pythonFitScores)) {
            return searchJobsRankedByPython(company, region, jobCategory, conditions, pageable, pythonFitScores, totalCount);
        }

        // 파이썬 점수가 없으면(성향 퀴즈 미완료·엔진 실패·비-FIT 정렬) 최신순/마감임박순과 동일하게 SQL로 조회한다.
        // 이 경우 fitScore 배지는 산출 근거가 없으므로 항상 null(§3.3 — 산출 불가 시 배지 생략).
        List<Job> jobs = queryFactory
                .selectFrom(job)
                .leftJoin(job.company, company).fetchJoin()
                .leftJoin(job.region, region).fetchJoin()
                .leftJoin(job.jobCategory, jobCategory).fetchJoin()
                .where(conditions)
                .orderBy(orderBy(request))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<JobSummaryResponse> content = jobs.stream()
                .map(JobSummaryResponse::from)
                .toList();

        return new PageImpl<>(content, pageable, totalCount);
    }

    /**
     * 파이썬 매칭 엔진 점수(externalId → 0~100) 기준 정렬 경로.
     * 엔진이 필터링·페이징을 모르므로, 필터를 통과한 공고를 전부 가져와 자바 메모리에서 정렬 후 페이지를 자른다.
     * 엔진 점수가 없는 공고(엔진이 후보로 안 준 경우)는 맨 뒤로 보낸다.
     */
    private Page<JobSummaryResponse> searchJobsRankedByPython(QCompany company, QRegion region, QJobCategory jobCategory,
                                                                BooleanExpression[] conditions, Pageable pageable,
                                                                Map<Long, Integer> pythonFitScores, long totalCount) {
        List<Job> allJobs = queryFactory
                .selectFrom(job)
                .leftJoin(job.company, company).fetchJoin()
                .leftJoin(job.region, region).fetchJoin()
                .leftJoin(job.jobCategory, jobCategory).fetchJoin()
                .where(conditions)
                .fetch();

        Comparator<Job> byPythonScoreThenLatest = Comparator
                .comparing((Job j) -> pythonFitScores.getOrDefault(j.getExternalId(), -1), Comparator.reverseOrder())
                .thenComparing(j -> j.getPostedAt() == null ? OffsetDateTime.MIN : j.getPostedAt(), Comparator.reverseOrder());

        List<Job> sorted = allJobs.stream().sorted(byPythonScoreThenLatest).toList();

        int fromIndex = Math.min((int) pageable.getOffset(), sorted.size());
        int toIndex = Math.min(fromIndex + pageable.getPageSize(), sorted.size());

        List<JobSummaryResponse> content = sorted.subList(fromIndex, toIndex).stream()
                .map(j -> JobSummaryResponse.from(j, pythonFitScores.get(j.getExternalId())))
                .toList();

        return new PageImpl<>(content, pageable, totalCount);
    }

    /**
     * 후보 유니버스(화면설계서 EXP §2.1) — ACTIVE 이면서 미마감 또는 상시채용인 공고만.
     */
    private BooleanExpression candidateUniverse(OffsetDateTime now) {
        return job.status.eq(JobStatus.ACTIVE)
                .and(job.deadlineAt.isNull().or(job.deadlineAt.gt(now)));
    }

    /**
     * "상시채용 포함" off → 마감일이 있는 공고만 남긴다(§4.2).
     */
    private BooleanExpression alwaysOpenIncluded(JobSearchRequest request) {
        return request.includeAlwaysOpenOrDefault() ? null : job.deadlineAt.isNotNull();
    }

    /**
     * 키워드 — 공고명 · 회사명 · 스킬 태그 부분 매칭(§3.2 ②).
     */
    private BooleanExpression keywordContains(QCompany company, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        BooleanExpression skillTagMatches = Expressions.booleanTemplate(
                "array_to_string({0}, ',') ilike {1}", job.skillTags, "%" + keyword + "%");

        return job.title.containsIgnoreCase(keyword)
                .or(company.name.containsIgnoreCase(keyword))
                .or(skillTagMatches);
    }

    /**
     * 세부직군 — category_child 배열에 선택값이 하나라도 포함되면 통과(§4.2).
     */
    private BooleanExpression categoryChildrenIn(List<String> categoryChildren) {
        if (CollectionUtils.isEmpty(categoryChildren)) {
            return null;
        }
        // PostgreSQL 에는 array_contains 가 없다. 배열에 값이 있으면 첨자를, 없으면 NULL 을 돌려주는
        // array_position 으로 판정한다(= ANY 는 HQL 이 부질의 한정자로 해석해 파싱이 깨진다).
        return categoryChildren.stream()
                .filter(StringUtils::hasText)
                .map(value -> (BooleanExpression) Expressions.booleanTemplate(
                        "array_position({0}, {1}) is not null", job.categoryChild, value))
                .reduce(BooleanExpression::or)
                .orElse(null);
    }

    /** 지역 시·도 필터 — 공고의 locationCity 와 대조한다(§4.2). */
    private BooleanExpression citiesIn(List<String> cities) {
        List<String> values = nonBlank(cities);
        return values.isEmpty() ? null : job.locationCity.in(values);
    }

    private BooleanExpression districtsIn(List<String> districts) {
        List<String> values = nonBlank(districts);
        return values.isEmpty() ? null : job.locationDistrict.in(values);
    }

    /**
     * 빈 문자열 항목을 걸러낸다.
     *
     * <p>{@code ?regions=} 처럼 값 없이 넘어온 항목을 그대로 두면 {@code IN ('')} 이 만들어져
     * 일치하는 공고가 없어지고, 필터를 안 건 것처럼 보이는 대신 결과가 0건이 된다.
     * 빈 항목만 들어온 경우는 필터를 걸지 않은 것으로 본다(categoryChildrenIn 과 동일한 정책).
     */
    private List<String> nonBlank(List<String> values) {
        if (CollectionUtils.isEmpty(values)) {
            return List.of();
        }
        return values.stream().filter(StringUtils::hasText).toList();
    }

    /**
     * 경력 구간 — 선택 구간들의 OR(§4.2).
     */
    private BooleanExpression careerRangesIn(List<CareerRange> careerRanges) {
        if (CollectionUtils.isEmpty(careerRanges)) {
            return null;
        }
        return careerRanges.stream()
                .map(this::careerRangeCondition)
                .reduce(BooleanExpression::or)
                .orElse(null);
    }

    private BooleanExpression careerRangeCondition(CareerRange range) {
        return switch (range) {
            case NEWCOMER -> job.isNewbie.isTrue().or(job.careerMin.eq(0));
            case JUNIOR -> job.careerMin.loe(3).and(job.careerMax.goe(1));
            case SENIOR -> job.careerMax.goe(4);
        };
    }

    private BooleanExpression employmentTypesIn(List<String> employmentTypes) {
        List<String> values = nonBlank(employmentTypes);
        return values.isEmpty() ? null : job.employmentType.in(values);
    }

    private BooleanExpression remoteOnly(JobSearchRequest request) {
        return request.remoteOnlyOrFalse() ? job.remoteAvailable.isTrue() : null;
    }

    /**
     * 정렬(§3.4). 상시채용(마감일 NULL)은 마감임박순에서 항상 뒤로 보낸다.
     * 추천순(FIT)은 파이썬 매칭 엔진 점수로만 랭킹하며(searchJobsRankedByPython), 그 점수가 없을 때는
     * 여기서 최신순과 동일하게 처리한다(§2.1 — 후보 범위는 안 좁히고 랭킹에만 반영).
     */
    private OrderSpecifier<?>[] orderBy(JobSearchRequest request) {
        OrderSpecifier<?> latest = job.postedAt.desc().nullsLast();

        return switch (request.sortOrDefault()) {
            case DEADLINE -> new OrderSpecifier<?>[]{
                    job.deadlineAt.asc().nullsLast(), latest, job.id.desc()
            };
            case RECENT, FIT -> new OrderSpecifier<?>[]{latest, job.id.desc()};
        };
    }
}
