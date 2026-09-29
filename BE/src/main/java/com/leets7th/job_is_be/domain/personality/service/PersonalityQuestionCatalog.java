package com.leets7th.job_is_be.domain.personality.service;

import com.leets7th.job_is_be.domain.personality.enums.PersonalityAxis;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Component
public class PersonalityQuestionCatalog {

    private static final List<Question> QUESTIONS = List.of(
            new Question(1, PersonalityAxis.STABILITY_CHALLENGE, "새 팀에 합류한다면?",
                    "체계·매뉴얼이 잘 갖춰진 안정된 팀", "유연한 체계 아래 빠르게 크는 팀"),
            new Question(2, PersonalityAxis.STABILITY_CHALLENGE, "더 끌리는 회사는?",
                    "오래 다닐 수 있는 탄탄한 회사", "지금은 작아도 폭발 성장할 회사"),
            new Question(3, PersonalityAxis.STABILITY_CHALLENGE, "연말 회사 소식으로 더 반가운 건?",
                    "창립 30주년, 안정 경영 지속", "시리즈B 투자유치, 사업 확장"),
            new Question(4, PersonalityAxis.STABILITY_CHALLENGE, "더 견디기 힘든 상황은?",
                    "매년 조직개편·직무 변경", "몇 년째 똑같은 업무의 반복"),
            new Question(5, PersonalityAxis.BALANCE_IMMERSION, "금요일 퇴근 직전 급한 요청이 들어온다면?",
                    "월요일 아침에 처리하겠다", "오늘 끝내고 마음 편히 주말을 보낸다"),
            new Question(6, PersonalityAxis.BALANCE_IMMERSION, "더 좋은 회사는?",
                    "칼퇴·유연근무가 보장되는 회사", "바쁘지만 성과만큼 확실히 인정하는 회사"),
            new Question(7, PersonalityAxis.BALANCE_IMMERSION, "이상적인 하루는?",
                    "일과 삶의 경계가 뚜렷한 하루", "몰입해서 시간 가는 줄 모르는 하루"),
            new Question(8, PersonalityAxis.BALANCE_IMMERSION, "더 부러운 동료는?",
                    "취미·자기 시간이 확실한 동료", "큰 프로젝트를 이끄는 동료"),
            new Question(9, PersonalityAxis.EXPERT_ALL_ROUNDER, "커리어 목표에 가까운 건?",
                    "한 분야 최고의 전문가", "여러 분야를 두루 아는 제너럴리스트"),
            new Question(10, PersonalityAxis.EXPERT_ALL_ROUNDER, "더 재미있는 업무는?",
                    "내 전문 영역을 깊게 파는 업무", "새 영역을 넘나들며 다양하게 하는 업무")
    );

    public List<Question> shuffled(Long testId) {
        List<Question> shuffled = new ArrayList<>(QUESTIONS);
        Collections.shuffle(shuffled, new Random(testId));
        return List.copyOf(shuffled);
    }

    public Optional<Question> find(Integer questionNo) {
        return QUESTIONS.stream()
                .filter(question -> question.questionNo().equals(questionNo))
                .findFirst();
    }

    public int size() {
        return QUESTIONS.size();
    }

    public record Question(
            Integer questionNo,
            PersonalityAxis axis,
            String content,
            String choiceOne,
            String choiceTwo
    ) {
    }
}
