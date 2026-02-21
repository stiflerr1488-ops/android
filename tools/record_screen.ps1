param(
    [int]$DurationSec = 20,
    [string]$OutputDir = "captures/video",
    [string]$FileName = "",
    [switch]$FallbackToBurst,
    [int]$BurstCount = 40,
    [int]$BurstIntervalMs = 220
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

if ($DurationSec -lt 1) { throw "DurationSec must be >= 1" }
if ($DurationSec -gt 180) { $DurationSec = 180 }

if ([string]::IsNullOrWhiteSpace($FileName)) {
    $FileName = "screen_{0}.mp4" -f (Get-Date -Format "yyyyMMdd_HHmmss")
}
if (-not $FileName.ToLowerInvariant().EndsWith(".mp4")) {
    $FileName = "$FileName.mp4"
}

New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

$adb = Get-AdbPath
& $adb start-server | Out-Null

$deviceLine = (& $adb devices) -match "device$" | Select-Object -First 1
if (-not $deviceLine) { throw "No connected Android device" }

$remote = "/sdcard/Download/$FileName"
$local = Join-Path $OutputDir $FileName

Write-Output "Recording for $DurationSec sec..."
$recordCmd = "screenrecord --time-limit $DurationSec $remote; echo __EXIT:`$?"
$prevErrorAction = $ErrorActionPreference
$ErrorActionPreference = "Continue"
$recordOut = & $adb shell $recordCmd 2>&1
$recordExitCode = $LASTEXITCODE
$ErrorActionPreference = $prevErrorAction

$recordOk = $false
if ($recordOut) {
    $recordText = ($recordOut | Out-String)
    if ($recordExitCode -eq 0 -and $recordText -notmatch "Segmentation fault" -and $recordText -match "__EXIT:0") {
        $recordOk = $true
    }
}

if ($recordOk) {
    & $adb pull $remote $local | Out-Null
    & $adb shell rm $remote | Out-Null
    if (Test-Path $local) {
        Write-Output ("Saved video to: {0}" -f (Resolve-Path $local))
        exit 0
    }
}

& $adb shell "rm -f $remote" 2>$null | Out-Null
Write-Output "screenrecord failed on this device."
if ($recordOut) {
    Write-Output ($recordOut | Out-String)
}

if ($FallbackToBurst) {
    $sessionDir = "captures/screens/session_{0}" -f (Get-Date -Format "yyyyMMdd_HHmmss")
    Write-Output "Falling back to burst screenshots..."
    & "$PSScriptRoot\capture_burst.ps1" -Count $BurstCount -IntervalMs $BurstIntervalMs -OutputDir $sessionDir
    exit 0
}

Write-Output "Tip: use .\\tools\\capture_burst.ps1 -Count 60 -IntervalMs 180"
exit 1
