# Start SoloSprint Trade locally
Write-Host "Starting SoloSprint Trade..." -ForegroundColor Cyan

# Check PostgreSQL
try {
    $null = & psql -h localhost -U sampat -d myportfolio -c "SELECT 1" 2>&1
    Write-Host "[OK] PostgreSQL connected" -ForegroundColor Green
} catch {
    Write-Host "[WARN] Cannot connect to PostgreSQL — make sure it's running on localhost:5432" -ForegroundColor Yellow
}

# Build frontend
Write-Host "Building frontend..." -ForegroundColor Cyan
Set-Location "$PSScriptRoot\..\frontend"
npm run build 2>&1 | Select-Object -Last 2

# Start backend
Write-Host "Starting backend on port 8081..." -ForegroundColor Cyan
Set-Location "$PSScriptRoot\..\backend"
Start-Process -NoNewWindow -FilePath "cmd.exe" -ArgumentList "/c", "mvnw.cmd spring-boot:run" -PassThru | Out-Null

# Wait for backend
Write-Host "Waiting for backend..." -ForegroundColor Cyan
for ($i = 1; $i -le 30; $i++) {
    Start-Sleep -Seconds 3
    try {
        $resp = Invoke-WebRequest -Uri "http://localhost:8081/api/auth/public-key" -UseBasicParsing -TimeoutSec 2 -ErrorAction Stop
        if ($resp.StatusCode -eq 200) {
            Write-Host "[OK] Backend ready!" -ForegroundColor Green
            Write-Host ""
            Write-Host "  App:   http://localhost:8081" -ForegroundColor White
            Write-Host "  Login: sampath12082@gmail.com" -ForegroundColor White
            Write-Host ""
            Write-Host "To stop: .\scripts\stop.ps1" -ForegroundColor Gray
            break
        }
    } catch { }
}

Set-Location "$PSScriptRoot\.."
