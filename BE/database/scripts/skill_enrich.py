"""NLP skill 보강: 빈 skill_tags 를 requirements/preferred_points 텍스트에서 채운다.

방식(하이브리드의 사전매칭 단계):
- 어휘: 이미 채워진 원티드 skill_tags(canonical title→id)를 시드로 사용.
- 별칭: 한글 음차/약어(자바→Java, 쿠버네티스→Kubernetes, k8s→Kubernetes 등) 매핑.
- 생성어(개발/서버/보안/영상/회로/로봇 등)는 오탐 방지로 제외.
- 짧고 모호한 토큰(C/R/Go/ML/BI 등)은 나열/언어 문맥일 때만 매칭.
- 채운 공고는 skills_inferred=true 로 표시(백엔드가 신뢰도 구분 가능).

원본 API 태그(있는 공고)는 건드리지 않는다(빈 공고만 보강).

사용:
  python skill_enrich.py --in ../out/job_postings.jsonl            # dry-run(검수)
  python skill_enrich.py --in ../out/job_postings.jsonl --apply    # 적용(백업 후 덮어씀)
"""
from __future__ import annotations
import argparse, json, re, shutil, sys
from collections import Counter

DENY = {"개발", "서버", "보안", "영상", "회로", "로봇", "BI", "Rx",
        # 생성어/너무 광범위 — 추론 태그로는 노이즈(기존 API태그 공고 원본은 유지)
        "인프라", "디자인", "API", "REST", "백엔드 개발", "프론트엔드 개발",
        "웹 개발", "앱 개발", "서비스 기획", "기획"}
AMBIG = {"C", "R", "Go", "ML", "UX", "C#", "C++"}  # 나열/문맥 필요(단, C#/C++는 기호로 구분되어 안전)

# 한글 음차·약어 → canonical title
ALIAS = {
    "자바스크립트": "JavaScript", "자바": "Java", "파이썬": "Python", "타입스크립트": "TypeScript",
    "리액트 네이티브": "React Native", "리액트네이티브": "React Native", "리액트": "React",
    "노드js": "Node.js", "노드": "Node.js", "스프링 부트": "Spring Boot", "스프링부트": "Spring Boot",
    "스프링": "Spring Framework", "장고": "Django", "도커": "Docker", "쿠버네티스": "Kubernetes",
    "리눅스": "Linux", "안드로이드": "Android", "코틀린": "Kotlin", "스위프트": "Swift",
    "고랭": "Go", "골랭": "Go", "골랑": "Go", "러스트": "Rust", "몽고디비": "MongoDB", "몽고": "MongoDB",
    "포스트그레스": "PostgreSQL", "포스트그레": "PostgreSQL", "레디스": "Redis", "카프카": "Kafka",
    "텐서플로우": "Tensorflow", "텐서플로": "Tensorflow", "파이토치": "PyTorch",
    "k8s": "Kubernetes", "restful": "Restful API", "rest api": "Restful API", "rdbms": "RDBMS",
    "rdb": "RDBMS", "nodejs": "Node.js", "springboot": "Spring Boot", "postgres": "PostgreSQL",
}


def build_vocab(posts):
    """채워진 skill_tags → {canonical_title: id}."""
    vocab = {}
    for p in posts:
        for t, i in zip(p.get("skill_tags") or [], p.get("skill_tag_ids") or []):
            if t not in DENY:
                vocab[t] = i
    return vocab


def compile_matchers(vocab):
    """각 스킬/별칭에 대한 (canonical, id, regex) 리스트."""
    terms = {}                         # surface_form -> canonical
    for t in vocab:
        terms[t] = t
    for a, c in ALIAS.items():
        terms[a] = c
    out = []
    for surf, canon in terms.items():
        if surf in DENY:
            continue
        if re.search(r"[가-힣]", surf):        # 한글: 경계 없이 부분일치
            rx = re.compile(re.escape(surf))
        elif surf in AMBIG and surf not in ("C#", "C++"):
            # 나열/언어 문맥에서만: 앞뒤가 구분자이거나 '언어/개발' 인접
            rx = re.compile(r"(?:^|[\s,/·(\[])" + re.escape(surf) +
                            r"(?=[\s,/·)\]]|$|\s*언어|\s*개발|/)")
        else:                                  # 일반 ASCII: 기호 포함 경계
            rx = re.compile(r"(?<![A-Za-z0-9+#.])" + re.escape(surf) +
                            r"(?![A-Za-z0-9+#.])", re.I)
        out.append((canon, vocab.get(canon), rx))
    return out


def extract(text, matchers):
    found = {}
    if not text:
        return found
    for canon, cid, rx in matchers:
        if rx.search(text):
            found[canon] = cid
    return found


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--in", dest="inp", required=True)
    ap.add_argument("--apply", action="store_true")
    ap.add_argument("--fields", default="requirements,preferred_points")
    a = ap.parse_args()

    posts = [json.loads(l) for l in open(a.inp, encoding="utf-8")]
    vocab = build_vocab(posts)
    matchers = compile_matchers(vocab)
    fields = a.fields.split(",")
    print(f"어휘 {len(vocab)} · 매처 {len(matchers)} · 대상필드 {fields}", file=sys.stderr)

    filled = 0
    before = sum(1 for p in posts if p.get("skill_tags"))
    add_counter = Counter()
    samples = []
    for p in posts:
        if p.get("skill_tags"):
            p["skills_inferred"] = False
            continue
        text = " ".join(p.get(f) or "" for f in fields)
        found = extract(text, matchers)
        if found:
            # id가 None이 아닌 (tag, id) 쌍만 남김
            valid_found = {k: v for k, v in found.items() if v is not None}
            # 정제된 데이터로 대입
            p["skill_tags"] = list(valid_found.keys())
            p["skill_tag_ids"] = list(valid_found.values())
            p["skills_inferred"] = True
            filled += 1
            add_counter.update(found.keys())
            if len(samples) < 14:
                samples.append((p["position"][:34], list(found.keys())))
        else:
            p["skills_inferred"] = False

    after = sum(1 for p in posts if p.get("skill_tags"))
    n = len(posts)

    if n == 0:
        print("\n=== dry-run 검수 표본 (position → 추출 스킬) ===")
        print("데이터가 없습니다.")
        print(f"\nskill 커버리지: 0/0(0%) → 0/0(0%)  (+{filled}건 보강)")
    else:
        print("\n=== dry-run 검수 표본 (position → 추출 스킬) ===")
        for pos, sk in samples:
            print(f"  {pos:36} {sk}")
        print(f"\n추가로 많이 잡힌 스킬 top20: {add_counter.most_common(20)}")
        print(f"\nskill 커버리지: {before}/{n}({100*before//n}%) → {after}/{n}({100*after//n}%)  (+{filled}건 보강)")

    if a.apply:
        shutil.copy(a.inp, a.inp + ".bak")
        with open(a.inp, "w", encoding="utf-8") as f:
            for p in posts:
                f.write(json.dumps(p, ensure_ascii=False) + "\n")
        print(f"\n적용 완료 → {a.inp} (백업: {a.inp}.bak)")
    else:
        print("\n(dry-run) --apply 로 반영")


if __name__ == "__main__":
    main()
