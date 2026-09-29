package com.leets7th.job_is_be.domain.personality.service;

import com.leets7th.job_is_be.domain.personality.enums.PersonalityResultType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PersonalityResultCalculatorTest {

    private PersonalityResultCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PersonalityResultCalculator(new PersonalityQuestionCatalog());
    }

    @Test
    void calculatesSbTypeAndExpertTag() {
        PersonalityResultCalculator.Result result = calculator.calculate(allChoices(1));

        assertThat(result.type()).isEqualTo(PersonalityResultType.SB);
        assertThat(result.stabilityChallenge()).isEqualTo(-4);
        assertThat(result.balanceImmersion()).isEqualTo(-4);
        assertThat(result.expertAllRounder()).isEqualTo(-2);
        assertThat(result.tags()).containsExactly("안정지향", "워라밸", "전문가형");
    }

    @Test
    void calculatesSfAndCbTypes() {
        Map<Integer, Integer> sfAnswers = allChoices(2);
        for (int questionNo = 1; questionNo <= 4; questionNo++) {
            sfAnswers.put(questionNo, 1);
        }
        sfAnswers.put(9, 1);
        PersonalityResultCalculator.Result sf = calculator.calculate(sfAnswers);

        assertThat(sf.type()).isEqualTo(PersonalityResultType.SF);
        assertThat(sf.tags()).containsExactly("안정지향", "성과지향");

        Map<Integer, Integer> cbAnswers = allChoices(2);
        for (int questionNo = 5; questionNo <= 8; questionNo++) {
            cbAnswers.put(questionNo, 1);
        }
        PersonalityResultCalculator.Result cb = calculator.calculate(cbAnswers);

        assertThat(cb.type()).isEqualTo(PersonalityResultType.CB);
        assertThat(cb.tags()).containsExactly("도전지향", "워라밸", "올라운더형");
    }

    @Test
    void defaultsTiesToChallengeAndImmersionWithoutCTag() {
        Map<Integer, Integer> answers = allChoices(2);
        answers.put(1, 1);
        answers.put(2, 1);
        answers.put(5, 1);
        answers.put(6, 1);
        answers.put(9, 1);

        PersonalityResultCalculator.Result result = calculator.calculate(answers);

        assertThat(result.type()).isEqualTo(PersonalityResultType.CF);
        assertThat(result.stabilityChallenge()).isZero();
        assertThat(result.balanceImmersion()).isZero();
        assertThat(result.expertAllRounder()).isZero();
        assertThat(result.tags()).containsExactly("도전지향", "성장지향");
    }

    private Map<Integer, Integer> allChoices(int choiceValue) {
        Map<Integer, Integer> answers = new LinkedHashMap<>();
        for (int questionNo = 1; questionNo <= 10; questionNo++) {
            answers.put(questionNo, choiceValue);
        }
        return answers;
    }
}
