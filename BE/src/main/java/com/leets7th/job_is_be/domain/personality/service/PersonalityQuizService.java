package com.leets7th.job_is_be.domain.personality.service;

import com.leets7th.job_is_be.domain.personality.dto.QuizAnswerRequest;
import com.leets7th.job_is_be.domain.personality.dto.QuizAnswerResponse;
import com.leets7th.job_is_be.domain.personality.dto.QuizApplyRequest;
import com.leets7th.job_is_be.domain.personality.dto.QuizApplyResponse;
import com.leets7th.job_is_be.domain.personality.dto.QuizQuestionsResponse;
import com.leets7th.job_is_be.domain.personality.dto.QuizResultResponse;
import com.leets7th.job_is_be.domain.personality.entity.PersonalityTest;
import com.leets7th.job_is_be.domain.personality.entity.PersonalityTestAnswer;
import com.leets7th.job_is_be.domain.personality.enums.PersonalityResultType;
import com.leets7th.job_is_be.domain.personality.enums.PersonalityTestSource;
import com.leets7th.job_is_be.domain.personality.repository.PersonalityTestAnswerRepository;
import com.leets7th.job_is_be.domain.personality.repository.PersonalityTestRepository;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.entity.UserProfile;
import com.leets7th.job_is_be.domain.user.enums.OnboardingStep;
import com.leets7th.job_is_be.domain.user.repository.UserProfileRepository;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PersonalityQuizService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PersonalityTestRepository testRepository;
    private final PersonalityTestAnswerRepository answerRepository;
    private final PersonalityQuestionCatalog questionCatalog;
    private final PersonalityResultCalculator resultCalculator;
    private final PersonalityTagCodec tagCodec;

    public PersonalityQuizService(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            PersonalityTestRepository testRepository,
            PersonalityTestAnswerRepository answerRepository,
            PersonalityQuestionCatalog questionCatalog,
            PersonalityResultCalculator resultCalculator,
            PersonalityTagCodec tagCodec
    ) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.testRepository = testRepository;
        this.answerRepository = answerRepository;
        this.questionCatalog = questionCatalog;
        this.resultCalculator = resultCalculator;
        this.tagCodec = tagCodec;
    }

    @Transactional
    public QuizQuestionsResponse getQuestions(Long userId, PersonalityTestSource source) {
        if (source == null) {
            throw new GeneralException(ErrorStatus.QUIZ_REQUEST_INVALID);
        }
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        PersonalityTest test = testRepository
                .findFirstByUserIdAndSourceAndCompletedFalseOrderByStartedAtDesc(userId, source)
                .orElseGet(() -> testRepository.save(PersonalityTest.builder()
                        .user(user)
                        .source(source)
                        .startedAt(OffsetDateTime.now())
                        .build()));
        Map<Integer, PersonalityTestAnswer> answers = answersByQuestion(test.getId());

        List<QuizQuestionsResponse.Question> questions = questionCatalog.shuffled(test.getId()).stream()
                .map(question -> new QuizQuestionsResponse.Question(
                        question.questionNo(),
                        question.content(),
                        List.of(
                                new QuizQuestionsResponse.Choice(1, question.choiceOne()),
                                new QuizQuestionsResponse.Choice(2, question.choiceTwo())
                        ),
                        selectedChoice(answers.get(question.questionNo()))
                ))
                .toList();

        return new QuizQuestionsResponse(
                test.getId(),
                test.getSource(),
                test.isCompleted(),
                answers.size(),
                questionCatalog.size(),
                questions
        );
    }

    @Transactional
    public QuizAnswerResponse saveAnswer(Long userId, QuizAnswerRequest request) {
        validateAnswerRequest(request);
        PersonalityTest test = findOwnedTestForUpdate(userId, request.testId());
        if (test.isCompleted()) {
            throw new GeneralException(ErrorStatus.QUIZ_TEST_ALREADY_COMPLETED);
        }
        questionCatalog.find(request.questionNo())
                .orElseThrow(() -> new GeneralException(ErrorStatus.QUIZ_QUESTION_INVALID));

        OffsetDateTime now = OffsetDateTime.now();
        PersonalityTestAnswer answer = answerRepository
                .findByTestIdAndQuestionNo(test.getId(), request.questionNo())
                .orElseGet(() -> PersonalityTestAnswer.builder()
                        .test(test)
                        .questionNo(request.questionNo())
                        .choiceValue(request.choiceValue().toString())
                        .answeredAt(now)
                        .build());
        answer.updateChoice(request.choiceValue().toString(), now);
        answerRepository.saveAndFlush(answer);

        int answeredCount = Math.toIntExact(answerRepository.countByTestId(test.getId()));
        if (answeredCount == questionCatalog.size()) {
            PersonalityResultCalculator.Result result = calculate(test.getId());
            test.complete(result.type(), tagCodec.encode(result.tags()), OffsetDateTime.now());
        }

        return new QuizAnswerResponse(
                test.getId(),
                request.questionNo(),
                request.choiceValue(),
                answeredCount,
                questionCatalog.size(),
                test.isCompleted()
        );
    }

    @Transactional(readOnly = true)
    public QuizResultResponse getResult(Long userId, Long testId) {
        PersonalityTest test = findOwnedTest(userId, testId);
        int answeredCount = Math.toIntExact(answerRepository.countByTestId(test.getId()));
        if (!test.isCompleted()) {
            return new QuizResultResponse(
                    test.getId(),
                    false,
                    answeredCount,
                    questionCatalog.size(),
                    null,
                    null,
                    List.of()
            );
        }

        PersonalityResultCalculator.Result result = calculate(test.getId());
        return completedResult(test, answeredCount, result);
    }

    @Transactional
    public QuizApplyResponse applyResult(Long userId, QuizApplyRequest request) {
        if (request == null || request.testId() == null) {
            throw new GeneralException(ErrorStatus.QUIZ_REQUEST_INVALID);
        }
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        PersonalityTest test = findOwnedTestForUpdate(userId, request.testId());
        if (!test.isCompleted()) {
            throw new GeneralException(ErrorStatus.QUIZ_TEST_INCOMPLETE);
        }
        UserProfile profile = findOrCreateProfileForApply(user, test);

        PersonalityResultCalculator.Result calculatedResult = calculate(test.getId());
        PersonalityResultType resultType = resolveResultType(test, calculatedResult);
        List<String> tags = resolveResultTags(test, calculatedResult);
        profile.applyPersonalityTags(tagCodec.encode(tags), OffsetDateTime.now());
        if (test.getSource() == PersonalityTestSource.ONBOARDING
                && profile.getOnboardingStep() == OnboardingStep.QUIZ) {
            profile.moveOnboardingStep(OnboardingStep.REVIEW);
        }

        return new QuizApplyResponse(
                test.getId(),
                resultType.name(),
                tags,
                profile.isJobTestCompleted(),
                true
        );
    }

    private UserProfile findOrCreateProfileForApply(User user, PersonalityTest test) {
        return userProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    if (test.getSource() != PersonalityTestSource.ONBOARDING) {
                        throw new GeneralException(ErrorStatus.PROFILE_NOT_FOUND);
                    }
                    return userProfileRepository.save(UserProfile.builder()
                            .user(user)
                            .onboardingStep(OnboardingStep.QUIZ)
                            .build());
                });
    }

    private void validateAnswerRequest(QuizAnswerRequest request) {
        if (request == null || request.testId() == null || request.questionNo() == null) {
            throw new GeneralException(ErrorStatus.QUIZ_REQUEST_INVALID);
        }
        if (request.choiceValue() == null
                || (request.choiceValue() != 1 && request.choiceValue() != 2)) {
            throw new GeneralException(ErrorStatus.QUIZ_CHOICE_INVALID);
        }
    }

    private PersonalityTest findOwnedTest(Long userId, Long testId) {
        if (testId == null) {
            throw new GeneralException(ErrorStatus.QUIZ_REQUEST_INVALID);
        }
        return testRepository.findById(testId)
                .filter(test -> test.getUser().getId().equals(userId))
                .orElseThrow(() -> new GeneralException(ErrorStatus.QUIZ_TEST_NOT_FOUND));
    }

    private PersonalityTest findOwnedTestForUpdate(Long userId, Long testId) {
        if (testId == null) {
            throw new GeneralException(ErrorStatus.QUIZ_REQUEST_INVALID);
        }
        return testRepository.findByIdForUpdate(testId)
                .filter(test -> test.getUser().getId().equals(userId))
                .orElseThrow(() -> new GeneralException(ErrorStatus.QUIZ_TEST_NOT_FOUND));
    }

    private Map<Integer, PersonalityTestAnswer> answersByQuestion(Long testId) {
        return answerRepository.findAllByTestIdOrderByQuestionNoAsc(testId).stream()
                .collect(Collectors.toMap(
                        PersonalityTestAnswer::getQuestionNo,
                        Function.identity()
                ));
    }

    private PersonalityResultCalculator.Result calculate(Long testId) {
        Map<Integer, Integer> answers = answerRepository
                .findAllByTestIdOrderByQuestionNoAsc(testId).stream()
                .collect(Collectors.toMap(
                        PersonalityTestAnswer::getQuestionNo,
                        answer -> Integer.valueOf(answer.getChoiceValue())
                ));
        return resultCalculator.calculate(answers);
    }

    private QuizResultResponse completedResult(
            PersonalityTest test,
            int answeredCount,
            PersonalityResultCalculator.Result result
    ) {
        PersonalityResultType type = resolveResultType(test, result);
        return new QuizResultResponse(
                test.getId(),
                true,
                answeredCount,
                questionCatalog.size(),
                new QuizResultResponse.Scores(
                        result.stabilityChallenge(),
                        result.balanceImmersion(),
                        result.expertAllRounder()
                ),
                new QuizResultResponse.ResultType(
                        type.name(),
                        type.getDisplayName(),
                        type.getSummary()
                ),
                resolveResultTags(test, result)
        );
    }

    private PersonalityResultType resolveResultType(
            PersonalityTest test,
            PersonalityResultCalculator.Result calculatedResult
    ) {
        return test.getResultType() == null
                ? calculatedResult.type()
                : test.getResultType();
    }

    private List<String> resolveResultTags(
            PersonalityTest test,
            PersonalityResultCalculator.Result calculatedResult
    ) {
        List<String> storedTags = tagCodec.decode(test.getResultTags());
        return storedTags.isEmpty() ? calculatedResult.tags() : storedTags;
    }

    private Integer selectedChoice(PersonalityTestAnswer answer) {
        return answer == null ? null : Integer.valueOf(answer.getChoiceValue());
    }
}
