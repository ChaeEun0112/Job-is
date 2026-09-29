package com.leets7th.job_is_be.domain.deck.repository;

import com.leets7th.job_is_be.domain.deck.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {

    @Query("SELECT c FROM Card c LEFT JOIN FETCH c.job j LEFT JOIN FETCH j.jobCategory " +
            "LEFT JOIN FETCH j.company LEFT JOIN FETCH j.region " +
            "WHERE c.deck.id = :deckId ORDER BY c.position ASC")
    List<Card> findByDeckId(@Param("deckId") Long deckId);

    /**
     * 사용자의 덱에 담긴 적이 있는 공고인지 조회한다. 상세 조회에서 확정 점수를 재사용하기 위한 용도로,
     * 같은 공고가 여러 날 덱에 있었다면 최신 덱 기준 1건을 쓴다.
     */
    @Query("SELECT c FROM Card c WHERE c.deck.user.id = :userId AND c.job.id = :jobId "
            + "ORDER BY c.deck.deckDate DESC, c.id DESC")
    List<Card> findByUserIdAndJobId(@Param("userId") Long userId, @Param("jobId") Long jobId);
}
