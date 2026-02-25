$ErrorActionPreference = "Stop"

if (Test-Path "app/google-services.json") {
    Write-Output "Using checked-out app/google-services.json."
    exit 0
}

if (Test-Path "app/google-services.json.example") {
    Copy-Item -Path "app/google-services.json.example" -Destination "app/google-services.json" -Force
    Write-Output "Prepared app/google-services.json from template."
    exit 0
}

Write-Error "Missing app/google-services.json and app/google-services.json.example"
exit 1

