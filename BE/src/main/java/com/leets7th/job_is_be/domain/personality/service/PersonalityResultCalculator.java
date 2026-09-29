package com.leets7th.job_is_be.domain.personality.service;

import com.leets7th.job_is_be.domain.personality.enums.PersonalityAxis;
import com.leets7th.job_is_be.domain.personality.enums.PersonalityResultType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class PersonalityResultCalculator {

    private final PersonalityQuestionCatalog questionCatalog;

    public PersonalityResultCalculator(PersonalityQuestionCatalog questionCatalog) {
        this.questionCatalog = questionCatalog;
    }

    public Result calculate(Map<Integer, Integer> answers) {
        int scoreA = score(answers, PersonalityAxis.STABILITY_CHALLENGE);
        int scoreB = score(answers, PersonalityAxis.BALANCE_IMMERSION);
        int scoreC = score(answers, PersonalityAxis.EXPERT_ALL_ROUNDER);

        PersonalityResultType type = resolveType(scoreA, scoreB);
        List<String> tags = new ArrayList<>(type.getFixedTags());
        if (scoreC < 0) {
            tags.add("전문가형");
        } else if (scoreC > 0) {
            tags.add("올라운더형");
        }

        return new Result(scoreA, scoreB, scoreC, type, List.copyOf(tags));
    }

    private int score(Map<Integer, Integer> answers, PersonalityAxis axis) {
        return answers.entrySet().stream()
                .filter(entry -> questionCatalog.find(entry.getKey())
                        .map(question -> question.axis() == axis)
                        .orElse(false))
                .mapToInt(entry -> entry.getValue() == 1 ? -1 : 1)
                .sum();
    }

    private PersonalityResultType resolveType(int scoreA, int scoreB) {
        boolean stable = scoreA < 0;
        boolean balance = scoreB < 0;
        if (stable && balance) {
            return PersonalityResultType.SB;
        }
        if (stable) {
            return PersonalityResultType.SF;
        }
        if (balance) {
            return PersonalityResultType.CB;
        }
        return PersonalityResultType.CF;
    }

    public record Result(
            int stabilityChallenge,
            int balanceImmersion,
            int expertAllRounder,
            PersonalityResultType type,
            List<String> tags
    ) {
    }
}
