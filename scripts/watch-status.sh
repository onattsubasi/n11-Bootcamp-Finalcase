#!/usr/bin/env bash
set -euo pipefail

CMD='docker compose ps && echo && docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}\t{{.BlockIO}}"'

if command -v watch >/dev/null 2>&1; then
  watch -n 5 "$CMD"
else
  while true; do
    clear || true
    bash -c "$CMD"
    sleep 5
  done
fi
