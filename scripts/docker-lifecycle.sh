#!/usr/bin/env bash
# ==============================================================================
# Docker Container Lifecycle Management Script for Motor Insurance Claim Portal
# ==============================================================================
set -euo pipefail

IMAGE_NAME="motorclaim/portal"
VERSION="1.0.0"
CONTAINER_NAME="motorclaim-portal-app"
PORT=8080

echo ">>> [1/8] Building multi-stage Docker image: ${IMAGE_NAME}:${VERSION}..."
docker build -t "${IMAGE_NAME}:${VERSION}" -t "${IMAGE_NAME}:latest" .

echo ">>> [2/8] Inspecting built Docker image metadata..."
docker images | grep "${IMAGE_NAME}" || true
docker inspect --format='Architecture: {{.Architecture}} | OS: {{.Os}} | Size: {{.Size}} bytes' "${IMAGE_NAME}:${VERSION}"

echo ">>> [3/8] Running container on mapped port ${PORT}:8080..."
# Remove any conflicting prior container
docker rm -f "${CONTAINER_NAME}" 2>/dev/null || true
docker run -d \
  --name "${CONTAINER_NAME}" \
  -p "${PORT}:8080" \
  -e SERVER_PORT=8080 \
  --restart unless-stopped \
  "${IMAGE_NAME}:${VERSION}"

echo ">>> [4/8] Waiting for container initialization & health check probe..."
sleep 15
docker ps --filter "name=${CONTAINER_NAME}"

echo ">>> [5/8] Querying application health probe inside container..."
curl -s "http://localhost:${PORT}/actuator/health" || true
echo ""

echo ">>> [6/8] Fetching container logs..."
docker logs --tail 25 "${CONTAINER_NAME}"

echo ">>> [7/8] Testing container lifecycle: Stop and Restart..."
echo "Stopping container..."
docker stop "${CONTAINER_NAME}"
echo "Restarting container..."
docker restart "${CONTAINER_NAME}"
sleep 5

echo ">>> [8/8] Container cleanup demonstration..."
read -p "Press [Enter] to stop and remove container..." || true
docker stop "${CONTAINER_NAME}"
docker rm "${CONTAINER_NAME}"
echo ">>> Lifecycle demonstration completed successfully."
