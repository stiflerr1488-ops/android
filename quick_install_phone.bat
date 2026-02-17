@echo off
setlocal

rem Always run from the repository folder where this script is located.
cd /d "%~dp0"

set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
if exist "%JAVA_HOME%\bin\java.exe" (
  set "PATH=%JAVA_HOME%\bin;%PATH%"
  echo [OK] JAVA_HOME=%JAVA_HOME%
) else (
  echo [WARN] JAVA not found at "%JAVA_HOME%"
  echo [WARN] Using current PATH Java
)

echo Installing debug build to connected device...
call "%~dp0gradlew.bat" :app:installDebug --stacktrace

if errorlevel 1 (
  echo.
  echo [ERROR] Install failed. Make sure:
  echo   1) USB debugging is enabled
  echo   2) Device is connected ^(adb devices^)
  echo   3) Gradle sync/build is successful
  exit /b 1
)

echo.
echo [DONE] App installed successfully.
endlocal
