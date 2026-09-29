package com.leets7th.job_is_be.domain.personality.repository;

import com.leets7th.job_is_be.domain.personality.entity.PersonalityTestAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonalityTestAnswerRepository extends JpaRepository<PersonalityTestAnswer, Long> {

    Optional<PersonalityTestAnswer> findByTestIdAndQuestionNo(Long testId, Integer questionNo);

    List<PersonalityTestAnswer> findAllByTestIdOrderByQuestionNoAsc(Long testId);

    long countByTestId(Long testId);
}
