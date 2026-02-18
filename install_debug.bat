@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0"

REM TeamCompass quick installer (debug)
REM - tries to use JAVA_HOME, otherwise falls back to Android Studio embedded JBR
REM - then runs :app:installDebug

if not defined JAVA_HOME (
  set "JBR=%ProgramFiles%\Android\Android Studio\jbr"
  if exist "%JBR%\bin\java.exe" (
    set "JAVA_HOME=%JBR%"
  )
)

if not defined JAVA_HOME (
  echo [ERROR] JAVA_HOME is not set, and Android Studio JBR was not found.
  echo Install Android Studio or JDK 17+ and set JAVA_HOME.
  pause
  exit /b 1
)

set "PATH=%JAVA_HOME%\bin;%PATH%"

REM === Ensure Android SDK path (local.properties) ===
REM local.properties is machine-specific. We (re)create it if missing OR empty.
call :ensureSdk

echo Using JAVA_HOME=%JAVA_HOME%
call "%~dp0gradlew.bat" :app:installDebug
set EXITCODE=%ERRORLEVEL%

echo.
if %EXITCODE%==0 (
  echo ✅ Installed successfully.
) else (
  echo ❌ Install failed with code %EXITCODE%.
)
pause
exit /b %EXITCODE%

goto :eof

:ensureSdk
set "SDKDIR="
if exist "local.properties" (
  for /f "usebackq tokens=1* delims==" %%A in (`findstr /b /i "sdk.dir=" local.properties 2^>nul`) do set "SDKDIR=%%B"
)

set "SDKDIR_BS=%SDKDIR:/=\%"
if not defined SDKDIR goto :writeSdk
if not exist "%SDKDIR_BS%" goto :writeSdk
goto :eof

:writeSdk
if not defined LOCALAPPDATA set "LOCALAPPDATA=%USERPROFILE%\AppData\Local"

set "SDK="
if defined ANDROID_HOME set "SDK=%ANDROID_HOME%"
if defined ANDROID_SDK_ROOT set "SDK=%ANDROID_SDK_ROOT%"
if not defined SDK set "SDK=%LOCALAPPDATA%\Android\Sdk"

if not exist "%SDK%" (
  echo.
  echo [ERROR] Android SDK not found.
  echo Set ANDROID_HOME or ANDROID_SDK_ROOT, or install Android SDK via Android Studio.
  echo Expected default path: %LOCALAPPDATA%\Android\Sdk
  echo.
  exit /b 1
)

set "SDK=%SDK:\=/%"
echo sdk.dir=%SDK%> local.properties
goto :eof
