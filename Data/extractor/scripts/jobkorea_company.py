"""잡코리아(enrichment) 기업 메타 추출기.

원티드는 기업 메타가 약하다(업종 정도). 잡코리아 공고 RSC의 CORP_INFO 객체에는
사원수·기업규모·업종·상장여부·주소가 정형으로 들어있다(실측 검증).

경로(기업명만 알면 됨):
  1) 잡코리아 통합검색 /Search/?stext={기업명}  → 해당 기업 공고 Gno 추출
  2) /Recruit/GI_Read/{Gno} 의 RSC → CORP_INFO 회사객체 파싱

한계:
- 해당 기업이 잡코리아에 공고가 있어야 함(없으면 not_found → 원티드 industry_name으로 fallback).
- 검색 첫 결과를 쓰므로 동명이인 가능 → 정규화 이름 일치(name_match)로 검증.
"""
from __future__ import annotations
import json
import re
import sys

from common import http_get, rsc_blob, norm_company, quote


def search_gnos(company_name, limit=4):
    """기업명으로 잡코리아 검색 → 후보 공고 Gno 들(순서 보존 dedup, 상위 limit개).
    첫 결과가 무관한 회사일 수 있어, enrich 에서 이름 일치하는 후보를 고른다."""
    html = http_get(f"https://www.jobkorea.co.kr/Search/?stext={quote(company_name)}",
                    referer="https://www.jobkorea.co.kr/")
    raw = re.findall(r"/Recruit/GI_Read/([0-9]+)", html)
    if not raw:
        raw = re.findall(r"GI_Read/([0-9]+)", rsc_blob(html))
    out = []
    for g in raw:                      # 순서 보존 dedup
        if g not in out:
            out.append(g)
        if len(out) >= limit:
            break
    return out


def _posting_company_name(html, blob):
    """공고의 회사명(신뢰): JSON-LD hiringOrganization.name. HTML LD 우선, RSC fallback."""
    for m in re.finditer(r'<script[^>]*application/ld\+json[^>]*>(.*?)</script>', html, re.S):
        try:
            j = json.loads(m.group(1))
        except Exception:
            continue
        for it in (j if isinstance(j, list) else [j]):
            if isinstance(it, dict) and it.get("@type") == "JobPosting":
                org = it.get("hiringOrganization") or {}
                if org.get("name"):
                    return org["name"]
    # fallback: RSC(이스케이프된 JSON) 안의 hiringOrganization name
    m = re.search(r'hiringOrganization\\?"\s*:\s*\{[^}]*?name\\?"\s*:\s*\\?"([^"\\]{1,60})', blob)
    return m.group(1) if m else None


def fetch_corp_info(gno):
    """공고 RSC의 CORP_INFO 회사객체에서 메타 추출. employeeCount 를 앵커로 사용."""
    html = http_get(f"https://www.jobkorea.co.kr/Recruit/GI_Read/{gno}?sc=729&sn=103",
                    referer="https://www.jobkorea.co.kr/")
    blob = rsc_blob(html)
    i = blob.find('"employeeCount"')
    if i < 0:
        return None
    seg = blob[max(0, i - 500):i + 700]

    def s(key):
        m = re.search(r'"' + key + r'"\s*:\s*"([^"]{0,80})"', seg)
        return m.group(1) if m else None

    def n(key):
        m = re.search(r'"' + key + r'"\s*:\s*([0-9]+)', seg)
        return int(m.group(1)) if m else None

    addr = None
    am = re.search(r'"address"\s*:\s*\{[^}]*?"address"\s*:\s*"([^"]{0,80})"'
                   r'(?:[^}]*?"addressDetail"\s*:\s*"([^"]{0,80})")?', seg)
    if am:
        addr = " ".join(p for p in [am.group(1), am.group(2)] if p).strip()

    return {
        "jobkorea_gno_ref": int(gno),
        "company_name_jk": _posting_company_name(html, blob),  # JSON-LD 기준 (신뢰)
        "employee_count": n("employeeCount"),
        "company_type": s("companyTypeName"),   # 대기업/중견기업/중소기업
        "industry": s("industryName"),
        "stock_status": s("stockStatusName"),   # 코스피/코스닥/-
        "hq_address": addr,
    }


def enrich(company_name, max_candidates=4):
    """기업명 → 잡코리아 메타 dict. enrichment_status 포함.

    상위 후보 Gno 들을 훑어 **이름이 일치하는 회사만** 채택한다. 일치가 없으면
    틀린 회사의 사원수·규모를 저장하지 않는다(무결성). status:
      enriched      — 이름 일치, 메타 채움
      name_mismatch — 후보는 있었으나 이름 일치 없음(타사 메타 폐기, 감사용 rejected_name 만 남김)
      not_found     — 검색 결과 없음
    """
    target = norm_company(company_name)
    gnos = search_gnos(company_name, limit=max_candidates)
    if not gnos:
        return {"enrichment_status": "not_found"}

    rejected = None
    for gno in gnos:
        meta = fetch_corp_info(gno)
        if not meta:
            continue
        cand = meta.get("company_name_jk") or ""
        if norm_company(cand) == target and target:
            meta["name_match"] = True
            meta["enrichment_status"] = "enriched"
            meta["raw_jobkorea"] = {k: meta[k] for k in
                                    ("employee_count", "company_type", "industry",
                                     "stock_status", "hq_address", "company_name_jk")}
            return meta
        rejected = rejected or cand  # 첫 후보명만 감사용으로 기록
    # 어떤 후보도 이름이 안 맞음 → 타사 메타는 버린다(저장 금지)
    return {"enrichment_status": "name_mismatch", "name_match": False,
            "rejected_name": rejected}


if __name__ == "__main__":
    name = sys.argv[1] if len(sys.argv) > 1 else "메리츠화재"
    print(json.dumps(enrich(name), ensure_ascii=False, indent=2))
