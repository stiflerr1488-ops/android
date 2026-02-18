# TeamCompass quick installer (debug)
# Uses JAVA_HOME if set, otherwise tries Android Studio embedded JBR.
$ErrorActionPreference = "Stop"

if (-not $env:JAVA_HOME -or $env:JAVA_HOME.Trim().Length -eq 0) {
  $jbr = Join-Path $env:ProgramFiles "Android\Android Studio\jbr"
  if (Test-Path (Join-Path $jbr "bin\java.exe")) {
    $env:JAVA_HOME = $jbr
  }
}

if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME "bin\java.exe"))) {
  Write-Host "[ERROR] JAVA_HOME is not set and JBR not found." -ForegroundColor Red
  Write-Host "Install Android Studio or JDK 17+ and set JAVA_HOME."
  exit 1
}

$env:Path = "$env:JAVA_HOME\bin;$env:Path"
Write-Host "Using JAVA_HOME=$env:JAVA_HOME"

# Ensure local.properties (SDK path) exists. It's machine-specific and may be missing after unzipping.
$lp = Join-Path $PSScriptRoot "local.properties"
function Ensure-AndroidSdkLocalProperties {
  $needWrite = $true
  if (Test-Path $lp) {
    $line = (Get-Content $lp -ErrorAction SilentlyContinue | Where-Object { $_ -match '^sdk\.dir=' } | Select-Object -First 1)
    if ($line) {
      $val = ($line -split '=', 2)[1]
      if ($val -and $val.Trim().Length -gt 0) {
        $candidate = $val.Trim().Replace('/', '\\')
        if (Test-Path $candidate) { $needWrite = $false }
      }
    }
  }

  if ($needWrite) {
    $sdk = $env:ANDROID_SDK_ROOT
    if (-not $sdk -or $sdk.Trim().Length -eq 0) { $sdk = $env:ANDROID_HOME }
    if (-not $sdk -or $sdk.Trim().Length -eq 0) {
      $local = $env:LOCALAPPDATA
      if (-not $local -or $local.Trim().Length -eq 0) { $local = Join-Path $env:USERPROFILE 'AppData\Local' }
      $sdk = Join-Path $local "Android\Sdk"
    }
    if (-not (Test-Path $sdk)) {
      Write-Host "[ERROR] Android SDK not found. Install Android SDK via Android Studio or set ANDROID_HOME/ANDROID_SDK_ROOT." -ForegroundColor Red
      Write-Host "Expected default path: $sdk" -ForegroundColor Yellow
      exit 1
    }
    $sdk = $sdk -replace "\\", "/"
    Set-Content -Path $lp -Value "sdk.dir=$sdk"
  }
}

Ensure-AndroidSdkLocalProperties

& "$PSScriptRoot\gradlew.bat" ":app:installDebug"
