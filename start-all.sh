#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="$ROOT_DIR/.runlogs"
PID_DIR="$ROOT_DIR/.runpids"

mkdir -p "$LOG_DIR" "$PID_DIR"

export DB_HOST="${DB_HOST:-classroom-dev-db.cvwy4uckycwn.eu-west-1.rds.amazonaws.com}"
export DB_PORT="${DB_PORT:-3306}"
export DB_NAME="${DB_NAME:-student_db}"
export AUTH_DB_NAME="${AUTH_DB_NAME:-auth_db}"
export DB_USER="${DB_USER:-admin}"
export DB_PASSWORD="${DB_PASSWORD:-admin123}"
export USE_LOCAL_DOCKER_MYSQL="${USE_LOCAL_DOCKER_MYSQL:-false}"

ensure_docker_ready() {
  if ! command -v docker >/dev/null 2>&1; then
    echo "Docker CLI is not installed or not on PATH."
    echo "Install Docker Desktop: https://docs.docker.com/desktop/setup/install/mac-install/"
    exit 1
  fi

  if docker info >/dev/null 2>&1; then
    return
  fi

  echo "Docker daemon is not running."

  if [[ "${OSTYPE:-}" == darwin* ]] && command -v open >/dev/null 2>&1; then
    echo "Trying to start Docker Desktop..."
    open -a Docker >/dev/null 2>&1 || true

    local max_wait=60
    local waited=0
    until docker info >/dev/null 2>&1; do
      sleep 2
      waited=$((waited + 2))
      if (( waited >= max_wait )); then
        echo "Docker did not become ready within ${max_wait}s."
        echo "Open Docker Desktop manually and wait until it shows 'Engine running', then re-run this script."
        exit 1
      fi
    done

    echo "Docker is now running."
    return
  fi

  echo "Start Docker and re-run this script."
  exit 1
}

start_if_not_running() {
  local name="$1"
  local workdir="$2"
  local cmd="$3"
  local logfile="$LOG_DIR/$name.log"
  local pidfile="$PID_DIR/$name.pid"

  if [[ -f "$pidfile" ]]; then
    local existing_pid
    existing_pid="$(cat "$pidfile")"
    if kill -0 "$existing_pid" 2>/dev/null; then
      echo "$name already running (pid=$existing_pid)"
      return
    fi
  fi

  echo "Starting $name..."
  (
    cd "$workdir"
    nohup bash -lc "$cmd" >"$logfile" 2>&1 &
    echo $! >"$pidfile"
  )
  local started_pid
  started_pid="$(cat "$pidfile")"
  sleep 1
  if kill -0 "$started_pid" 2>/dev/null; then
    echo "$name started (pid=$started_pid)"
  else
    echo "$name failed to start. Check log: $logfile"
    rm -f "$pidfile"
  fi
}

if [[ "$USE_LOCAL_DOCKER_MYSQL" == "true" ]]; then
  echo "Ensuring MySQL is running via docker compose..."
  ensure_docker_ready
  (
    cd "$ROOT_DIR"
    docker compose up -d mysql
  )
else
  echo "Using remote MySQL at ${DB_HOST}:${DB_PORT} (set USE_LOCAL_DOCKER_MYSQL=true for local Docker DB)"
fi

start_if_not_running "discovery-server" "$ROOT_DIR/services/discovery-server" "mvn spring-boot:run"
start_if_not_running "auth-service" "$ROOT_DIR/services/auth-service" "mvn spring-boot:run"
start_if_not_running "room-service" "$ROOT_DIR/services/room-service" "mvn clean spring-boot:run"
start_if_not_running "booking-service" "$ROOT_DIR/services/booking-service" "mvn spring-boot:run"
start_if_not_running "api-gateway" "$ROOT_DIR/services/api-gateway" "mvn spring-boot:run"
start_if_not_running "ui" "$ROOT_DIR/ui" "npm run dev -- --host 127.0.0.1 --port 5173 --strictPort"

echo
echo "Services requested. Check logs in:"
echo "  $LOG_DIR"
echo
echo "Quick port check:"
lsof -nP -iTCP:8761,8081,8083,8084,8085,5173 -sTCP:LISTEN || true
