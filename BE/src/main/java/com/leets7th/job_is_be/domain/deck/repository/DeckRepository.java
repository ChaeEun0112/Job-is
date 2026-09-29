package com.leets7th.job_is_be.domain.deck.repository;

import com.leets7th.job_is_be.domain.deck.entity.Deck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DeckRepository extends JpaRepository<Deck, Long> {

    Optional<Deck> findByUserIdAndDeckDate(Long userId, LocalDate deckDate);

    // 레터 회차(No.{n}) — 해당 날짜까지 사용자에게 발행된 덱 누적 개수 (REC §0.3 deck_seq)
    long countByUserIdAndDeckDateLessThanEqual(Long userId, LocalDate deckDate);
}
