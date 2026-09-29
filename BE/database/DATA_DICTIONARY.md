# Job.is 공고 DB — 데이터 사전 (백엔드 인계용)

Job.is MVP의 **IT/개발 직군 채용공고** 데이터셋. 초개인화 큐레이션의 후보 데이터 소스.

- **출처 아키텍처**: 원티드 = 공고 본체(JD·직무·지역·경력·스킬·썸네일·좌표) / 잡코리아 = 기업 메타 보강(사원수·규모·업종·상장·주소)
- **스냅샷**: 공고 **1,000** · 기업 **618** (수집 기준일 2026-06-30)
- 테이블 2개: `job_postings` ─(company_id FK)→ `companies`

## 스냅샷 커버리지

| 항목 | 값 |
|---|---|
| category_parent = 개발 | 1000/1000 (100%) |
| JD 5필드·position·career·employment_type | 100% |
| thumbnail_url | 1000/1000 (100%) |
| geo_lat/lng | 997/1000 (99.7%) |
| skill_tags 有 | 954/1000 (95%) — 원티드 API 508 + NLP 추론 446 |
| 기업 보강 enriched | 249/618 (40%) — 나머지는 원티드 industry로 fallback |
| **급여** | **없음**(원티드 비공개) |

## 테이블: `job_postings` (공고, 원티드)

| 컬럼 | 타입 | 의미 / 주의 |
|---|---|---|
| id | bigserial PK | |
| source | text | 'wanted' 고정 |
| external_id | bigint | 원티드 wd id. `(source, external_id)` UNIQUE |
| source_url | text | 원문 URL |
| company_id | bigint FK→companies.id | 전건 결선됨(NULL 없음) |
| position | text | 직무명 |
| intro / main_tasks / requirements / preferred_points / benefits | text | **JD 의미단위 5필드**. 임베딩·추천이유 생성 재료 |
| category_parent | text | '개발' 고정(MVP IT한정) |
| category_child | text[] | 세부직군 (예: {iOS 개발자}) |
| career_min / career_max | int | 경력 **연수(년)**. ⚠️ **연봉 아님** |
| is_newbie / is_expert | bool | 신입/전문가 가능 |
| employment_type | text | regular 등 |
| location_country/city/district/full | text | 근무지 |
| geo_lat / geo_lng | float8 | 근무지 좌표(거리기반 매칭). 3건 NULL |
| is_remote | bool | |
| due_time | timestamptz | **NULL = 상시채용** |
| confirm_time | timestamptz | 게시확정일 |
| status | text | active 등 |
| hire_rounds | text | 전형절차 |
| skill_tags | text[] | 스킬명. **하드필터 시 `skills_inferred=false`만 신뢰 권장** |
| skill_tag_ids | int[] | 원티드 canonical 스킬 id(동의어 정규화용). NLP 추론분은 매핑된 것만 |
| skills_inferred | bool | **true=NLP 텍스트 추론(저신뢰) / false=원티드 API 태그(정답)** |
| thumbnail_url | text | 회사 브랜딩 배너(카드 썸네일). JD 정보 아님 |
| image_urls | text[] | 전체 이미지 |
| reward_total | text | 추천보상금 (예: "100만원") |
| raw | jsonb | 원본(현재 미적재, NULL) |
| collected_at | timestamptz | 수집시각 |

## 테이블: `companies` (기업, 원티드 식별 + 잡코리아 메타)

| 컬럼 | 타입 | 의미 / 주의 |
|---|---|---|
| id | bigserial PK | |
| name | text | 회사명 원문 |
| normalized_name | text UNIQUE | 매칭 키(괄호별칭·법인표기·공백 제거). 조인은 `company_id` 사용 |
| registration_number | text | 사업자등록번호 |
| wanted_company_id | bigint | 원티드 회사 id |
| jobkorea_gno_ref | bigint | 메타 추출한 잡코리아 공고 Gno(출처추적) |
| employee_count | int | 사원수 ★ |
| company_type | text | 대기업/중견기업/중소기업/벤처기업 ★ |
| industry | text | 업종(잡코리아 우선, 없으면 원티드) |
| stock_status | text | 코스피/코스닥/비상장/- ★ |
| hq_address | text | 본사주소 ★ |
| homepage | text | (미적재) |
| description | text | 회사소개(원티드) |
| enrichment_status | text CHECK | 아래 enum |
| name_match | bool | 원티드명↔잡코리아명 일치 |
| rejected_name | text | name_mismatch 시 잡코리아가 준 엉뚱한 회사명(감사용) |
| enriched_at | timestamptz | (미적재) |
| raw_jobkorea | jsonb | 잡코리아 CORP_INFO 원본 |

★ = `enrichment_status='enriched'` 인 행에서만 채워짐.

### `enrichment_status` enum

| 값 | 의미 | ★ 메타 |
|---|---|---|
| `enriched` | 잡코리아 회사명 일치 → 메타 채움 | O |
| `name_mismatch` | 후보 있었으나 이름 불일치 → **메타 미채움**, rejected_name만 | NULL |
| `not_found` | 잡코리아 검색결과 없음 → 메타 미채움 | NULL |
| `pending` | (미보강 기본값. 현재 스냅샷엔 없음) | NULL |

> ⚠️ **무결성 보장**: name_mismatch/not_found 행은 employee_count 등 ★ 필드가 **반드시 NULL**이다
> (틀린 회사 값을 저장하지 않음). 보강 안 된 기업은 `industry`(원티드 업종)로 fallback.

## 조인

```sql
SELECT j.*, c.name, c.employee_count, c.company_type
FROM job_postings j JOIN companies c ON c.id = j.company_id;
```

## 알려진 갭 (제품 결정, 차단요소 아님)

1. **급여 없음** — 원티드가 비공개. 초개인화에서 유일하게 못 채우는 축. "비공개" 전제로 설계.
2. **skill 95%** — 446건은 NLP 추론(`skills_inferred=true`). 정밀 하드필터는 API 태그(false)만, 의미매칭/임베딩엔 전량 활용 가능. 나머지 46건은 텍스트에 인식 가능한 스킬 없음.
3. **기업 보강 40%** — name_mismatch/not_found 대부분은 잡코리아에 공고 없는 기업. `industry`로 fallback됨. 더 높이려면 사업자번호 기반 매칭 필요.
4. **geo 3건 NULL** — 원티드가 좌표 미제공.
