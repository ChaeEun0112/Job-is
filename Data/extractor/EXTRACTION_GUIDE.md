# Job.is 수집 설계 — 추출 패턴 & 실행 가이드

**아키텍처: 원티드 = 공고 본체(base) · 잡코리아 = 기업 메타 보강(enrichment)**

이 문서 하나로 바로 착수 가능하다. 스키마(`schema.sql`)와 실행 스크립트(`scripts/`)는 모두
실측 검증됨(아래 "검증 로그" 참고). 표준 라이브러리만 사용 — 외부 패키지 설치 불필요.

---

## 1. 왜 이 조합인가 (30건 실측 근거)

| | 원티드 | 잡코리아 |
|---|---|---|
| JD 품질 | **의미단위 텍스트 분리 10/10**(intro/main_tasks/requirements/preferred/benefits) | 텍스트~이미지 편차 |
| 기업 메타 | 약함(업종 정도) | **CORP_INFO: 사원수·기업규모·상장·업종·주소** |
| 결론 | **공고 본체로 최적** | **기업 메타 보강원으로 최적** |

→ 공고는 원티드에서, 그 회사의 객관적 메타는 잡코리아에서 가져와 합친다.

---

## 2. 디렉터리

```
design/
├── EXTRACTION_GUIDE.md     ← 이 문서
├── schema.sql              ← PostgreSQL 스키마 (companies, job_postings)
└── scripts/
    ├── common.py           ← HTTP/RSC/정규화 헬퍼 (stdlib only)
    ├── wanted.py           ← 원티드 목록(태그 디스커버리)+상세 추출기 (base)
    ├── jobkorea_company.py ← 잡코리아 기업메타 추출기 (enrichment)
    ├── run_pipeline.py     ← 단일 프로세스 오케스트레이션 → JSONL
    ├── build_pool.py       ← 개발 태그 풀 구성 + N개 샤드 분할 (대량용)
    ├── shard_extract.py    ← 샤드 1개 추출 워커 (멀티에이전트용)
    ├── merge_shards.py     ← 샤드 결과 병합 + dedup → 최종 JSONL + 통계
    ├── skill_enrich.py     ← 빈 skill_tags NLP 보강(사전매칭) + skills_inferred 표시
    └── load.py             ← JSONL → PostgreSQL 적재기 (psycopg2, idempotent)
```

**백엔드 인계 패키지**: `../database/` (schema.sql + load.py + data/*.jsonl + README + DATA_DICTIONARY).
`python load.py --dir data --schema schema.sql --init` 한 방으로 Postgres 적재. Docker Postgres로 적재·검증 완료.

## 3. 빠른 시작

```sh
cd design/scripts
# (a) 단건 확인
python3 wanted.py 371247            # 원티드 공고 1건 정규화 출력 (skill/썸네일/geo 포함)
python3 jobkorea_company.py 지니언스  # 잡코리아 기업메타 1건

# (b) 소량 단일 프로세스 (개발직군만, 기업 자동 보강) → ../out/*.jsonl
python3 run_pipeline.py --target 30 --per-tag 20 --delay 1.5 --out ../out

# (c) 대량 (예: 1000건) — 풀 분할 후 멀티워커 → 병합
python3 build_pool.py --shards 10 --per-tag-max 400 --out ../out/shards
#   → shard_0..9.json. 각 샤드를 워커(또는 병렬 에이전트)로:
python3 shard_extract.py --shard ../out/shards/shard_0.json --cap 100 \
        --delay 1.2 --label 0 --out ../out/shards          # 0..9 반복(병렬)
python3 merge_shards.py --shards-dir ../out/shards --out ../out

# (d) DB 적재
psql "$DATABASE_URL" -f ../schema.sql
#   companies.jsonl / job_postings.jsonl 를 COPY/INSERT (5절 참고)
```

---

## 4. 추출 패턴 (소스별)

### 4.1 원티드 (base / 공고)

**목록 discovery** — 공개 JSON API:
```
GET https://www.wanted.co.kr/api/v4/jobs?country=kr&job_group_id=518&tag_type_id=872&job_sort=job.latest_order&years=-1&locations=all&limit=100&offset=N
→ data[].id
```
- ⚠️ **`job_group_id=518`(개발) 단독은 직군을 안 거른다**(마케터·변호사 등 섞임 — 실측). **세부직군
  `tag_type_id` 가 깨끗하게 걸린다**. → `wanted.DEV_TAG_IDS`(백엔드/프론트/모바일/QA/데이터/ML/AI/
  DevOps/보안/임베디드 등 19개)의 **합집합으로 발견**한다. `wanted.list_dev_job_ids()` / `build_pool.py`가 처리.
  (검증 2026-06-28: 19개 태그 offset 페이징 → 고유 후보 **2,619건** 확보. 전부 IT/개발.)
- ⚠️ 태그도 기업 임의 태깅이라 미세 누수 가능 → fetch 후 **`category_parent=="개발"` 최종 가드**(저렴, 구현됨).

**상세 추출 — 두 소스 병합 필수**(`wanted.fetch_posting`):

| 소스 | URL | 여기서만 얻는 것 |
|---|---|---|
| ① HTML initialData | `https://www.wanted.co.kr/wd/{id}` 의 `__NEXT_DATA__` → `props.pageProps.initialData` | `category_tag`(개발필터), `company.registration_number`(사업자번호), JD 5필드 |
| ② 상세 API | `https://www.wanted.co.kr/api/v4/jobs/{id}` → `job` | **`skill_tags`(정형+id)·`title_img`/`company_images`(썸네일)·`address.geo_location`(위경도)** |

- ⚠️ **skill_tags·이미지·좌표는 initialData 엔 필드 자체가 없다**(항상 null). 오직 상세 API(②)에만 온다.
  초개인화 매칭/썸네일의 핵심 소스이므로 ②를 반드시 같이 호출한다. (②는 사업자번호가 없어 ①도 필요)
- ⚠️ **래핑 변형 방어**(실측): `id="__NEXT_DATA__"` 태그에 `crossorigin` 속성이 붙거나, 태그 자체가
  없고 일반 `<script>`에 들어있는 경우가 있다. → **"initialData 포함 모든 script 스캔"** fallback 필수
  (`wanted.extract_initial_data`가 처리).

**필드 매핑 (initialData → DB):**

| initialData | → job_postings | 비고 |
|---|---|---|
| id | external_id | 원티드 wd id |
| position | position | 직무명 |
| intro / main_tasks / requirements / preferred_points / benefits | 동명 컬럼 | **JD 의미단위(핵심)** |
| category_tag.parent_tag.text | category_parent | 예: 개발 |
| category_tag.child_tags[].text | category_child[] | 예: iOS 개발자 |
| career.annual_from / annual_to | career_min / career_max | ⚠️ **경력 연수**(연봉 아님) |
| employment_type | employment_type | regular 등 |
| address.{country,location,district,full_location} | location_* | |
| is_remote_work | is_remote | |
| due_time | due_time | null = 상시채용 |
| hire_rounds | hire_rounds | 전형절차 텍스트 |
| reward.formatted_total | reward_total | 추천보상금 |
| company.company_name | companies.name | |
| company.registration_number | companies.registration_number | **사업자번호** (initialData 에만) |
| company.company_id | companies.wanted_company_id | |
| company.industry_name | companies.industry(fallback) | |
| company.company_description | companies.description | |

**상세 API(`/api/v4/jobs/{id}` → `job`) 전용 필드 (initialData 엔 없음):**

| job 필드 | → job_postings | 비고 |
|---|---|---|
| skill_tags[].title | skill_tags[] | 스킬명 |
| skill_tags[].id | skill_tag_ids[] | **canonical id** (동의어 정규화/매칭) |
| title_img.origin → company_images[0] → logo_img | thumbnail_url | 폴백 체인 (`wanted._images`) |
| (위 전체) | image_urls[] | |
| address.geo_location.location.{lat,lng} | geo_lat / geo_lng | **거리기반 초개인화** |

- ⚠️ `skill_tags`는 **상세 API 에선 ~30% 채워짐**(개발직군은 더 자주). 단 **기업 임의 태깅이라 노이즈 있음**
  (예: "백엔드(Node.js)"에 React/Next.js). → 정형(API) + 비는 부분은 `requirements`/`preferred_points`
  **NLP 보강** 하이브리드. (initialData 만 보면 항상 null 이라 "수집 불가"로 오판하기 쉬움 — 실측 정정)
- ⚠️ **이미지 = 회사 브랜딩 배너**(`title_img`), JD 정보가 아니다. JD 는 `job.detail` 에 전부 텍스트.
  → **원티드엔 OCR 불필요**(배너 OCR 해봐야 홍보문구만 나옴). title_img 는 카드 썸네일 용도로만 사용.
- ⚠️ **현재 설계에선 OCR 자체가 불필요**: 원티드=JD 텍스트, 잡코리아=기업메타 정형필드만, 사람인=제외.
  JD 를 이미지로 박는 본문을 아무 데서도 안 가져온다. → OCR/이미지입력 폴백은
  **향후 잡코리아/사람인 'JD 본문'을 공고 소스로 승격할 때만** 검토 대상.
- ⚠️ **연봉/급여 필드 없음**(30건 전부) → 수집 대상 아님. 초개인화 매칭에서 유일하게 못 채우는 축.

### 4.2 잡코리아 (enrichment / 기업 메타)

**경로**: 기업명만 있으면 된다.
```
1) https://www.jobkorea.co.kr/Search/?stext={기업명}     → /Recruit/GI_Read/(\d+) 후보 Gno 들(상위 N개)
2) 각 후보 https://www.jobkorea.co.kr/Recruit/GI_Read/{Gno}?sc=729&sn=103
   → RSC(self.__next_f.push 결합) 안 CORP_INFO 회사객체 + JSON-LD 회사명
3) 회사명이 원티드명과 정규화 일치하는 첫 후보 채택. 일치 없으면 폐기(아래).
```

**필드 매핑 (CORP_INFO → companies):**

| RSC 키 | → companies | 예 |
|---|---|---|
| employeeCount | employee_count | 210 |
| companyTypeName | company_type | 대기업/중견기업/중소기업/벤처기업 |
| industryName | industry | 정보보안 |
| stockStatusName | stock_status | 코스피/코스닥 상장/비상장/- |
| address.address(+addressDetail) | hq_address | 경기 안양시 … |
| (JSON-LD hiringOrganization.name) | name_match 검증용 | 지니언스(주) |

- 회사명 검증: 공고의 **JSON-LD `hiringOrganization.name`**(HTML `<script type=ld+json>` 우선, RSC fallback)을
  원티드 회사명과 `norm_company()`로 정규화 비교. status:
  - `enriched` — 후보 중 이름 일치, 메타 채움
  - `name_mismatch` — 후보는 있었으나 이름 일치 없음. **타사 메타는 저장하지 않고**(무결성) `rejected_name`만 감사용 기록
  - `not_found` — 검색 결과 없음. → 둘 다 원티드 `industry_name`으로 fallback
- ⚠️ **첫 Gno 무조건 채택 금지**(실측 교훈): 해당 기업이 잡코리아에 없으면 검색이 *무관한 회사* 공고를 첫 결과로
  준다(예: `대동애그테크`→`24시라인동물의료센터`). → `enrich()`는 **상위 N=3 후보를 훑어 이름 일치하는 것만** 채택.
- ⚠️ **정규화는 괄호 별칭까지 흡수해야 함**(실측): 원티드 `크몽(kmong)`·`넵튠(Neptune)` ↔ 잡코리아 `㈜크몽`·`㈜넵튠`.
  `norm_company()`가 `(...)` 괄호군 + 법인표기(㈜/주식회사/Inc/Co.,Ltd)를 제거해 일치시킨다.
- ⚠️ **name_mismatch 시 사원수·규모 등 숫자 필드를 절대 채우지 말 것** — 틀린 회사 값이 들어가는 무결성 버그(수정됨).

---

## 5. DB 적재

```sh
psql "$DATABASE_URL" -f schema.sql
```
JSONL → 테이블 적재(권장: 임시 staging 후 upsert). 조인 키는 `companies.normalized_name`
↔ `job_postings.company_normalized_name`. 간단 적재 예:

```sql
-- companies upsert (사업자번호 우선, 없으면 normalized_name)
INSERT INTO companies (name, normalized_name, registration_number, wanted_company_id,
  industry, employee_count, company_type, stock_status, hq_address, description,
  jobkorea_gno_ref, name_match, enrichment_status, raw_jobkorea, enriched_at)
VALUES (...)  -- companies.jsonl 각 행
ON CONFLICT (normalized_name) DO UPDATE SET
  employee_count=EXCLUDED.employee_count, company_type=EXCLUDED.company_type,
  stock_status=EXCLUDED.stock_status, industry=EXCLUDED.industry,
  enrichment_status=EXCLUDED.enrichment_status, updated_at=now();

-- job_postings: company_normalized_name 으로 company_id 매핑 후 insert
INSERT INTO job_postings (source, external_id, source_url, company_id, position,
  intro, main_tasks, requirements, preferred_points, benefits, category_parent,
  category_child, career_min, career_max, employment_type, location_full,
  is_remote, due_time, hire_rounds, reward_total, raw)
SELECT 'wanted', j.external_id, j.source_url, c.id, j.position, ...
FROM staging_postings j JOIN companies c ON c.normalized_name = j.company_normalized_name
ON CONFLICT (source, external_id) DO UPDATE SET status=EXCLUDED.status, updated_at=now();
```
(psycopg2 로더가 필요하면 `run_pipeline` 출력 JSONL을 그대로 읽어 INSERT 하면 된다.)

---

## 6. 알려진 갭 / 다음 작업

1. **개발직군 수집 효율** — ✅ 해결: `wanted.DEV_TAG_IDS`(19개 세부직군) 합집합 디스커버리로 사전 fetch 낭비 제거.
   더 넓히려면 태그 추가(현재 풀 ~2,600건). 비활성/마감 공고 제외는 `status` 확인 단계 추가 검토.
2. **NLP 스킬 보강** — `skill_tags`는 상세 API 로 **~30%만** 채워짐(개발직군은 더 많음, 노이즈 있음).
   빈 ~70%는 `requirements`+`preferred_points`에서 기술 키워드 추출 모듈로 보강 → `skill_tags` 채움.
   (canonical `skill_tag_ids` 사전을 추출 결과 정규화 기준으로 재활용 가능.)
3. **기업 매칭 정확도** — 현재 "상위 N=3 후보 Gno + 이름 정규화 일치(괄호별칭 흡수)". 무결성은 확보(불일치 시
   타사 메타 폐기)되나 **보강 커버리지는 ~50%대**(나머지는 잡코리아에 공고 없는 기업 → fallback). 더 높이려면
   사업자번호 기반 매칭(원티드 `registration_number` 보유 → 잡코리아가 사업자번호를 노출하는 경로 확인 필요).
4. **급여** — 30건 중 1건만 공개 → 수집 제외, "비공개" 기본 전제.
5. **레이트리밋/매너** — `--delay`(단일 1.5s / 샤드 1.2s) 준수. 멀티에이전트는 샤드별 disjoint id 라 중복 호출
   없음(동일 기업이 복수 샤드에 걸리면 보강만 일부 중복 → 병합 dedup).

---

## 7. 검증 로그 (실측)

- `wanted.py` → wd/371260 등 정상 추출(JD 5필드 + company.registration_number 등).
- `jobkorea_company.py 지니언스` → `{employee_count:210, company_type:중소기업, industry:정보보안,
  stock_status:코스닥 상장, name_match:true, enrichment_status:enriched}`.
- `run_pipeline.py` → 개발필터 통과 → 잡코리아 보강 → companies.jsonl + job_postings.jsonl 생성 확인.
- **(2026-06-28) 상세 API 병합 검증**: IT 공고 24건 표본에서 `skill_tags` 7/24 채워짐(개발직군 비중↑,
  canonical id 포함), `title_img` 24/24, geo_location 위경도 정상. 썸네일 1건 다운로드(973KB PNG)
  후 직접 판독 성공(브랜딩 배너 — JD 정보 아님 확인). initialData 에는 skill/이미지 필드 부재 확인.
- **(2026-06-28) 30건 자체검증**: position/JD/category/thumbnail/geo/career **100%**, skill 36%, due_time 33%,
  필수필드 누락 0·중복 0. 기업 보강 enriched 11 / name_mismatch 11 / not_found 4. → mismatch 원인 2종 발견:
  (A) 괄호 별칭 정규화 누락(넵튠(Neptune)↔㈜넵튠), (B) 검색 첫 Gno 오매칭. **둘 다 수정**:
  `norm_company` 괄호 제거 + `enrich` 다후보 스캔/타사메타 폐기. 재검증: 넵튠·크몽 enriched 복구,
  대동애그테크·디플래닛 emp=None(틀린 숫자 제거) 확인.
- **(2026-06-28) 대량 추출 1,000건**: `build_pool.py` 풀 2,619 → 10 샤드 → Sonnet 병렬 워커 10개(각 cap 100,
  에러 0) → `merge_shards.py` 병합. 결과: **공고 1,000 / 기업 618(dedup)**. 커버리지: category 개발 100%,
  thumbnail 100%, geo 99.7%(3건 누락), skill 50%(508건). 보강: enriched 249 / name_mismatch 228 / not_found 141.
  무결성 검증 통과: 공고·기업 중복 0, 필수필드 누락 0, **mismatch/not_found 레코드의 메타 오염 0**, enriched 전건 name_match=True.
