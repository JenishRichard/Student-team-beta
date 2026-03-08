#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Starting build..."

for service in room-service booking-service; do
  echo "Building $service..."
  cd "$ROOT_DIR/services/$service"
  chmod +x mvnw
  ./mvnw clean test package
done

echo "All builds completed successfully"