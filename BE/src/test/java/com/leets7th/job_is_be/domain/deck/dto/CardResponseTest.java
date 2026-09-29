package com.leets7th.job_is_be.domain.deck.dto;

import com.leets7th.job_is_be.domain.deck.entity.Card;
import com.leets7th.job_is_be.domain.deck.entity.Deck;
import com.leets7th.job_is_be.domain.job.entity.Job;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CardResponseTest {

    @Test
    void 지역_태그는_Region_연관관계_대신_locationCity_locationDistrict를_사용한다() {
        Job job = Job.builder()
                .title("백엔드 엔지니어")
                .careerLevel("신입")
                .locationCity("서울")
                .locationDistrict("강남구")
                .build();
        Card card = Card.builder().deck(Deck.builder().build()).job(job).build();

        CardResponse response = CardResponse.from(card, List.of());

        assertThat(response.tags()).contains("서울 강남구");
    }

    @Test
    void 지역_정보가_없으면_지역_태그를_생략한다() {
        Job job = Job.builder().title("백엔드 엔지니어").build();
        Card card = Card.builder().deck(Deck.builder().build()).job(job).build();

        CardResponse response = CardResponse.from(card, List.of());

        assertThat(response.tags()).doesNotContain("null", "서울 강남구");
    }
}
