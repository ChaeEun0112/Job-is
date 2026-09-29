package com.leets7th.job_is_be.domain.personality.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum PersonalityResultType {

    SB("완벽주의 타입", "안정된 울타리 안에서 균형 있게", List.of("안정지향", "워라밸")),
    SF("일잘러 타입", "탄탄한 조직에서 성과로 인정받는", List.of("안정지향", "성과지향")),
    CB("이너피스 타입", "자유로운 분위기 + 나만의 페이스", List.of("도전지향", "워라밸")),
    CF("오너 타입", "빠르게 크는 곳에서 주도적으로", List.of("도전지향", "성장지향"));

    private final String displayName;
    private final String summary;
    private final List<String> fixedTags;
}
