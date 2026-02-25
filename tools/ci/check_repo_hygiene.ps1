$ErrorActionPreference = "Stop"

$failed = $false

function Assert-NotTracked {
    param([string]$Path)
    $previousErrorActionPreference = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    & git ls-files --error-unmatch -- $Path 2>$null | Out-Null
    $exitCode = $LASTEXITCODE
    $ErrorActionPreference = $previousErrorActionPreference
    if ($exitCode -eq 0) {
        Write-Error "File must not be tracked in git: $Path"
        $script:failed = $true
    }
}

function Assert-NoTrackedPattern {
    param([string]$Pattern)
    $matches = git ls-files -- $Pattern
    if (-not [string]::IsNullOrWhiteSpace($matches)) {
        Write-Error "Tracked files matched forbidden pattern: $Pattern"
        $matches | ForEach-Object { Write-Error $_ }
        $script:failed = $true
    }
}

Assert-NotTracked "app/google-services.json"
Assert-NotTracked "google-services.json"
Assert-NotTracked '$null'

Assert-NoTrackedPattern "build_log.txt"
Assert-NoTrackedPattern "*_compile_log*.txt"
Assert-NoTrackedPattern "*_test_log.txt"
Assert-NoTrackedPattern "_shots/*"

if ($failed) {
    Write-Error "Repository hygiene checks failed."
    exit 1
}

Write-Output "Repository hygiene checks passed."
