#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR=$(cd "$(dirname "$0")/.." && pwd)
INFRA_DIR="$ROOT_DIR/infra"
LOG_DIR="$ROOT_DIR/.local-logs"

SERVICES=(
  "auth-service"
  "user-service"
  "placement-service"
  "project-service"
  "wallet-service"
  "gateway-service"
)

ensure_log_dir() {
  mkdir -p "$LOG_DIR"
}

compose_up_all() {
  echo "[compose] Building and starting all services via docker compose..."
  docker compose -f "$INFRA_DIR/docker-compose.yml" up -d --build
  echo "[compose] Services are starting. Use: docker compose -f $INFRA_DIR/docker-compose.yml ps"
}

compose_up_core() {
  echo "[compose] Starting core infra (mysql, redis, discovery, config)..."
  docker compose -f "$INFRA_DIR/docker-compose.yml" up -d --build mysql redis discovery-service config-service
}

compose_down() {
  echo "[compose] Stopping compose stack..."
  docker compose -f "$INFRA_DIR/docker-compose.yml" down
}

run_service_dev() {
  local module="$1"
  local log_file="$LOG_DIR/${module}.log"
  echo "[dev] Starting $module ... logs: $log_file"
  nohup mvn -q -f "$ROOT_DIR/$module/pom.xml" spring-boot:run > "$log_file" 2>&1 &
}

run_all_dev() {
  ensure_log_dir
  if ! command -v mvn >/dev/null 2>&1; then
    echo "[dev] Apache Maven (mvn) is required on PATH to run services locally."
    exit 1
  fi
  compose_up_core
  # give discovery and config a moment to come up
  echo "[dev] Waiting 8s for discovery/config to initialize..."
  sleep 8
  for svc in "${SERVICES[@]}"; do
    run_service_dev "$svc"
  done
  echo "[dev] All services started in background. View logs with: $0 logs"
}

show_logs() {
  ensure_log_dir
  echo "[logs] Tailing logs (Ctrl-C to stop)"
  tail -n 200 -f "$LOG_DIR"/*.log
}

usage() {
  cat <<EOF
Usage: $(basename "$0") <command>
Commands:
  compose   Build and run ALL services with Docker Compose
  dev       Run infra (mysql, redis, discovery, config) in Docker, Java services via Maven locally
  down      Stop Docker Compose stack
  logs      Tail logs for Java services started in dev mode

Examples:
  chmod +x scripts/run-all.sh
  scripts/run-all.sh compose
  scripts/run-all.sh dev
  scripts/run-all.sh logs
  scripts/run-all.sh down
EOF
}

cmd="${1:-}" || true
case "$cmd" in
  compose)
    compose_up_all
    ;;
  dev)
    run_all_dev
    ;;
  down)
    compose_down
    ;;
  logs)
    show_logs
    ;;
  *)
    usage
    exit 1
    ;;
 esac
