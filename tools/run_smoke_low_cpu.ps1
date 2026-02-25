param(
    [ValidateSet("guards", "compile", "unit", "lint", "androidTest", "assembleDebug", "assembleRelease")]
    [string]$Check = "guards",
    [string]$AndroidTestClass = "com.example.teamcompass.TeamCompassSmokeTest"
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

$commonGradleArgs = @(
    "--no-daemon",
    "--max-workers=1",
    "-Dorg.gradle.parallel=false",
    "-Dkotlin.compiler.execution.strategy=in-process"
)

function Invoke-Gradle {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$Tasks,
        [string[]]$ExtraArgs = @()
    )

    Write-Host ("Running low-load Gradle check: {0}" -f (($Tasks + $ExtraArgs) -join " "))
    & .\gradlew @Tasks @ExtraArgs @commonGradleArgs
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
}

switch ($Check) {
    "guards" {
        Write-Host "Running low-load static guard checks..."
        powershell -ExecutionPolicy Bypass -File .\tools\ci\run_guard_checks.ps1
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    }
    "compile" {
        Invoke-Gradle -Tasks @(":app:compileDebugKotlin", ":app:compileDebugAndroidTestKotlin")
    }
    "unit" {
        Invoke-Gradle -Tasks @(":core:test", ":app:testDebugUnitTest")
    }
    "lint" {
        Invoke-Gradle -Tasks @(":app:lintDebug")
    }
    "androidTest" {
        Invoke-Gradle -Tasks @(":app:connectedDebugAndroidTest") -ExtraArgs @(
            "-PandroidTestClass=$AndroidTestClass",
            "-PandroidTestArgs=teamcompass.test.hermetic=true,teamcompass.test.disable_telemetry=true"
        )
    }
    "assembleDebug" {
        Invoke-Gradle -Tasks @(":app:assembleDebug")
    }
    "assembleRelease" {
        Invoke-Gradle -Tasks @(":app:assembleRelease")
    }
    default {
        throw "Unsupported check: $Check"
    }
}

Write-Host "Low-load check completed: $Check"
