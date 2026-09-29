# Job.is 초개인화 매칭 엔진

유저 프로필 → **[1] 임베딩+정형필터 후보검색(recall)** → **[2] LLM 최종선별+추천이유(precision)** → top-K 추천.

기획서의 핵심 가설(초개인화 + 설명가능성)을 1,000건 공고 데이터로 검증하는 수직 슬라이스.
구현 순서 1~4단계(pgvector DB화 · 추천로깅 · 이력서신호 · 개선루프) 완료, 5단계(성향 테스트)만 미착수.

> **전제**: `../database/`(공고/기업 데이터·schema.sql·load.py)가 먼저 적재돼 있어야 함.
> **결과 예시**: `examples/` 에 실제 top-5 추천 2건 — `reco_pg_*`(선언-only) vs `reco_user_*`(이력서+피드백 통합).

## 폴더 구성

```
matching/
├── engine/      실행 스크립트 (전부 여기서 실행; 서로 import)
├── fixtures/    샘플 입력 — personas.json · resumes/
├── schema/      user_schema.sql (L3 유저신호 + L4 추천로그 + L5 이벤트 DDL)
├── docs/        SIGNAL_DESIGN.md (4신호 → 매칭 연결 설계)
├── examples/    실제 추천 결과 2건 (선언-only vs 신호통합)
└── README.md · requirements.txt
```

`engine/` (실행 순서·역할):
```
[numpy 원형]      embed · retrieve · llm_select · recommend
[step1 pgvector]  load_embeddings · retrieve_pg · recommend_pg
[step2 로깅]      log_recommendation
[step3 이력서]    ingest_resume · retrieve_user · recommend_user
[step4 개선루프]  compute_signal_state
```

> 스크립트는 서로 `import` 하므로 한 폴더(`engine/`)에 두고 **`engine/` 에서 실행**한다.
> 입력(personas·resumes·DB 데이터)은 `__file__` 기준 경로라 cwd 무관하게 찾고, 생성물(embeddings·candidates·reco)은 cwd(=`engine/`)에 쓴다.

## 아키텍처 (retrieve → rerank/select)

```
프로필 ─┬─▶ [retrieve*]  의미 임베딩 유사도 + 정형 하드필터(지역/경력/제외) + 소프트부스트(스킬/규모)
        │                → 후보군 top-N (예: 20)   ← recall, 로컬·저비용
        └─▶ [llm_select] Claude가 프로필↔후보 대조 → top-K 선별 + 추천이유 생성
                         → 추천 top-K (예: 5)      ← precision, 설명가능성
```

- **recall(임베딩)**: 싸고 넓게. 놓치지 않는 게 목적.
- **precision(LLM)**: 비싸지만 정확. 프로필과 진짜 맞는 것만 고르고 *왜 맞는지* 설명.

## 빠른 시작 (`engine/` 에서 실행)

```sh
cd matching && pip install -r requirements.txt && cd engine
export DATABASE_URL=postgresql://jobis:jobis@127.0.0.1:5433/jobis

# A. DB 준비 (최초 1회) — pgvector + 스키마 + 데이터
docker run -d --name jobis-pg -e POSTGRES_USER=jobis -e POSTGRES_PASSWORD=jobis \
  -e POSTGRES_DB=jobis -p 127.0.0.1:5433:5432 pgvector/pgvector:pg17
psql "$DATABASE_URL" -f ../../database/schema.sql
psql "$DATABASE_URL" -f ../schema/user_schema.sql          # vector 확장 + embedding 컬럼 + 유저테이블
python ../../database/load.py --dir ../../database/data     # --init 금지(embedding 컬럼 보존)

# B. 임베딩 적재 (최초 1회) — fastembed 모델 0.22GB 자동 다운로드
python embed.py                       # → embeddings.npy + meta.jsonl (engine/)
python load_embeddings.py --data . --hnsw

# C. 추천 (단계별)
python recommend_pg.py       --persona p2-senior-backend --k 5   # step1: 선언 신호
python log_recommendation.py --persona p2-senior-backend --simulate  # step2: 추천 로깅 + 피드백
python ingest_resume.py      --persona p2-senior-backend         # step3: 이력서 → user_skills/문서
python recommend_user.py     --persona p2-senior-backend --k 5   # step3: 신호통합 + 근거선별
python compute_signal_state.py --persona p2-senior-backend       # step4: 피드백 → taste_vector/pref_memory
```

> `ANTHROPIC_API_KEY` 없으면 `recommend_*` 는 후보군 + LLM 프롬프트(`select_prompt_*.txt`)만 저장 →
> 에이전트/콘솔에서 선별해 `reco_*.json` 으로 저장(이 저장소 `examples/` 결과도 그렇게 생성).

## 설계 메모

- **임베딩 텍스트**: position + main_tasks + requirements + preferred_points + skill_tags (회사 홍보성 intro/benefits 제외 — 직무 신호 집중).
- **하드필터**: 지역(도시 or 원격), 경력(공고 최소경력 ≤ 내 경력), 제외 키워드(SI/파견 등).
- **소프트부스트**: `score = cosine + 0.15·스킬교집합 + 0.05·규모선호`.
- **LLM 선별 계약**: 구조화 JSON(rank/reason/fit_points/caution/dropped_note). 억지 채움 금지, 근거 중심.

---

## 단계별 상세

### step1 — pgvector DB화

메모리 numpy(`embeddings.npy`) 대신 **Postgres+pgvector** 로 후보검색. SQL 이 무거운 일(벡터 KNN `ORDER BY embedding <=> q` + 하드필터),
앱이 가벼운 재랭킹(스킬/규모 부스트, `retrieve.py` 와 동일 공식). 결과 스키마 동일 → `llm_select.py` 무수정 재사용.
- `load_embeddings.py`(→`job_postings.embedding` 384d + HNSW) · `retrieve_pg.py` · `recommend_pg.py`
- **검증**: 동일 fastembed·동일 임베딩 기준 numpy↔SQL **top-20 100% 일치(순위까지)**. `EXPLAIN` 상 HNSW(`idx_jp_emb`) 사용.
- ⚠️ 필터가 매우 선택적이면(좁은 지역) HNSW 가 LIMIT 를 못 채울 수 있음 → pgvector 0.8+ `hnsw.iterative_scan` 또는 overfetch 상향. 현 1,000건 규모엔 미발생.

### step2 — 추천 로깅 (L4/L5)

'무엇을·왜 노출했나'를 저장해야 피드백을 노출건과 연결 → 개선 루프의 뼈대.
- `log_recommendation.py`: reco → `users`/`user_preferences`(신호①) + `rec_bundles`/`rec_items`(이유·근거·약점·단계별점수) + `user_events`(impression 자동, `--simulate` 로 like/dislike)
- **검증**: `user_events.rec_item_id → rec_items → job_postings(.embedding)` 로 피드백이 노출·공고·임베딩까지 연결. 유저×일자 번들 단위 멱등.

### step3 — 이력서 신호② (정밀도 최대 레버)

이력서를 공고와 **같은 축**에 올려 매칭·근거를 강화.
- `ingest_resume.py`: 어휘사전을 **DB 권위 태그(skills_inferred=false)에서 직접 구축**(276 스킬) + DENY 필터 → `user_documents`(임베딩) + `user_skills`(canonical_id)
- `retrieve_user.py`: `user_vector = blend(선언, 이력서, taste)` + `user_skills.canonical_id` ↔ 공고 `skill_tag_ids` **동일축 교집합**
- `recommend_user.py` + `llm_select.build_prompt(..., evidence=)`: 이력서 요약·스킬근거 주입 → "지원자의 X 경험이 공고의 Y 요구와 맞다"
- **검증**: 선언-only 대비 **top10 중 4건 교체**(커머스·중견 대용량 진입). 이유가 스킬 나열 → 경험 근거로 전환.

### step4 — 개선 루프 신호④ (쓸수록 좋아짐)

피드백을 파생상태로 응축해 다음 추천을 바꿈.
- `compute_signal_state.py`: `user_events` → `taste_vector`(긍정 공고 임베딩 가중평균) + `pref_memory`(사유 요약, 운영은 LLM) + `event_count`
- 루프: 노출(rec_items) → 행동(user_events) → `compute_signal_state` → 다음 `retrieve_user`/`recommend_user` 가 달라짐
- **검증**: 대형 커머스 좋아요 → blend `declared0.32/resume0.48/taste0.20` → 쿠팡 #3→#2·컬리 #9→#4·올리브영 신규. `pref_memory` 는 선별기 evidence 로 주입(retrieve·select 양쪽 반영).
- 블렌드 비중(콜드→웜): 선언만 `declared` → +이력서 `0.4·선언+0.6·이력서` → +피드백 `taste` 비중을 `event_count` 에 따라 상향(최대 0.4).

상세 설계(스코어링·성향축 매핑·루프) = **`docs/SIGNAL_DESIGN.md`**. 유저신호 DDL = **`schema/user_schema.sql`**.

---

## 프로덕션 전환 시 (남은 것)

- 임베딩 모델 업그레이드: multilingual-e5-large(1024d) 등 (컬럼 차원 변경 필요).
- 유저 프로필: `fixtures/personas.json` 스텁 → 실제 L3 유저 테이블(`schema/user_schema.sql`).
- 신호③ 성향 테스트(구현 순서 5단계, 미착수): `docs/SIGNAL_DESIGN.md §6` 축-피처 매핑 확정 후 소프트 신호로.
