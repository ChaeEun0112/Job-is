# Job.is 유저 신호 → 매칭 연결 설계 (L3 + 매칭 통합)

초개인화 = **4가지 신호의 결합**. 각 신호는 *다른 질문*에 답하고, 매칭 엔진(retrieve→LLM select)에
*다른 방식*으로 들어간다. 스키마는 `user_schema.sql`, 매칭 엔진 구현은 이 폴더(`matching/`)의 스크립트.

## 1. 4신호 모델

| # | 신호 | 답하는 질문 | 테이블 | 성격 |
|---|---|---|---|---|
| ① | 선언적 선호 | 뭘 원한다고 말하는가 | `user_preferences` | 콜드스타트 필수, 코스 |
| ② | 역량/증거 | 실제 뭘 할 수 있나 | `user_documents`,`user_skills` | 정밀도 최대 레버 |
| ③ | 잠재 성향 | 기질상 뭐가 맞나 | `user_disposition` | 콜드스타트 보완, 소프트 |
| ④ | 행동 피드백 | 실제 뭐에 반응하나 | `user_events`,`user_signal_state` | 차별화·복리(웜스타트) |

## 2. 신호별 매칭 진입 방식

| 신호 | retrieve (recall, 넓게) | LLM select (precision, 정확히) | 콜드스타트 |
|---|---|---|---|
| ① 선언 | **하드필터**: 지역(도시∨원격)·경력(공고 min≤내 연차)·`excludes` 키워드. + 임베딩 쿼리 재료 | 프로필 컨텍스트 | 항상 가용(진입 최소입력) |
| ② 이력서 | 문서 임베딩→**JD와 대칭 코사인**. 추출 스킬→소프트 부스트/필터 | 원문·경력항목을 **근거 컨텍스트**로 → "당신의 X 경험이 Y 요구와 맞다" | 선택. 없으면 ①로 폴백 |
| ③ 성향 | 태그 기반 **소프트 부스트**(예: 스타트업선호→벤처 가점) | 동점 시 **타이브레이커** 컨텍스트 | 선택 |
| ④ 피드백 | `taste_vector`로 취향 반영 + 싫어요/사유 패턴 **다운랭크** | `pref_memory` 주입("대기업 선호 낮음") | day-1 없음 → 누적 복리 |

## 3. retrieve 스코어링 (matching 확장)

```
user_vector = blend(  declared_query_emb,          # ① 선언 텍스트 임베딩
                      resume_emb,                  # ② 이력서 임베딩 (있으면 비중↑)
                      taste_vector )               # ④ 저장/좋아요 취향 (누적되면 비중↑)

score(job) = w_sem  · cos(user_vector, job.embedding)      # 의미 적합
           + w_skill · skill_overlap(user_skills, job.skill_tags)   # ①②
           + w_size  · size_pref_match                     # ①
           + w_disp  · disposition_match(tags, job/company) # ③
           - w_neg   · feedback_penalty                     # ④ (싫어요/사유 패턴)
   , 단 하드필터(지역·경력·excludes) 통과 공고에 한함
```

- `user_vector` 합성 비중은 신호 가용성에 따라 동적(§5). 스킬은 `canonical_id`로 공고 스킬과 **동일 축**에서 교집합.
- retrieve 는 top-N(예 20)까지만 좁혀 LLM 비용을 통제(matching 검증된 구조).

## 4. LLM 선별기에 들어가는 컨텍스트

`llm_select`(matching)의 프롬프트에 **유저 신호 묶음**을 추가:
```
[프로필] user_preferences (선언)
[역량]   resume 요약 + user_skills(증거강도) + 경력항목        ← 근거 있는 이유 생성
[성향]   disposition tags                                    ← 타이브레이커
[학습]   pref_memory (진화형 선호)  + 최근 싫어요/사유 요약     ← 웜스타트 보정
[후보]   retrieve top-N (JD 스니펫 포함)
→ top-K 선별 + 근거(fit_points)·약점(caution) → rec_items 로그
```
선별 결과는 `rec_bundles`/`rec_items`에 저장 → (a) 피드백을 노출건과 연결, (b) 이유 감사/평가, (c) 개선 루프 입력.

## 5. 콜드스타트 → 웜스타트 (가중치 전환 + 진행형 프로파일)

와이어프레임의 **"온보딩 최소화"** 와 충돌하지 않게, 신호를 생애주기로 배치:

- **진입(콜드)**: ① 최소입력만 → 즉시 첫 추천. `user_vector≈declared_query_emb`, `w_disp/ w_neg=0`.
- **업그레이드(선택)**: "이력서 넣으면 정확해져요" / "3분 성향 게임" 을 *유도*(강제 X). ②③ 활성 → `resume_emb` 비중↑.
- **사용 중(웜)**: `user_signal_state.event_count` 증가에 따라 `w_neg`·`taste_vector` 비중↑, 선언 비중↓.

즉 **첫날에도 되고(선언+선택적 이력서/성향), 쓸수록 좋아지는(피드백 복리)** 2단계. 이게 정적 AI레터 대비 차별화의 실체.

## 6. 성향 테스트 설계 원칙 (③의 타당성 가드)

⚠️ 재미만 있고 예측력 없는 퀴즈는 매칭에 **노이즈**를 주입한다. 규칙: **모든 축은 관측가능한 공고/기업 피처에 매핑**되어야 채택.

| 성향 축 (-1~1) | 매핑되는 공고/기업 피처 | 매칭 작용 |
|---|---|---|
| 안정 ↔ 도전 | company_type/employee_count/설립연차 | 대기업 vs 벤처 가점 |
| 스페셜리스트 ↔ 제너럴리스트 | 직무 범위(단일직무 vs 풀스택/멀티롤) | category_child·JD 시그널 |
| 협업 ↔ 독립 | JD의 팀/문화 시그널 | benefits/culture 텍스트 |
| 워라밸 ↔ 몰입 | benefits(유연근무 등) 시그널 | 가점/감점 |
| 직무 적성 | interest_fields 확정/확장 | 관심분야 보정 |

매핑 안 되는 축은 **넣지 않는다**(연출 금지). 결과는 소프트 신호라 가중치를 낮게(w_disp 小).

## 7. 개선 루프 (④가 다음 추천을 바꾸는 경로)

```
노출(rec_items) ─▶ 유저 행동(user_events: 좋아요/싫어요/저장/스킵/코멘트)
   │
   ├─ 저장/좋아요 공고 임베딩 가중평균 → taste_vector 갱신 (retrieve 반영)
   ├─ 싫어요/사유 패턴 → excludes 보강 + feedback_penalty (다운랭크)
   └─ 코멘트/사유 → LLM 요약 → pref_memory 갱신 (선별기 컨텍스트)
        → 다음 rec_bundle 이 달라짐
```
배치(야간) 또는 준실시간으로 `user_signal_state` 재계산.

## 8. 구현 순서(제안)

1. **①선언 + 공고 임베딩 pgvector 이관** → matching 로직을 DB화(대칭 매칭 기반).
2. **rec_bundles/rec_items + user_events** → 노출·행동 로깅(루프의 뼈대). 피드백 UI는 와이어의 저장/스킵/사유칩부터.
3. **②이력서** 파싱·임베딩·스킬추출(공고 스킬 어휘 재사용) → 정밀도 점프.
4. **④ taste_vector/pref_memory 루프** → 웜스타트 복리.
5. **③성향 테스트** → §6 축-피처 매핑 확정 후 소프트 신호로.
