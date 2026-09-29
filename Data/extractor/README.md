# Job.is 공고 추출 툴킷 — 인계 패키지

원티드(공고 본체) + 잡코리아(기업 메타 보강) 기반 **IT/개발 채용공고 수집 파이프라인**.
산출물은 JSONL 2종(`job_postings` / `companies`)이며, DB 적재는 별도 패키지(`../database/`)의 `load.py`가 담당한다.

> **의존성 없음** — Python 3 표준 라이브러리만 사용(외부 패키지 설치 불필요). `skill_enrich`도 stdlib.

```
extractor/
├── README.md              ← 이 문서
├── EXTRACTION_GUIDE.md    ← 마스터 가이드 (추출 패턴·필드매핑·주의·검증로그) ★깊은 내용
├── schema.sql             ← 산출물이 매핑되는 DB 스키마(참고)
└── scripts/
    ├── common.py           ← HTTP/RSC/기업명 정규화 헬퍼
    ├── wanted.py           ← 원티드 목록(개발 태그 디스커버리)+상세 추출 (base)
    ├── jobkorea_company.py ← 잡코리아 기업메타 추출 (enrichment)
    ├── run_pipeline.py     ← 소량 단일 프로세스 오케스트레이션
    ├── build_pool.py       ← 대량: 개발 태그 풀 구성 + N 샤드 분할
    ├── shard_extract.py    ← 샤드 1개 추출 워커 (멀티프로세스/멀티에이전트)
    ├── merge_shards.py     ← 샤드 병합 + dedup → 최종 JSONL + 통계
    └── skill_enrich.py     ← 빈 skill_tags NLP 보강(사전매칭) + skills_inferred 표시
```

## 빠른 시작 (`scripts/`에서 실행)

```sh
cd scripts

# (a) 단건 확인
python3 wanted.py 371247              # 원티드 공고 1건 (skill/썸네일/geo 포함)
python3 jobkorea_company.py 지니언스    # 잡코리아 기업메타 1건

# (b) 소량 (개발직군만, 기업 자동 보강) → out/*.jsonl
python3 run_pipeline.py --target 30 --per-tag 20 --delay 1.5 --out ./out

# (c) 대량 (예: 1000건) — 풀 분할 → 병렬 워커 → 병합
python3 build_pool.py --shards 10 --per-tag-max 400 --out ./out/shards
for k in $(seq 0 9); do
  python3 shard_extract.py --shard ./out/shards/shard_$k.json --cap 100 \
          --delay 1.2 --label $k --out ./out/shards &
done; wait
python3 merge_shards.py --shards-dir ./out/shards --out ./out

# (d) 후처리: skill 보강 (50%→~95%)
python3 skill_enrich.py --in ./out/job_postings.jsonl          # dry-run 검수
python3 skill_enrich.py --in ./out/job_postings.jsonl --apply  # 적용

# (e) DB 적재는 → ../database/ 의 load.py 사용
```

## 핵심 주의 (자세한 근거는 EXTRACTION_GUIDE.md)

- **개발직군 발견**: 목록 `job_group_id=518`은 직군을 안 거른다. `wanted.DEV_TAG_IDS`(세부직군 19개) 합집합으로 발견.
- **원티드 상세 = HTML initialData + `/api/v4/jobs/{id}` 병합**: skill_tags·썸네일·geo는 API에만 있음.
- **기업 매칭 무결성**: 상위 N후보 스캔 + 이름 정규화(괄호별칭 흡수) 일치만 채택. 불일치 시 **타사 메타 저장 안 함**.
- **OCR 불필요**: 원티드 JD는 전부 텍스트, 잡코리아는 정형필드만 사용.
- **급여 없음**: 원티드 비공개.
- **레이트리밋**: `--delay` 준수(단일 1.5s / 샤드 1.2s). 샤드는 disjoint id라 중복 호출 없음.
