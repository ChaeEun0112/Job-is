"""원티드(base 소스) 공고 수집기.

수집 경로 = HTML initialData + 상세 API 병합 (둘 다 필요. 실측 근거):
- 목록 : 공개 JSON API /api/v4/jobs (job_group_id=518 = 개발)
- 상세A: https://www.wanted.co.kr/wd/{id} 의 __NEXT_DATA__ → props.pageProps.initialData
         → category_tag(개발 필터용), company.registration_number(사업자번호) 가 여기에만 있음
- 상세B: /api/v4/jobs/{id} → job.detail(JD 5필드) + skill_tags(+canonical id) + title_img/
         company_images(썸네일) + address.geo_location(위경도). ★ skill_tags·이미지·좌표는
         initialData 엔 필드 자체가 없고 이 API 에만 온다 (초개인화 매칭/썸네일의 핵심 소스).

방어 포인트(실측 근거):
- <script id="__NEXT_DATA__"> 의 id 속성이 가변(crossorigin 추가)이거나, 태그 자체가
  없고 일반 <script>에 들어있는 경우가 있음 → "initialData 포함 모든 script 스캔" fallback.
- career.annual_from/to 는 '경력 연수'이지 연봉이 아니다 (혼동 금지).
- skill_tags: 상세 API 에선 ~30%(개발직군은 더 자주) 채워지며 기업이 임의 태깅이라 노이즈 있음
  → 정형(API) + 비는 부분은 requirements NLP 보강 하이브리드. (initialData 만 보면 항상 null)
- title_img 는 '회사 브랜딩 배너'(JD 정보 아님) → 썸네일용일 뿐, 원티드엔 OCR 불필요.
"""
from __future__ import annotations
import json
import re
import sys

from common import http_get, http_json, norm_company

LIST_API = "https://www.wanted.co.kr/api/v4/jobs"
DETAIL_API = "https://www.wanted.co.kr/api/v4/jobs"  # /{id}
DEV_JOB_GROUP = 518  # 개발

# ⚠️ job_group_id=518(개발) 목록은 직군을 안 거른다(마케터/변호사 등 섞임 — 실측).
# 세부직군 tag_type_id 는 깨끗하게 걸린다 → 개발/IT 풀은 이 태그들의 합집합으로 발견.
# (검증 2026-06-28: 전부 IT/개발/엔지니어 직군 반환. 라벨은 대략치, 합집합 커버리지가 목적.)
DEV_TAG_IDS = [
    660, 669, 671, 672, 674, 676, 677, 678, 872, 873,
    895, 899, 900, 1024, 1026, 1027, 1634, 10110, 939,
]  # 백엔드/프론트/모바일/QA/데이터/ML/AI/DevOps/보안/임베디드/시스템 등


def list_job_ids(limit=20, offset=0, job_group_id=DEV_JOB_GROUP, tag_type_id=None):
    url = (f"{LIST_API}?country=kr&job_group_id={job_group_id}"
           f"&job_sort=job.latest_order&years=-1&locations=all"
           f"&limit={limit}&offset={offset}")
    if tag_type_id is not None:
        url += f"&tag_type_id={tag_type_id}"
    data = http_json(url, referer="https://www.wanted.co.kr/")
    return [j["id"] for j in (data.get("data") or []) if j.get("id")]


def list_dev_job_ids(per_tag=20, max_total=None, tag_ids=DEV_TAG_IDS):
    """개발 세부직군 tag_type_id 들을 순회해 공고 id 수집(순서 보존 dedup).
    목록 단계에서 개발만 추려 사전 fetch 낭비를 없앤다."""
    seen, out = set(), []
    for tid in tag_ids:
        try:
            ids = list_job_ids(limit=per_tag, tag_type_id=tid)
        except Exception as e:
            print(f"[list] tag {tid} 실패: {e}", file=sys.stderr)
            continue
        for jid in ids:
            if jid not in seen:
                seen.add(jid)
                out.append(jid)
                if max_total and len(out) >= max_total:
                    return out
    return out


def extract_initial_data(html):
    candidates = []
    m = re.search(r'<script[^>]*id="__NEXT_DATA__"[^>]*>(.*?)</script>', html, re.S)
    if m:
        candidates.append(m.group(1))
    # fallback: initialData 를 포함한 모든 <script>
    for sm in re.finditer(r"<script[^>]*>(.*?)</script>", html, re.S):
        body = sm.group(1)
        if '"initialData"' in body and '"pageProps"' in body:
            candidates.append(body)
    for c in candidates:
        try:
            d = json.loads(c.strip())
            init = d.get("props", {}).get("pageProps", {}).get("initialData")
            if init:
                return init
        except Exception:
            continue
    raise ValueError("initialData를 찾지 못함")


def fetch_detail(job_id):
    """상세 API job 객체. skill_tags(+id)·이미지·geo_location 의 유일한 소스."""
    data = http_json(f"{DETAIL_API}/{job_id}", referer="https://www.wanted.co.kr/")
    return data.get("job") or {}


def _images(job):
    """썸네일 폴백 체인: title_img → company_images[0] → logo_img(기본 플레이스홀더 제외)."""
    urls = []
    ti = (job.get("title_img") or {}).get("origin")
    if ti:
        urls.append(ti)
    for im in (job.get("company_images") or []):
        u = im.get("url") if isinstance(im, dict) else im
        if u and u not in urls:
            urls.append(u)
    logo = (job.get("logo_img") or {}).get("origin")
    if logo and "/wdes/" not in logo and logo not in urls:  # /wdes/ = 기본 로고
        urls.append(logo)
    return urls


def normalize(init, job=None):
    """init = HTML initialData(필수), job = 상세 API job 객체(skill/이미지/geo 보강용)."""
    job = job or {}
    c = init.get("company") or {}
    cat = init.get("category_tag") or {}
    car = init.get("career") or {}
    addr = init.get("address") or {}
    rew = init.get("reward") or {}
    name = c.get("company_name")

    # kill_tags(정형+id), 이미지, 위경도
    skills = job.get("skill_tags") or []

    # title과 id가 둘 다 제대로 있는 쌍만 먼저 추출
    pairs = [(s["title"], s["id"]) for s in skills if isinstance(s, dict) and s.get("title") and s.get("id")]

    # 추출된 쌍을 기반으로 리스트 분리
    skill_titles = [p[0] for p in pairs]
    skill_ids = [p[1] for p in pairs]

    images = _images(job)
    geo = ((job.get("address") or {}).get("geo_location") or {}).get("location") or {}

    return {
        "source": "wanted",
        "external_id": init.get("id"),
        "source_url": f"https://www.wanted.co.kr/wd/{init.get('id')}",
        "position": init.get("position"),
        "intro": init.get("intro"),
        "main_tasks": init.get("main_tasks"),
        "requirements": init.get("requirements"),
        "preferred_points": init.get("preferred_points"),
        "benefits": init.get("benefits"),
        "category_parent": (cat.get("parent_tag") or {}).get("text"),
        "category_child": [t.get("text") for t in (cat.get("child_tags") or []) if t.get("text")],
        "career_min": car.get("annual_from"),   # 경력 연수 (연봉 아님)
        "career_max": car.get("annual_to"),
        "is_newbie": car.get("is_newbie"),
        "is_expert": car.get("is_expert"),
        "employment_type": init.get("employment_type"),
        "location_country": addr.get("country"),
        "location_city": addr.get("location"),
        "location_district": addr.get("district"),
        "location_full": addr.get("full_location"),
        "geo_lat": geo.get("lat"),               # 상세 API (거리기반 매칭용)
        "geo_lng": geo.get("lng"),
        "is_remote": init.get("is_remote_work"),
        "due_time": init.get("due_time"),
        "confirm_time": init.get("confirm_time"),
        "status": init.get("status"),
        "hire_rounds": init.get("hire_rounds"),
        "reward_total": (rew or {}).get("formatted_total"),
        # 정형 skill (상세 API). 비면 requirements NLP 보강은 별도 단계.
        "skill_tags": skill_titles,
        "skill_tag_ids": skill_ids,              # canonical id (동의어 정규화/매칭)
        "thumbnail_url": images[0] if images else None,
        "image_urls": images,
        "company": {
            "name": name,
            "normalized_name": norm_company(name),
            "registration_number": c.get("registration_number"),  # initialData 에만 있음
            "wanted_company_id": c.get("company_id"),
            "industry_name": c.get("industry_name"),
            "description": c.get("company_description"),
        },
    }


def fetch_posting(job_id):
    """HTML initialData + 상세 API 병합. 상세 API 실패해도 본문은 살림."""
    html = http_get(f"https://www.wanted.co.kr/wd/{job_id}",
                    referer="https://www.wanted.co.kr/")
    init = extract_initial_data(html)
    try:
        job = fetch_detail(job_id)
    except Exception as e:
        print(f"[wanted] {job_id} 상세 API 실패(skill/이미지 누락): {e}", file=sys.stderr)
        job = {}
    return normalize(init, job)


if __name__ == "__main__":
    jid = sys.argv[1] if len(sys.argv) > 1 else list_job_ids(limit=1)[0]
    print(json.dumps(fetch_posting(jid), ensure_ascii=False, indent=2))
