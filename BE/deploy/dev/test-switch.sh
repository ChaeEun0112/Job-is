#!/usr/bin/env bash
# 전환(switch.sh) 도중 실제로 요청이 끊기는지 확인하는 스크립트.
#
# 사용법:
#   터미널 1: ./test-switch.sh          (계속 nginx에 요청을 쏘며 실패를 기록)
#   터미널 2: ./switch.sh               (전환 실행)
#   터미널 1에서 Ctrl+C로 종료 -> 실패 횟수 확인
#
# 실패(FAIL)가 0이면 무중단 전환 성공.

set -uo pipefail

total=0
fail=0

cleanup() {
  echo
  echo "[test] total=${total} fail=${fail}"
  if [ "$fail" -eq 0 ]; then
    echo "[test] 무중단 전환 성공 (실패 0건)"
  else
    echo "[test] 전환 도중 실패 ${fail}건 발생 - switch.sh 타이밍/헬스체크 조건 점검 필요"
  fi
  exit 0
}
trap cleanup INT TERM

echo "[test] http://localhost/health 로 100ms 간격 요청 시작 (Ctrl+C로 종료)"
while true; do
  total=$((total + 1))
  code=$(curl -s -o /dev/null -w "%{http_code}" --max-time 1 http://localhost/health)
  if [ "$code" != "200" ]; then
    fail=$((fail + 1))
    echo "$(date +%T) FAIL (code=${code:-timeout})"
  fi
  sleep 0.1
done
