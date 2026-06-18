param(
    [ValidateSet("run", "copy", "none")]
    [string] $DeployMode = "",

    [string] $DeployDir = "",

    [int] $Port = 8081,

    [switch] $SkipTests,

    [switch] $StopExisting,

    [switch] $StopJavaTools
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepoRoot = Split-Path -Parent $PSScriptRoot
$TargetDir = Join-Path $RepoRoot "target"
$ArtifactName = "myportfolio-0.0.1-SNAPSHOT.war"
$ArtifactPath = Join-Path $TargetDir $ArtifactName

function Write-Step {
    param([string] $Message)
    Write-Host ""
    Write-Host "==> $Message"
}

function Remove-TargetDirectory {
    if (-not (Test-Path -LiteralPath $TargetDir)) {
        Write-Host "target directory does not exist; nothing to delete."
        return
    }

    for ($Attempt = 1; $Attempt -le 5; $Attempt++) {
        try {
            & attrib.exe -R -S -H -P -U "$TargetDir" /S /D 2>$null

            Get-ChildItem -LiteralPath $TargetDir -Recurse -Force -ErrorAction SilentlyContinue |
                ForEach-Object {
                    try {
                        $_.Attributes = $_.Attributes -band (-bnot [System.IO.FileAttributes]::ReadOnly)
                    }
                    catch {
                    }
                }

            Remove-Item -LiteralPath $TargetDir -Recurse -Force -ErrorAction Stop
            Write-Host "Deleted target directory."
            return
        }
        catch {
            try {
                [System.IO.Directory]::Delete($TargetDir, $true)
                Write-Host "Deleted target directory."
                return
            }
            catch {
            }

            try {
                $LongTargetDir = "\\?\$TargetDir"
                Remove-Item -LiteralPath $LongTargetDir -Recurse -Force -ErrorAction Stop
                Write-Host "Deleted target directory."
                return
            }
            catch {
            }

            if ($Attempt -eq 5) {
                throw "Could not delete '$TargetDir'. Close any running app, terminal, IDE process, or file explorer window using target, then try again. Last error: $($_.Exception.Message)"
            }

            Write-Host "Delete attempt $Attempt failed; retrying..."
            Start-Sleep -Seconds 2
        }
    }
}

function Stop-JavaToolsForRepo {
    $Processes = Get-CimInstance Win32_Process -Filter "name = 'java.exe' or name = 'javaw.exe'" -ErrorAction SilentlyContinue |
        Where-Object { $_.CommandLine -and $_.CommandLine.Contains($RepoRoot) }

    foreach ($Process in $Processes) {
        if ($Process.ProcessId -and $Process.ProcessId -ne $PID) {
            Write-Host "Stopping Java tooling process $($Process.ProcessId) using this repo."
            Stop-Process -Id $Process.ProcessId -Force
        }
    }
}

function Get-MavenCommand {
    $Mvn = Get-Command "mvn.cmd" -ErrorAction SilentlyContinue
    if ($Mvn) {
        return $Mvn.Source
    }

    $WrapperProperties = Join-Path $RepoRoot ".mvn\wrapper\maven-wrapper.properties"
    if (Test-Path -LiteralPath $WrapperProperties) {
        $Properties = Get-Content -Raw -LiteralPath $WrapperProperties | ConvertFrom-StringData
        $DistributionUrl = $Properties.distributionUrl

        if ($DistributionUrl) {
            $DistributionFile = Split-Path -Leaf $DistributionUrl
            $DistributionName = $DistributionFile -replace "-bin\.zip$", "" -replace "\.zip$", ""
            $WrapperDists = Join-Path $env:USERPROFILE ".m2\wrapper\dists"

            $CachedMaven = Get-ChildItem -LiteralPath $WrapperDists -Recurse -Filter "mvn.cmd" -ErrorAction SilentlyContinue |
                Where-Object { $_.FullName -like "*$DistributionName*" } |
                Select-Object -First 1

            if ($CachedMaven) {
                return $CachedMaven.FullName
            }
        }
    }

    $Wrapper = Join-Path $RepoRoot "mvnw.cmd"
    if (Test-Path -LiteralPath $Wrapper) {
        return $Wrapper
    }

    throw "Maven was not found. Install Maven or keep mvnw.cmd in the project root."
}

function Stop-ProcessOnPort {
    param([int] $TargetPort)

    $Connections = Get-NetTCPConnection -LocalPort $TargetPort -State Listen -ErrorAction SilentlyContinue
    $ProcessIds = $Connections | Select-Object -ExpandProperty OwningProcess -Unique

    foreach ($ProcessId in $ProcessIds) {
        if ($ProcessId -and $ProcessId -ne $PID) {
            Write-Host "Stopping process $ProcessId listening on port $TargetPort."
            Stop-Process -Id $ProcessId -Force
        }
    }
}

Push-Location $RepoRoot
try {
    if ($StopJavaTools) {
        Write-Step "Stopping Java tooling for this repo"
        Stop-JavaToolsForRepo
    }

    Write-Step "Deleting target"
    Remove-TargetDirectory

    Write-Step "Building package"
    $MavenCommand = Get-MavenCommand
    $MavenArgs = @("package")
    if ($SkipTests) {
        $MavenArgs += "-DskipTests"
    }

    & $MavenCommand @MavenArgs
    if ($LASTEXITCODE -ne 0) {
        throw "Maven package failed with exit code $LASTEXITCODE."
    }

    if (-not (Test-Path -LiteralPath $ArtifactPath)) {
        throw "Build completed, but '$ArtifactPath' was not created."
    }

    if (-not $DeployMode) {
        if ($DeployDir -or $env:DEPLOY_DIR -or $env:CATALINA_HOME -or $env:TOMCAT_HOME) {
            $DeployMode = "copy"
        }
        else {
            $DeployMode = "run"
        }
    }

    if ($DeployMode -eq "none") {
        Write-Step "Skipping deploy"
        Write-Host "Built artifact: $ArtifactPath"
        exit 0
    }

    if ($DeployMode -eq "copy") {
        Write-Step "Deploying WAR by copy"

        if (-not $DeployDir) {
            if ($env:DEPLOY_DIR) {
                $DeployDir = $env:DEPLOY_DIR
            }
            elseif ($env:CATALINA_HOME) {
                $DeployDir = Join-Path $env:CATALINA_HOME "webapps"
            }
            elseif ($env:TOMCAT_HOME) {
                $DeployDir = Join-Path $env:TOMCAT_HOME "webapps"
            }
        }

        if (-not $DeployDir) {
            throw "DeployMode is copy, but no deploy directory was provided. Pass -DeployDir or set DEPLOY_DIR, CATALINA_HOME, or TOMCAT_HOME."
        }

        New-Item -ItemType Directory -Path $DeployDir -Force | Out-Null
        Copy-Item -LiteralPath $ArtifactPath -Destination (Join-Path $DeployDir $ArtifactName) -Force
        Write-Host "Deployed to: $(Join-Path $DeployDir $ArtifactName)"
        exit 0
    }

    Write-Step "Deploying by running executable WAR"
    if ($StopExisting) {
        Stop-ProcessOnPort -TargetPort $Port
    }

    $OutLogPath = Join-Path $TargetDir "myportfolio.out.log"
    $ErrLogPath = Join-Path $TargetDir "myportfolio.err.log"
    $JavaArgs = @("-jar", "`"$ArtifactPath`"", "--server.port=$Port")
    $Process = Start-Process -FilePath "java" -ArgumentList ($JavaArgs -join " ") -WorkingDirectory $RepoRoot -RedirectStandardOutput $OutLogPath -RedirectStandardError $ErrLogPath -PassThru

    Write-Host "Started PID $($Process.Id) on port $Port."
    Write-Host "Output log: $OutLogPath"
    Write-Host "Error log: $ErrLogPath"
}
finally {
    Pop-Location
}
