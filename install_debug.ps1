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

& "$PSScriptRoot\gradlew.bat" ":app:installDebug"
