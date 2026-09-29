# Job.is — 채용공고 데이터 & 추출 파이프라인

Job.is(AI 커리어 큐레이션 서비스)의 **IT/개발 채용공고 데이터셋**과 그것을 생산하는 **수집 파이프라인**.
원티드(공고 본체) + 잡코리아(기업 메타 보강) 아키텍처.

## 구성

```
.
├── database/     ← 백엔드용: 바로 적재 가능한 PostgreSQL 데이터셋
│   ├── schema.sql · load.py · data/*.jsonl
│   └── README.md · DATA_DICTIONARY.md
├── extractor/    ← 데이터팀용: 수집/추출 파이프라인 (Python stdlib only, 무설치)
│   ├── scripts/  (wanted · jobkorea_company · build_pool · shard_extract · merge_shards · skill_enrich)
│   └── README.md · EXTRACTION_GUIDE.md · schema.sql
├── matching/     ← 백엔드/ML용: 초개인화 추천 엔진 (임베딩+정형 후보검색 → LLM 근거선별)
│   ├── engine/   (실행 스크립트: *_pg 매칭 · *_user 신호통합 · ingest_resume · compute_signal_state)
│   ├── schema/user_schema.sql (L3 유저신호+L4 추천로그+L5 이벤트) · docs/SIGNAL_DESIGN.md
│   └── fixtures/ · examples/ (실제 추천 결과 2건) · README.md
├── proposal/     ← 기획 문서 (서비스 정의 · 문제정의 · PRD · IA · 최종발표)
└── visioning/    ← 화면 시안 (와이어프레임 · 예시 목업, HTML)
    ├── wireframe.html    (Lo-fi 와이어프레임 보드)
    └── mockup_v1.html    (예시 목업 — 오늘의 추천)
```

| 폴더 | 대상 | 내용 | 시작점 |
|---|---|---|---|
| **database/** | 백엔드팀 | 데이터를 Postgres에 적재해 서비스에 사용 | `database/README.md` |
| **extractor/** | 수집/데이터팀 | 파이프라인을 돌려 데이터를 생산·갱신 | `extractor/README.md` |
| **matching/** | 백엔드/ML팀 | 유저↔공고 추천 엔진(후보검색→LLM 선별) + 유저신호 스키마 | `matching/README.md` |
| **proposal/** | 전체 | Job.is 기획 의도·요구사항·정보구조 | `proposal/INDEX.md` |
| **visioning/** | 전체 | 화면 와이어프레임·예시 목업 (브라우저로 열기) | `visioning/wireframe.html` |

## 데이터 스냅샷

| | |
|---|---|
| 공고 / 기업 | **1,000 / 618** |
| 직군 | 개발/IT 100% (MVP 한정) |
| 커버리지 | JD·썸네일 100% · skill 95% · geo 99.7% |
| 기업 보강 | enriched 40% (나머지 원티드 업종 fallback) |

## 빠른 시작

```sh
# A. DB 적재 (백엔드)
cd database && pip install -r requirements.txt
export DATABASE_URL="postgresql://user:pass@host:5432/jobis"
python load.py --dir data --schema schema.sql --init

# B. 데이터 재생산 (수집팀, 무설치)
cd extractor/scripts && python3 run_pipeline.py --target 30 --out ./out

# C. 추천 엔진 (백엔드/ML) — pgvector 위에서
cd matching && pip install -r requirements.txt && cd engine
#   database/schema.sql → matching/schema/user_schema.sql 적용 후 임베딩 적재 → 추천. 상세: matching/README.md
```

세부 내용은 각 패키지의 README / DATA_DICTIONARY / EXTRACTION_GUIDE 참고.
