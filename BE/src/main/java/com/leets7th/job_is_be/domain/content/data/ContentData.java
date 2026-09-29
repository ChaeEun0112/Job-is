package com.leets7th.job_is_be.domain.content.data;

import com.leets7th.job_is_be.domain.content.enums.ContentType;

import java.time.LocalDate;
import java.util.List;

public final class ContentData {

    private static final List<ContentItem> CONTENTS = List.of(
            new ContentItem(
                    1L,
                    ContentType.NEWS,
                    "2026년 신입 개발자 채용 시장 리포트",
                    "하반기 백엔드 채용 공고가 전년 대비 18% 증가했습니다.",
                    "2026년 하반기에는 백엔드와 데이터 분야를 중심으로 신입 개발자 채용 공고가 "
                            + "증가하고 있습니다. 지원 전 요구 기술과 채용 일정을 확인해 보세요.",
                    "잡코리아",
                    LocalDate.of(2026, 7, 1),
                    null,
                    null,
                    null,
                    null,
                    "https://example.com/news/developer-job-market-2026",
                    1
            ),
            new ContentItem(
                    2L,
                    ContentType.BENEFIT,
                    "신입·주니어 개발자 이력서 첨삭 무료 프로그램",
                    "신입 개발자 대상 이력서 첨삭 프로그램으로 선착순 30명을 모집합니다.",
                    "현직 개발자 멘토가 이력서를 첨삭하고 모의 면접을 진행하는 무료 프로그램입니다. "
                            + "신입·주니어 개발자를 대상으로 하며 선착순으로 마감됩니다.",
                    "잡코리아 파트너",
                    LocalDate.of(2026, 7, 1),
                    "신입 ~ 3년 차 개발자",
                    LocalDate.of(2026, 7, 1),
                    LocalDate.of(2026, 7, 31),
                    "잡코리아 파트너 페이지에서 신청서 작성",
                    "https://example.com/benefits/resume-review-2026",
                    2
            )
    );

    private ContentData() {
    }

    public static List<ContentItem> getContents() {
        return CONTENTS;
    }
}
