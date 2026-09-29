package com.leets7th.job_is_be.domain.user.repository;

import com.leets7th.job_is_be.domain.user.entity.UserRegion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRegionRepository extends JpaRepository<UserRegion, Long> {

    // 희망 지역은 사용자당 1건. 과거 다중 저장분이 남아 있어도 최신 1건만 사용한다.
    Optional<UserRegion> findFirstByUserIdOrderByIdDesc(Long userId);

    void deleteAllByUserId(Long userId);
}
