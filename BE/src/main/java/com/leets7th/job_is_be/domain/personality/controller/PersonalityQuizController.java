package com.leets7th.job_is_be.domain.personality.controller;

import com.leets7th.job_is_be.domain.personality.controller.docs.PersonalityQuizControllerDocs;
import com.leets7th.job_is_be.domain.personality.dto.QuizAnswerRequest;
import com.leets7th.job_is_be.domain.personality.dto.QuizAnswerResponse;
import com.leets7th.job_is_be.domain.personality.dto.QuizApplyRequest;
import com.leets7th.job_is_be.domain.personality.dto.QuizApplyResponse;
import com.leets7th.job_is_be.domain.personality.dto.QuizQuestionsResponse;
import com.leets7th.job_is_be.domain.personality.dto.QuizResultResponse;
import com.leets7th.job_is_be.domain.personality.enums.PersonalityTestSource;
import com.leets7th.job_is_be.domain.personality.service.PersonalityQuizService;
import com.leets7th.job_is_be.global.response.ApiResponse;
import com.leets7th.job_is_be.global.status.SuccessStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quiz")
public class PersonalityQuizController implements PersonalityQuizControllerDocs {

    private final PersonalityQuizService quizService;

    public PersonalityQuizController(PersonalityQuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/questions")
    public ResponseEntity<ApiResponse<QuizQuestionsResponse>> getQuestions(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "ONBOARDING") PersonalityTestSource source
    ) {
        return ApiResponse.success(
                SuccessStatus.QUIZ_QUESTIONS_GET_SUCCESS,
                quizService.getQuestions(userId(jwt), source)
        );
    }

    @PostMapping("/answers")
    public ResponseEntity<ApiResponse<QuizAnswerResponse>> saveAnswer(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody QuizAnswerRequest request
    ) {
        return ApiResponse.success(
                SuccessStatus.QUIZ_ANSWER_SAVE_SUCCESS,
                quizService.saveAnswer(userId(jwt), request)
        );
    }

    @GetMapping("/result")
    public ResponseEntity<ApiResponse<QuizResultResponse>> getResult(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Long testId
    ) {
        return ApiResponse.success(
                SuccessStatus.QUIZ_RESULT_GET_SUCCESS,
                quizService.getResult(userId(jwt), testId)
        );
    }

    @PostMapping("/result/apply")
    public ResponseEntity<ApiResponse<QuizApplyResponse>> applyResult(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody QuizApplyRequest request
    ) {
        return ApiResponse.success(
                SuccessStatus.QUIZ_RESULT_APPLY_SUCCESS,
                quizService.applyResult(userId(jwt), request)
        );
    }

    private Long userId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
