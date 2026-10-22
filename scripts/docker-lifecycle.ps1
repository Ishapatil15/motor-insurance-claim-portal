# ==============================================================================
# PowerShell Docker Container Lifecycle Management Script
# Motor Insurance Claim Portal
# ==============================================================================
param(
    [string]$ImageName = "motorclaim/portal",
    [string]$Version = "1.0.0",
    [string]$ContainerName = "motorclaim-portal-app",
    [int]$Port = 8080
)

# Pre-flight check: Verify if Docker CLI is installed and running
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host ""
    Write-Host "=======================================================================" -ForegroundColor Red
    Write-Host "[!] ERROR: Docker is not installed or not added to your system PATH." -ForegroundColor Red
    Write-Host "=======================================================================" -ForegroundColor Red
    Write-Host "To run Docker containers on Windows:" -ForegroundColor Yellow
    Write-Host "  1. Install Docker Desktop using winget:  winget install Docker.DockerDesktop" -ForegroundColor White
    Write-Host "     Or download from: https://www.docker.com/products/docker-desktop/" -ForegroundColor White
    Write-Host "  2. Start Docker Desktop and ensure the Docker daemon is running." -ForegroundColor White
    Write-Host "  3. Re-run this script: .\scripts\docker-lifecycle.ps1" -ForegroundColor White
    Write-Host ""
    Write-Host "NOTE: You can run the application directly without Docker using:" -ForegroundColor Cyan
    Write-Host "  java -jar target\claim-portal.jar --server.port=8080" -ForegroundColor Green
    Write-Host "=======================================================================" -ForegroundColor Red
    exit 1
}

$ErrorActionPreference = "Continue"

Write-Host ">>> [1/7] Building multi-stage Docker image: ${ImageName}:${Version}..." -ForegroundColor Cyan
docker build -t "${ImageName}:${Version}" -t "${ImageName}:latest" .

Write-Host ">>> [2/7] Inspecting Docker image details..." -ForegroundColor Cyan
docker images | Select-String "motorclaim"

Write-Host ">>> [3/7] Launching fresh container on port ${Port}:8080..." -ForegroundColor Cyan
docker stop $ContainerName 2>$null
docker rm -f $ContainerName 2>$null
docker run -d --name $ContainerName -p "${Port}:8080" -e SERVER_PORT=8080 "${ImageName}:${Version}"

Write-Host ">>> [4/7] Awaiting container startup (15s)..." -ForegroundColor Cyan
Start-Sleep -Seconds 15
docker ps --filter "name=$ContainerName"

Write-Host ">>> [5/7] Querying health check probe..." -ForegroundColor Cyan
try {
    $res = Invoke-RestMethod -Uri "http://localhost:${Port}/actuator/health" -TimeoutSec 5
    Write-Host "Health Probe Output: " ($res | ConvertTo-Json) -ForegroundColor Green
} catch {
    Write-Host "Could not query health probe: $_" -ForegroundColor Yellow
}

Write-Host ">>> [6/7] Inspecting recent container logs..." -ForegroundColor Cyan
docker logs --tail 20 $ContainerName

Write-Host ">>> [7/7] Demonstrating stop, restart, and remove..." -ForegroundColor Cyan
docker stop $ContainerName
docker restart $ContainerName
Start-Sleep -Seconds 3
docker stop $ContainerName
docker rm $ContainerName
Write-Host ">>> Container lifecycle demonstration completed!" -ForegroundColor Green
