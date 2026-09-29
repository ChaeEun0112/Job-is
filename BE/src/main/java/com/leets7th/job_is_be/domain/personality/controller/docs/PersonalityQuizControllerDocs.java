package com.leets7th.job_is_be.domain.personality.controller.docs;

import com.leets7th.job_is_be.domain.personality.dto.QuizAnswerRequest;
import com.leets7th.job_is_be.domain.personality.dto.QuizAnswerResponse;
import com.leets7th.job_is_be.domain.personality.dto.QuizApplyRequest;
import com.leets7th.job_is_be.domain.personality.dto.QuizApplyResponse;
import com.leets7th.job_is_be.domain.personality.dto.QuizQuestionsResponse;
import com.leets7th.job_is_be.domain.personality.dto.QuizResultResponse;
import com.leets7th.job_is_be.domain.personality.enums.PersonalityTestSource;
import com.leets7th.job_is_be.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

@Tag(name = "PersonalityQuiz", description = "성향 진단 퀴즈 문항/응답/결과 API")
@SecurityRequirement(name = "JWT")
public interface PersonalityQuizControllerDocs {

    @Operation(
            summary = "퀴즈 문항 조회",
            description = "성향 진단 퀴즈 문항 목록을 조회합니다. 사용자는 Access Token(JWT)으로 식별됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<QuizQuestionsResponse>> getQuestions(
            @Parameter(hidden = true) Jwt jwt,
            @Parameter(description = "진단 진입 출처 (기본값: ONBOARDING)", example = "ONBOARDING") PersonalityTestSource source
    );

    @Operation(summary = "퀴즈 응답 저장", description = "사용자가 선택한 퀴즈 답변을 저장합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "저장 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 바디 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<QuizAnswerResponse>> saveAnswer(
            @Parameter(hidden = true) Jwt jwt,
            @RequestBody(required = true, description = "퀴즈 응답 저장 요청")
            QuizAnswerRequest request
    );

    @Operation(summary = "퀴즈 결과 조회", description = "지정한 진단 회차(testId)의 성향 진단 결과를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<QuizResultResponse>> getResult(
            @Parameter(hidden = true) Jwt jwt,
            @Parameter(description = "진단 회차 ID") Long testId
    );

    @Operation(summary = "퀴즈 결과 적용", description = "성향 진단 결과를 사용자 프로필에 적용합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "적용 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 바디 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<QuizApplyResponse>> applyResult(
            @Parameter(hidden = true) Jwt jwt,
            @RequestBody(required = true, description = "퀴즈 결과 적용 요청")
            QuizApplyRequest request
    );
}
