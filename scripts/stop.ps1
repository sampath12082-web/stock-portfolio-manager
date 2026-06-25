# Stop SoloSprint Trade
Write-Host "Stopping SoloSprint Trade..." -ForegroundColor Cyan

$javaProcesses = Get-Process -Name java -ErrorAction SilentlyContinue
if ($javaProcesses) {
    $javaProcesses | Stop-Process -Force
    Write-Host "[OK] Backend stopped ($($javaProcesses.Count) process(es))" -ForegroundColor Green
} else {
    Write-Host "[OK] No backend process running" -ForegroundColor Gray
}
