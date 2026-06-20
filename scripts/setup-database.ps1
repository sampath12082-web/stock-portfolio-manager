param(
    [string] $PgHost = "localhost",
    [int]    $PgPort = 5432,
    [string] $PgUser = "sampat",
    [string] $DbName = "myportfolio",
    [switch] $SkipEnvSetup
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Write-Step {
    param([string] $Message)
    Write-Host ""
    Write-Host "==> $Message" -ForegroundColor Cyan
}

function Test-PsqlAvailable {
    $Psql = Get-Command "psql" -ErrorAction SilentlyContinue
    if ($Psql) {
        return $Psql.Source
    }

    $CommonPaths = @(
        "$env:ProgramFiles\PostgreSQL\*\bin\psql.exe",
        "${env:ProgramFiles(x86)}\PostgreSQL\*\bin\psql.exe",
        "$env:LOCALAPPDATA\Programs\pgAdmin 4\*\runtime\psql.exe"
    )

    foreach ($Pattern in $CommonPaths) {
        $Found = Get-ChildItem -Path $Pattern -ErrorAction SilentlyContinue |
            Sort-Object FullName -Descending |
            Select-Object -First 1
        if ($Found) {
            return $Found.FullName
        }
    }

    return $null
}

# ── Step 1: Find psql ──────────────────────────────────────────────

Write-Step "Locating psql"
$PsqlPath = Test-PsqlAvailable
if (-not $PsqlPath) {
    Write-Host "ERROR: psql not found. Install PostgreSQL or add its bin directory to PATH." -ForegroundColor Red
    Write-Host "Download: https://www.postgresql.org/download/windows/"
    exit 1
}
Write-Host "Found psql at: $PsqlPath"

# ── Step 2: Drop and recreate database ─────────────────────────────

Write-Step "Dropping database '$DbName' (if it exists)"
Write-Host "You may be prompted for the PostgreSQL password for user '$PgUser'."
Write-Host ""

& $PsqlPath -h $PgHost -p $PgPort -U $PgUser -d "postgres" -c "DROP DATABASE IF EXISTS $DbName;"
if ($LASTEXITCODE -ne 0) {
    Write-Host "WARNING: Could not drop database. It may not exist yet, or active connections may be blocking it." -ForegroundColor Yellow
    Write-Host "If the database has active connections, close any running app instances and retry."
    Write-Host ""

    Write-Host "Attempting to terminate active connections..." -ForegroundColor Yellow
    & $PsqlPath -h $PgHost -p $PgPort -U $PgUser -d "postgres" -c @"
SELECT pg_terminate_backend(pid) FROM pg_stat_activity
WHERE datname = '$DbName' AND pid <> pg_backend_pid();
"@
    & $PsqlPath -h $PgHost -p $PgPort -U $PgUser -d "postgres" -c "DROP DATABASE IF EXISTS $DbName;"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Failed to drop database. Please close all connections and try again." -ForegroundColor Red
        exit 1
    }
}
Write-Host "Database dropped."

Write-Step "Creating database '$DbName'"
& $PsqlPath -h $PgHost -p $PgPort -U $PgUser -d "postgres" -c "CREATE DATABASE $DbName;"
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to create database." -ForegroundColor Red
    exit 1
}
Write-Host "Database '$DbName' created."

# ── Step 3: Verify connection ──────────────────────────────────────

Write-Step "Verifying connection to '$DbName'"
& $PsqlPath -h $PgHost -p $PgPort -U $PgUser -d $DbName -c "SELECT 'connection_ok' AS status;" | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Cannot connect to '$DbName'." -ForegroundColor Red
    exit 1
}
Write-Host "Connection verified."

# ── Step 4: Environment variables (optional) ───────────────────────

if (-not $SkipEnvSetup) {
    Write-Step "Setting up environment variables"

    $CurrentDbPass = [System.Environment]::GetEnvironmentVariable("DB_PASSWORD", "User")
    if (-not $CurrentDbPass) {
        $DbPass = Read-Host "Enter your PostgreSQL password for user '$PgUser'"
        if ($DbPass) {
            [System.Environment]::SetEnvironmentVariable("DB_PASSWORD", $DbPass, "User")
            $env:DB_PASSWORD = $DbPass
            Write-Host "DB_PASSWORD set." -ForegroundColor Green
        }
    } else {
        Write-Host "DB_PASSWORD is already set." -ForegroundColor Green
    }

    $CurrentToken = [System.Environment]::GetEnvironmentVariable("GROWW_ACCESS_TOKEN", "User")
    if (-not $CurrentToken) {
        Write-Host "GROWW_ACCESS_TOKEN is not set."
        $Token = Read-Host "Enter your Groww API access token (or press Enter to skip)"
        if ($Token) {
            [System.Environment]::SetEnvironmentVariable("GROWW_ACCESS_TOKEN", $Token, "User")
            $env:GROWW_ACCESS_TOKEN = $Token
            Write-Host "GROWW_ACCESS_TOKEN set." -ForegroundColor Green
        }
    } else {
        Write-Host "GROWW_ACCESS_TOKEN is already set." -ForegroundColor Green
    }

    $CurrentSecret = [System.Environment]::GetEnvironmentVariable("GROWW_API_SECRET", "User")
    if (-not $CurrentSecret) {
        $Secret = Read-Host "Enter your Groww API secret (or press Enter to skip)"
        if ($Secret) {
            [System.Environment]::SetEnvironmentVariable("GROWW_API_SECRET", $Secret, "User")
            $env:GROWW_API_SECRET = $Secret
            Write-Host "GROWW_API_SECRET set." -ForegroundColor Green
        }
    } else {
        Write-Host "GROWW_API_SECRET is already set." -ForegroundColor Green
    }

    $CurrentEnabled = [System.Environment]::GetEnvironmentVariable("GROWW_API_ENABLED", "User")
    if (-not $CurrentEnabled) {
        [System.Environment]::SetEnvironmentVariable("GROWW_API_ENABLED", "true", "User")
        $env:GROWW_API_ENABLED = "true"
        Write-Host "GROWW_API_ENABLED set to true." -ForegroundColor Green
    } else {
        Write-Host "GROWW_API_ENABLED is already '$CurrentEnabled'." -ForegroundColor Green
    }
}

# ── Step 5: Summary ───────────────────────────────────────────────

Write-Step "Setup complete"
Write-Host ""
Write-Host "  Database:    $DbName (empty, ready for Flyway)" -ForegroundColor Green
Write-Host "  Host:        ${PgHost}:${PgPort}" -ForegroundColor Green
Write-Host "  User:        $PgUser" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "  1. Restart your terminal so environment variables take effect"
Write-Host "  2. Run the application:  ./mvnw spring-boot:run"
Write-Host "     Flyway will automatically create all tables (V1-V7)"
Write-Host "  3. Open Swagger UI:      http://localhost:8081/swagger-ui.html"
Write-Host ""
