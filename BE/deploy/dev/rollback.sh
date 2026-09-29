#!/usr/bin/env bash
# 방금 전환한 색상에 문제가 있을 때, 재배포 없이 즉시 이전 색상으로 되돌린다.
# 새 이미지를 올리거나 헬스체크를 하지 않고 nginx만 되돌리므로 몇 초 내로 끝난다.
#
# 사용법: ./rollback.sh

set -euo pipefail
cd "$(dirname "$0")"

STATE_FILE=".active_color"

current=$(cat "$STATE_FILE" 2>/dev/null || echo "blue")
if [ "$current" = "blue" ]; then
  previous="green"
else
  previous="blue"
fi

echo "[rollback] reverting nginx from ${current} to ${previous}"
cp "nginx/conf.d/${previous}.conf" "nginx/conf.d/active.conf"
docker compose exec -T nginx nginx -s reload

echo "$previous" > "$STATE_FILE"
echo "[rollback] done. active=${previous}"
