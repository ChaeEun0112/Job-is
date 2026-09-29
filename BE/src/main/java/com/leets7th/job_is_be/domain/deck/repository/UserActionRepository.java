package com.leets7th.job_is_be.domain.deck.repository;

import com.leets7th.job_is_be.domain.deck.entity.UserAction;
import com.leets7th.job_is_be.domain.deck.enums.ActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface UserActionRepository extends JpaRepository<UserAction, Long> {

    boolean existsByUserIdAndJobIdAndActionType(Long userId, Long jobId, ActionType actionType);

    void deleteByUserIdAndJobIdAndActionType(Long userId, Long jobId, ActionType actionType);

    @Query("""
            SELECT ua.job.id FROM UserAction ua
            WHERE ua.user.id = :userId
            AND ua.job.id IN :jobIds
            AND ua.actionType = :actionType
            """)
    List<Long> findJobIdsByUserIdAndJobIdInAndActionType(
            @Param("userId") Long userId,
            @Param("jobIds") Collection<Long> jobIds,
            @Param("actionType") ActionType actionType);

    /**
     * 같은 (user, job)에 여러 종류의 액션이 쌓일 수 있어(예: 지원의향 클릭 후 저장),
     * job별로 가장 최근(id 최대) 이벤트 한 건만 히스토리 항목으로 노출한다.
     */
    @Query(value = """
            SELECT ua FROM UserAction ua
            JOIN FETCH ua.job j
            LEFT JOIN FETCH j.company
            WHERE ua.user.id = :userId
            AND ua.actionType IN :actionTypes
            AND ua.id = (
                SELECT MAX(ua2.id) FROM UserAction ua2
                WHERE ua2.user.id = ua.user.id
                AND ua2.job.id = ua.job.id
                AND ua2.actionType IN :actionTypes
            )
            ORDER BY ua.createdAt DESC
            """,
            countQuery = """
            SELECT COUNT(ua) FROM UserAction ua
            WHERE ua.user.id = :userId
            AND ua.actionType IN :actionTypes
            AND ua.id = (
                SELECT MAX(ua2.id) FROM UserAction ua2
                WHERE ua2.user.id = ua.user.id
                AND ua2.job.id = ua.job.id
                AND ua2.actionType IN :actionTypes
            )
            """)
    Page<UserAction> findLatestByUserIdAndActionTypeIn(
            @Param("userId") Long userId,
            @Param("actionTypes") Collection<ActionType> actionTypes,
            Pageable pageable);
}
