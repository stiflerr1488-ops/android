param(
    [switch]$FullSuite
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

$testClass = "com.example.teamcompass.TeamCompassInteractionTest"

Write-Host "Installing debug app on connected device..."
./gradlew :app:installDebug --no-daemon

if ($FullSuite) {
    Write-Host "Running full connected androidTest suite..."
    ./gradlew :app:connectedDebugAndroidTest --no-daemon
} else {
    Write-Host "Running smoke UI tests: $testClass"
    ./gradlew :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=$testClass" --no-daemon
}

Write-Host "Done."
