package com.leets7th.job_is_be.domain.user.controller.docs;

import com.leets7th.job_is_be.domain.user.dto.PresignedUrlRequest;
import com.leets7th.job_is_be.domain.user.dto.PresignedUrlResponse;
import com.leets7th.job_is_be.domain.user.dto.ResumeConfirmRequest;
import com.leets7th.job_is_be.domain.user.dto.ResumeDownloadUrlResponse;
import com.leets7th.job_is_be.domain.user.dto.ResumeResponse;
import com.leets7th.job_is_be.domain.user.dto.ResumeUploadResponse;
import com.leets7th.job_is_be.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

@Tag(name = "Resume", description = "이력서 파일 업로드/조회/삭제 API")
@SecurityRequirement(name = "JWT")
public interface ResumeControllerDocs {

    @Operation(
            summary = "이력서 업로드용 Presigned URL 발급",
            description = "이력서 파일을 스토리지에 직접 업로드할 수 있는 Presigned URL을 발급합니다. 사용자는 Access Token(JWT)으로 식별됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 바디 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<PresignedUrlResponse>> issuePresignedUrl(
            @Parameter(hidden = true) Jwt jwt,
            @RequestBody(required = true, description = "Presigned URL 발급 요청")
            PresignedUrlRequest request
    );

    @Operation(
            summary = "이력서 업로드 확정",
            description = "Presigned URL로 업로드가 완료된 이력서 파일을 서버에 등록/갱신 확정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "업로드 확정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 바디 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<ResumeUploadResponse>> confirmUpload(
            @Parameter(hidden = true) Jwt jwt,
            @RequestBody(required = true, description = "이력서 업로드 확정 요청")
            ResumeConfirmRequest request
    );

    @Operation(summary = "이력서 파일 목록 조회", description = "로그인한 사용자가 업로드한 이력서 파일 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<List<ResumeResponse>>> getFiles(
            @Parameter(hidden = true) Jwt jwt
    );

    @Operation(summary = "이력서 파일 삭제", description = "지정한 이력서 파일을 삭제합니다. 본인 파일만 삭제할 수 있습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 파일이 아님")
    })
    ResponseEntity<ApiResponse<Void>> deleteFile(
            @Parameter(hidden = true) Jwt jwt,
            @Parameter(description = "파일 ID") Long fileId
    );

    @Operation(
            summary = "이력서 파일 다운로드용 Presigned URL 발급",
            description = "지정한 이력서 파일을 다운로드할 수 있는 Presigned URL을 발급합니다. 본인 파일만 발급받을 수 있습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음")
    })
    ResponseEntity<ApiResponse<ResumeDownloadUrlResponse>> issueDownloadUrl(
            @Parameter(hidden = true) Jwt jwt,
            @Parameter(description = "파일 ID") Long fileId
    );
}
