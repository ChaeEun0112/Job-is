package com.leets7th.job_is_be.domain.deck.repository;

import com.leets7th.job_is_be.domain.deck.entity.UserAction;
import com.leets7th.job_is_be.domain.deck.enums.ActionType;
import com.leets7th.job_is_be.domain.job.entity.Company;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.enums.SocialType;
import com.leets7th.job_is_be.global.config.JpaConfig;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// 프로필을 지정하지 않으면 기본값(dev)으로 로컬 Postgres 에 붙어 ddl-auto=validate 로 실패한다.
// 다른 테스트와 동일하게 test 프로필(H2, create-drop)을 쓴다.
@DataJpaTest
@ActiveProfiles("test")
@Import({JpaConfig.class, UserActionRepositoryTest.QueryDslConfig.class})
class UserActionRepositoryTest {

    @TestConfiguration
    static class QueryDslConfig {
        @Bean
        JPAQueryFactory jpaQueryFactory(EntityManager em) {
            return new JPAQueryFactory(em);
        }

        // @EnableCaching이 JobIsBeApplication에 선언되어 있어 DataJpa 슬라이스에서도 로드됨
        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager();
        }
    }

    @Autowired
    private UserActionRepository userActionRepository;

    @Autowired
    private TestEntityManager em;

    // ─── 픽스처 헬퍼 ──────────────────────────────────────────────────────

    private User persistUser(String suffix) {
        return em.persistAndFlush(User.builder()
                .socialId("social-" + suffix)
                .socialType(SocialType.KAKAO)
                .email(suffix + "@test.com")
                .build());
    }

    private Job persistJob(String title) {
        // CascadeType.PERSIST on Job.company → Company도 함께 저장
        return em.persistAndFlush(Job.builder()
                .company(Company.builder().name("테스트 기업").build())
                .title(title)
                .build());
    }

    private UserAction persistAction(User user, Job job, ActionType type) {
        return em.persistAndFlush(UserAction.builder()
                .user(user)
                .job(job)
                .actionType(type)
                .build());
    }

    // ─── 1. 중복 액션 최신 선택 ────────────────────────────────────────────

    @Test
    void 동일_공고_복수_액션_중_id_최대값_하나만_반환한다() {
        // MAX(id) 기준이므로 나중에 저장된 SAVED가 선택되어야 한다
        User user = persistUser("dup-latest");
        Job job = persistJob("백엔드 개발자");
        persistAction(user, job, ActionType.VIEWED);                    // 이전 액션 (id 작음)
        UserAction latest = persistAction(user, job, ActionType.SAVED); // 최신 액션 (id 큼)

        Page<UserAction> page = userActionRepository.findLatestByUserIdAndActionTypeIn(
                user.getId(),
                List.of(ActionType.VIEWED, ActionType.SAVED),
                PageRequest.of(0, 10)
        );

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo(latest.getId());
    }

    @Test
    void 필터_밖_액션은_subquery_최대값_계산에서_제외된다() {
        // VIEWED(id=N) → DISMISSED(id=N+1): 필터가 VIEWED뿐이면
        // subquery MAX(id WHERE type IN [VIEWED]) = N이므로 VIEWED가 반환된다
        User user = persistUser("filter-out");
        Job job = persistJob("프론트엔드 개발자");
        UserAction viewed = persistAction(user, job, ActionType.VIEWED);
        persistAction(user, job, ActionType.DISMISSED); // 필터 밖 → subquery 집계 대상 아님

        Page<UserAction> page = userActionRepository.findLatestByUserIdAndActionTypeIn(
                user.getId(),
                List.of(ActionType.VIEWED),
                PageRequest.of(0, 10)
        );

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo(viewed.getId());
    }

    @Test
    void 공고별_독립적으로_최신_액션이_선택된다() {
        User user = persistUser("per-job");
        Job jobA = persistJob("공고A");
        Job jobB = persistJob("공고B");

        persistAction(user, jobA, ActionType.VIEWED);                      // jobA 이전
        UserAction latestA = persistAction(user, jobA, ActionType.SAVED);  // jobA 최신
        UserAction latestB = persistAction(user, jobB, ActionType.VIEWED); // jobB 유일

        Page<UserAction> page = userActionRepository.findLatestByUserIdAndActionTypeIn(
                user.getId(),
                List.of(ActionType.VIEWED, ActionType.SAVED),
                PageRequest.of(0, 10)
        );

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent())
                .extracting(UserAction::getId)
                .containsExactlyInAnyOrder(latestA.getId(), latestB.getId());
    }

    // ─── 2. 사용자 격리 ────────────────────────────────────────────────────

    @Test
    void 다른_사용자_액션은_조회되지_않는다() {
        User user1 = persistUser("iso-u1");
        User user2 = persistUser("iso-u2");
        Job job = persistJob("공통 공고");
        persistAction(user2, job, ActionType.VIEWED);
        UserAction user1Only = persistAction(user1, job, ActionType.SAVED);

        Page<UserAction> page = userActionRepository.findLatestByUserIdAndActionTypeIn(
                user1.getId(),
                List.of(ActionType.VIEWED, ActionType.SAVED),
                PageRequest.of(0, 10)
        );

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo(user1Only.getId());
    }

    // ─── 3. countQuery 검증 ────────────────────────────────────────────────

    @Test
    void countQuery가_중복_제거된_공고_수를_반환한다() {
        // 공고 5개, 각각 VIEWED→SAVED 2건 적재 → deduplicated count = 5
        User user = persistUser("count");
        for (int i = 0; i < 5; i++) {
            Job job = persistJob("공고" + i);
            persistAction(user, job, ActionType.VIEWED);
            persistAction(user, job, ActionType.SAVED);
        }

        Page<UserAction> page = userActionRepository.findLatestByUserIdAndActionTypeIn(
                user.getId(),
                List.of(ActionType.VIEWED, ActionType.SAVED),
                PageRequest.of(0, 3)  // 5개 중 3개만 페이지에 담음
        );

        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(3);
    }

    @Test
    void 마지막_페이지는_나머지_항목만_반환한다() {
        User user = persistUser("page-tail");
        for (int i = 0; i < 5; i++) {
            persistAction(user, persistJob("공고" + i), ActionType.VIEWED);
        }

        Page<UserAction> lastPage = userActionRepository.findLatestByUserIdAndActionTypeIn(
                user.getId(),
                List.of(ActionType.VIEWED),
                PageRequest.of(1, 3)
        );

        assertThat(lastPage.getContent()).hasSize(2);
        assertThat(lastPage.isLast()).isTrue();
    }

    // ─── 4. fetch join 검증 ────────────────────────────────────────────────

    @Test
    void fetchJoin으로_job과_company가_분리된_컨텍스트에서도_접근된다() {
        // JOIN FETCH ua.job / LEFT JOIN FETCH j.company 가 적용되면,
        // 조회 결과의 job·company는 프록시가 아닌 실제 객체로 채워진다.
        // em.clear() 후 detached 상태에서 접근해도 LazyInitializationException이 발생하지 않아야 한다.
        User user = persistUser("fetch-join");
        Job job = em.persistAndFlush(Job.builder()
                .company(Company.builder().name("페치조인기업").build())
                .title("개발자 채용")
                .build());
        persistAction(user, job, ActionType.VIEWED);
        em.clear(); // 쿼리 전 컨텍스트 초기화 → 캐시 없이 DB에서 로드하도록 강제

        Page<UserAction> page = userActionRepository.findLatestByUserIdAndActionTypeIn(
                user.getId(),
                List.of(ActionType.VIEWED),
                PageRequest.of(0, 10)
        );
        em.clear(); // 조회 결과를 영속성 컨텍스트에서 분리 → 프록시였다면 LazyInitializationException 발생

        UserAction ua = page.getContent().get(0);
        assertThat(ua.getJob().getTitle()).isEqualTo("개발자 채용");
        assertThat(ua.getJob().getCompany().getName()).isEqualTo("페치조인기업");
    }
}
