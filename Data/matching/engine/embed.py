"""공고 임베딩: JD(직무+담당업무+자격+우대+스킬)를 다국어 임베딩 → embeddings.npy + meta.jsonl.

retrieve 단계(의미 유사도)의 재료. 로컬 ONNX 모델(fastembed) 사용 — API 키 불필요.

사용:
  python embed.py --in ../../database/data/job_postings.jsonl --out .
"""
from __future__ import annotations
import argparse, json, os
import numpy as np
from fastembed import TextEmbedding

MODEL = "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"  # 384d, 다국어


def embed_text(p):
    """매칭에 쓸 의미 텍스트(회사 홍보성 intro/benefits 제외 — 직무 신호 집중)."""
    parts = [p.get("position") or ""]
    parts += [p.get("main_tasks") or "", p.get("requirements") or "", p.get("preferred_points") or ""]
    sk = p.get("skill_tags") or []
    if sk:
        parts.append("기술스택: " + ", ".join(sk))
    return "\n".join(x for x in parts if x)[:2000]


def snippet(s, n=280):
    return (s or "").replace("\n", " ").strip()[:n]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--in", dest="inp", default=os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "database", "data", "job_postings.jsonl"))
    ap.add_argument("--companies", default=os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "database", "data", "companies.jsonl"))
    ap.add_argument("--out", default=".")
    a = ap.parse_args()
    posts = [json.loads(l) for l in open(a.inp, encoding="utf-8") if l.strip()]
    # 기업 메타(사원수/규모/업종)는 companies.jsonl 에 있음 → normalized_name 으로 조인
    comps = {}
    for l in open(a.companies, encoding="utf-8"):
        if l.strip():
            cc = json.loads(l)
            comps[cc.get("normalized_name")] = cc
    texts = [embed_text(p) for p in posts]
    print(f"임베딩 {len(texts)}건 (모델 {MODEL}) ...")

    model = TextEmbedding(model_name=MODEL)
    vecs = np.array(list(model.embed(texts)), dtype=np.float32)
    vecs /= (np.linalg.norm(vecs, axis=1, keepdims=True) + 1e-9)   # L2 정규화(코사인=내적)

    os.makedirs(a.out, exist_ok=True)
    np.save(os.path.join(a.out, "embeddings.npy"), vecs)
    with open(os.path.join(a.out, "meta.jsonl"), "w", encoding="utf-8") as f:
        for p in posts:
            c = p.get("company") or {}
            meta = comps.get(p.get("company_normalized_name")) or {}
            f.write(json.dumps({
                "external_id": p.get("external_id"),
                "position": p.get("position"),
                "company": c.get("name"),
                "location_city": p.get("location_city"),
                "location_full": p.get("location_full"),
                "is_remote": p.get("is_remote"),
                "career_min": p.get("career_min"),
                "career_max": p.get("career_max"),
                "skill_tags": p.get("skill_tags") or [],
                "skills_inferred": p.get("skills_inferred"),
                "employee_count": meta.get("employee_count"),
                "company_type": meta.get("company_type"),
                "industry": meta.get("industry") or c.get("industry_name"),
                "source_url": p.get("source_url"),
                "req_snippet": snippet(p.get("requirements")),
                "tasks_snippet": snippet(p.get("main_tasks")),
            }, ensure_ascii=False) + "\n")
    print(f"저장: embeddings.npy {vecs.shape} + meta.jsonl {len(posts)}행 → {a.out}/")


if __name__ == "__main__":
    main()
