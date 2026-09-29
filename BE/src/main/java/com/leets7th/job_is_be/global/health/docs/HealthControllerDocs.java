package com.leets7th.job_is_be.global.health.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Health", description = "서버 헬스 체크 API")
public interface HealthControllerDocs {

    @Operation(
            summary = "헬스 체크",
            description = "서버가 정상 동작 중인지 확인합니다. 항상 \"ok\" 문자열을 반환합니다."
    )
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "헬스 체크 성공")
    })
    String health();
}
