package com.leets7th.job_is_be.domain.personality.entity;


import com.leets7th.job_is_be.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 퀴즈 문항별 응답. PersonalityTest 하위 엔티티.
 */
@Entity
@Table(
        name = "personality_test_answers",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_personality_test_answers_test_question",
                columnNames = {"test_id", "question_no"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonalityTestAnswer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private PersonalityTest test;

    @Column(name = "question_no", nullable = false)
    private Integer questionNo; // 문항 번호 1~10

    @Column(name = "choice_value", nullable = false, length = 50)
    private String choiceValue; // 선택한 카드 값

    @Column(name = "answered_at", nullable = false)
    private OffsetDateTime answeredAt;

    @Builder
    public PersonalityTestAnswer(PersonalityTest test, Integer questionNo, String choiceValue, OffsetDateTime answeredAt) {
        this.test = test;
        this.questionNo = questionNo;
        this.choiceValue = choiceValue;
        this.answeredAt = answeredAt;
    }

    public void updateChoice(String choiceValue, OffsetDateTime answeredAt) {
        this.choiceValue = choiceValue;
        this.answeredAt = answeredAt;
    }
}
