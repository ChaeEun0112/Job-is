"""공통 HTTP/파싱 헬퍼 (표준 라이브러리만 사용 — 외부 의존성 없음)."""
from __future__ import annotations
import gzip
import json
import re
import urllib.parse
import urllib.request

UA = ("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
      "(KHTML, like Gecko) Chrome/120.0 Safari/537.36")


def http_get(url, referer=None, timeout=30):
    """브라우저 UA로 GET. gzip 응답도 처리. 본문 문자열 반환."""
    req = urllib.request.Request(url, headers={
        "User-Agent": UA,
        "Accept-Language": "ko-KR,ko;q=0.9,en;q=0.8",
    })
    if referer:
        req.add_header("Referer", referer)
    with urllib.request.urlopen(req, timeout=timeout) as r:
        data = r.read()
        if r.headers.get("Content-Encoding") == "gzip":
            data = gzip.decompress(data)
    return data.decode("utf-8", "replace")


def http_json(url, referer=None, timeout=30):
    return json.loads(http_get(url, referer, timeout))


def quote(s):
    return urllib.parse.quote(str(s))


def rsc_blob(html):
    """잡코리아 Next.js(App Router) RSC: self.__next_f.push 조각들을 하나의 문자열로 결합."""
    out = []
    for p in re.findall(r"self\.__next_f\.push\((\[.*?\])\)", html, re.S):
        try:
            for el in json.loads(p):
                if isinstance(el, str):
                    out.append(el)
        except Exception:
            out.append(p)
    return "".join(out)


_PAREN = re.compile(r"\([^)]*\)")  # 영문 별칭 등 괄호군: 넵튠(Neptune)→넵튠, 크몽(kmong)→크몽
_DROP = re.compile(r"㈜|주식회사|유한회사|inc\.?|co\.,?\s*ltd\.?|corp\.?|ltd\.?", re.I)


def norm_company(name):
    """기업명 매칭/조인 키: 괄호별칭·법인 표기·공백 제거 + 소문자.
    원티드 '크몽(kmong)' ↔ 잡코리아 '㈜크몽' 같은 표기 차를 흡수한다."""
    if not name:
        return ""
    n = _PAREN.sub("", name)   # (Neptune)/(kmong)/(주) 등 괄호군 제거
    n = _DROP.sub("", n)       # ㈜/주식회사/Inc/Co.,Ltd 등 법인 표기 제거
    n = re.sub(r"\s+", "", n)
    return n.lower()
