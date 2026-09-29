package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.global.ai.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EditorNoteService {

    private final OpenAiService openAiService;


    public String generateEditorNote(Job job) {

        String systemPrompt = """
                너는 취업 추천 서비스 Job.is의 에디터다.
                채용공고를 읽고 상세 화면에 노출될 Editor's Note를 작성한다.

                규칙:
                - 공고 원문에 있는 사실만 사용한다.
                - 과장하지 않는다.
                - 1~2문장으로 작성한다.
                - 너무 짧은 문장을 이용하지않는다.
                - 존댓말을 사용한다.
                """;


        String userPrompt = """
                다음 채용공고의 Editor's Note를 작성해줘.

                회사명: %s

                포지션:
                %s

                담당업무:
                %s

                자격요건:
                %s

                우대사항:
                %s
                """
                .formatted(
                        job.getCompany() != null
                                ? job.getCompany().getName()
                                : "",
                        job.getTitle(),
                        job.getMainTasks(),
                        job.getRequirements(),
                        job.getPreferredPoints()
                );


        return openAiService.chat(
                systemPrompt,
                userPrompt
        );
    }
}
