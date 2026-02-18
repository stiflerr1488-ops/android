@echo off
setlocal EnableExtensions

rem Always run from this script's directory
cd /d "%~dp0"

rem === TeamCompass build verifier ===
rem 1) assembles debug with full diagnostics
rem 2) optionally installs to a connected device

set "JBR=C:\Program Files\Android\Android Studio\jbr"

if not exist "%JBR%\bin\java.exe" (
  echo.
  echo [ERROR] Cannot find Java at: %JBR%\bin\java.exe
  echo Edit verify_build.bat and set JBR to your Android Studio jbr path.
  echo Example: set "JBR=C:\Program Files\Android\Android Studio\jbr"
  echo.
  pause
  exit /b 1
)

set "JAVA_HOME=%JBR%"
set "PATH=%JAVA_HOME%\bin;%PATH%"

rem === Ensure Android SDK path (local.properties) ===
rem local.properties is machine-specific. We (re)create it if missing OR empty.
call :ensureSdk

echo Using JAVA_HOME=%JAVA_HOME%
echo.

echo === 1/2 Assemble Debug (full diagnostics) ===
call gradlew.bat :app:assembleDebug --warning-mode all --stacktrace
if errorlevel 1 (
  echo.
  echo [ERROR] Build failed. Scroll up for the first error block.
  echo.
  pause
  exit /b 1
)

echo.
choice /M "Build OK. Install to connected device now?"
if errorlevel 2 goto end

echo.
echo === 2/2 Install Debug ===
call gradlew.bat :app:installDebug --warning-mode all --stacktrace
if errorlevel 1 (
  echo.
  echo [ERROR] Install failed. If build succeeded, check USB debugging and device authorization.
  echo.
  pause
  exit /b 1
)

echo.
echo [OK] Installed successfully.
:end
pause
endlocal

goto :eof

:ensureSdk
set "SDKDIR="
if exist "local.properties" (
  for /f "usebackq tokens=1* delims==" %%A in (`findstr /b /i "sdk.dir=" local.properties 2^>nul`) do set "SDKDIR=%%B"
)

rem If sdk.dir is missing/empty or the folder doesn't exist, generate a correct one.
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

set "SDK_BS=%SDK%"
if not exist "%SDK_BS%" (
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
