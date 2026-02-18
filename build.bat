@echo off
setlocal EnableExtensions

REM ==========================================================
REM TeamCompass: build DEBUG APK (creates APK file only)
REM - No adb, no install
REM - Auto-detects JDK (prefers Android Studio embedded JDK)
REM - Auto-detects Android SDK and writes local.properties
REM - Keeps window open (relaunches under cmd /k)
REM Output:
REM   dist\TeamCompass_debug.apk
REM ==========================================================

if /I "%~1"=="__inside" goto inside
cmd /k ""%~f0" __inside"
exit /b

:inside
cd /d "%~dp0"

echo.
echo ==========================================
echo   TeamCompass DEBUG APK build
echo   Project: %CD%
echo ==========================================
echo.

if not exist "gradlew.bat" goto err_no_gradle

call :detect_java_home
if "%JAVA_HOME%"=="" goto err_no_java
if not exist "%JAVA_HOME%\bin\java.exe" goto err_no_java

set "PATH=%JAVA_HOME%\bin;%PATH%"
echo [ OK ] Using JAVA_HOME: %JAVA_HOME%
echo.
"%JAVA_HOME%\bin\java.exe" -version
echo.

call :ensure_android_sdk
if "%SDK_DIR%"=="" goto err_no_sdk

echo.
echo [INFO] Running: gradlew :app:assembleDebug
echo.
call gradlew.bat --no-daemon :app:assembleDebug --stacktrace
if errorlevel 1 goto err_gradle

set "APK=app\build\outputs\apk\debug\app-debug.apk"
if not exist "%APK%" goto err_no_apk

REM Ensure dist
if not exist "dist" mkdir "dist" >nul 2>nul
copy /Y "%APK%" "dist\TeamCompass_debug.apk" >nul

echo.
echo [ OK ] APK created: dist\TeamCompass_debug.apk
echo.
echo Done.
pause
exit /b 0

:detect_java_home
set "JAVA_HOME="
for /f "usebackq delims=" %%H in (`powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='SilentlyContinue'; $c=@(); if($env:JAVA_HOME){$c+=Join-Path $env:JAVA_HOME 'bin\\java.exe'}; $cmd=Get-Command java -ErrorAction SilentlyContinue; if($cmd){$c+=$cmd.Source}; $c+=@('C:\\Program Files\\Android\\Android Studio\\jbr\\bin\\java.exe','C:\\Program Files\\Android\\Android Studio\\jre\\bin\\java.exe','C:\\Program Files (x86)\\Android\\Android Studio\\jbr\\bin\\java.exe','C:\\Program Files (x86)\\Android\\Android Studio\\jre\\bin\\java.exe'); $tb=Join-Path $env:LOCALAPPDATA 'JetBrains\\Toolbox\\apps\\AndroidStudio'; if(Test-Path $tb){ $j=Get-ChildItem -Path $tb -Recurse -Filter java.exe -ErrorAction SilentlyContinue | Where-Object { $_.FullName -match '\\jbr\\bin\\java.exe$' } | Select-Object -First 1; if($j){$c+=$j.FullName} }; foreach($p in $c){ if($p -and (Test-Path $p)){ $jh=Split-Path (Split-Path $p -Parent) -Parent; Write-Output $jh; break } }"`) do set "JAVA_HOME=%%H"
exit /b 0

:ensure_android_sdk
set "SDK_DIR="

REM 1) (ignored) local.properties may contain escaped paths; use env/default detection
REM 2) Env vars
if "%SDK_DIR%"=="" if not "%ANDROID_SDK_ROOT%"=="" set "SDK_DIR=%ANDROID_SDK_ROOT%"
if "%SDK_DIR%"=="" if not "%ANDROID_HOME%"=="" set "SDK_DIR=%ANDROID_HOME%"

REM 3) Default Android Studio locations
if "%SDK_DIR%"=="" if not "%LOCALAPPDATA%"=="" if exist "%LOCALAPPDATA%\Android\Sdk" set "SDK_DIR=%LOCALAPPDATA%\Android\Sdk"
if "%SDK_DIR%"=="" if exist "%USERPROFILE%\AppData\Local\Android\Sdk" set "SDK_DIR=%USERPROFILE%\AppData\Local\Android\Sdk"
if "%SDK_DIR%"=="" if exist "C:\Android\Sdk" set "SDK_DIR=C:\Android\Sdk"

REM Clean quotes
set "SDK_DIR=%SDK_DIR:"=%"

if "%SDK_DIR%"=="" exit /b 0
if not exist "%SDK_DIR%" (
  set "SDK_DIR="
  exit /b 0
)

REM Write local.properties (always) with a safe escaped sdk.dir (Windows-friendly)
for /f "usebackq delims=" %%P in (`powershell -NoProfile -ExecutionPolicy Bypass -Command "$p='%SDK_DIR%'; $p=$p.Replace('\','/'); if($p -match '^[A-Za-z]:'){ $p=$p.Substring(0,1) + '\:' + $p.Substring(2) }; 'sdk.dir='+$p"`) do set "SDK_LINE=%%P"
> local.properties echo %SDK_LINE%
echo [ OK ] Wrote local.properties: %SDK_LINE%
echo [ OK ] Android SDK: %SDK_DIR%
exit /b 0


:err_no_gradle
echo [ERROR] gradlew.bat not found.
echo         Put build.bat in the Android project root (same folder as gradlew.bat).
echo.
pause
exit /b 1

:err_no_java
echo [ERROR] Java (JDK) not found.
echo.
echo Install Android Studio OR install JDK 17.
echo If you have Android Studio, open it once and set:
echo   Settings ^> Build Tools ^> Gradle ^> Gradle JDK: Embedded JDK
echo.
pause
exit /b 1

:err_no_sdk
echo [ERROR] Android SDK location not found.
echo.
echo Fix options:
echo   1) Android Studio ^> Settings ^> Android SDK (SDK Manager)  (install SDK)
echo   2) Set ANDROID_SDK_ROOT to your SDK path, e.g.:
echo      C:\Users\%USERNAME%\AppData\Local\Android\Sdk
echo   3) Or create local.properties next to gradlew.bat with:
echo      sdk.dir=C:/Users/%USERNAME%/AppData/Local/Android/Sdk
echo.
pause
exit /b 1

:err_gradle
echo.
echo [ERROR] Gradle build failed. Scroll up for the FIRST error.
echo.
pause
exit /b 1

:err_no_apk
echo.
echo [ERROR] APK not found at expected path:
echo         %APK%
echo.
pause
exit /b 1
