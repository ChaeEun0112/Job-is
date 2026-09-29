#!/usr/bin/env bash
# blue/green 중 비활성 색상에 새 이미지를 올리고, 헬스체크를 통과하면
# nginx를 그 색상으로 전환한다. 실패하면 아무것도 바꾸지 않고 종료한다.
#
# 사용법: ./switch.sh
# 전제조건: docker compose pull 로 최신 이미지를 이미 받아온 상태여야 함
#          (deploy.yml에서 pull 이후 이 스크립트를 호출)

set -euo pipefail
cd "$(dirname "$0")"

STATE_FILE=".active_color"
HEALTH_RETRIES=30
HEALTH_INTERVAL=2

current=$(cat "$STATE_FILE" 2>/dev/null || echo "blue")
if [ "$current" = "blue" ]; then
  target="green"
  target_port=18081
else
  target="blue"
  target_port=18080
fi

echo "[switch] current active: $current / target: $target"

echo "[switch] recreating api-${target} with latest image"
docker compose up -d --force-recreate "api-${target}"

echo "[switch] waiting for api-${target} health check (max $((HEALTH_RETRIES * HEALTH_INTERVAL))s)"
healthy=false
for i in $(seq 1 "$HEALTH_RETRIES"); do
  if curl -fs "http://localhost:${target_port}/health" > /dev/null; then
    healthy=true
    break
  fi
  sleep "$HEALTH_INTERVAL"
done

if [ "$healthy" != "true" ]; then
  echo "[switch] api-${target} failed health check. aborting, nginx still points to ${current}."
  exit 1
fi

echo "[switch] api-${target} healthy. flipping nginx from ${current} to ${target}"
cp "nginx/conf.d/${target}.conf" "nginx/conf.d/active.conf"
docker compose exec -T nginx nginx -s reload

echo "$target" > "$STATE_FILE"
echo "[switch] done. active=${target}, standby=${current} (${current} left running for quick rollback)"
