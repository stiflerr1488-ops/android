param(
    [int]$Count = 30,
    [int]$IntervalMs = 250,
    [string]$OutputDir = "",
    [switch]$KeepPng
)

$ErrorActionPreference = "Stop"

function Get-AdbPath {
    $line = (Get-Content local.properties | Select-String '^sdk.dir=' | Select-Object -First 1)
    if (-not $line) { throw "local.properties does not contain sdk.dir" }

    $sdkRaw = $line.ToString().Split('=')[1].Trim()
    $sdk = $sdkRaw.Replace('\:', ':').Replace('\\', '\')
    $adb = Join-Path $sdk 'platform-tools\adb.exe'
    if (-not (Test-Path $adb)) { throw "adb not found: $adb" }
    return $adb
}

if ($Count -lt 1) { throw "Count must be >= 1" }
if ($IntervalMs -lt 50) { throw "IntervalMs must be >= 50" }

if ([string]::IsNullOrWhiteSpace($OutputDir)) {
    $OutputDir = "captures/screens/session_{0}" -f (Get-Date -Format "yyyyMMdd_HHmmss")
}

New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

$adb = Get-AdbPath
& $adb start-server | Out-Null

$deviceLine = (& $adb devices) -match "device$" | Select-Object -First 1
if (-not $deviceLine) { throw "No connected Android device" }

Add-Type -AssemblyName System.Drawing

$saved = 0

for ($i = 1; $i -le $Count; $i++) {
    $stamp = Get-Date -Format "yyyyMMdd_HHmmss_fff"
    $base = "frame_{0:D4}_{1}" -f $i, $stamp
    $remote = "/sdcard/Download/$base.png"
    $localPng = Join-Path $OutputDir "$base.png"
    $localJpg = Join-Path $OutputDir "$base.jpg"

    & $adb shell screencap -p $remote | Out-Null
    & $adb pull $remote $localPng | Out-Null
    & $adb shell rm $remote | Out-Null

    if (-not (Test-Path $localPng)) { continue }
    if ((Get-Item $localPng).Length -le 0) {
        Remove-Item $localPng -ErrorAction SilentlyContinue
        continue
    }

    $img = $null
    try {
        $img = [System.Drawing.Image]::FromFile($localPng)
        $img.Save($localJpg, [System.Drawing.Imaging.ImageFormat]::Jpeg)
        $saved++
    } finally {
        if ($img -ne $null) { $img.Dispose() }
    }

    if (-not $KeepPng) {
        Remove-Item $localPng -ErrorAction SilentlyContinue
    }

    Start-Sleep -Milliseconds $IntervalMs
}

Write-Output ("Saved {0}/{1} frames to: {2}" -f $saved, $Count, (Resolve-Path $OutputDir))
