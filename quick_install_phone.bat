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

set "LOG_FILE=%~dp0quick_install_phone.log"
echo ================================================== > "%LOG_FILE%"
echo quick_install_phone started at %date% %time% >> "%LOG_FILE%"
echo Working directory: %cd% >> "%LOG_FILE%"
echo ================================================== >> "%LOG_FILE%"

echo Installing debug build to connected device...
echo [INFO] Logging full output to: "%LOG_FILE%"
call "%~dp0gradlew.bat" :app:installDebug --stacktrace >> "%LOG_FILE%" 2>&1

if errorlevel 1 (
  echo.
  echo [ERROR] Install failed.
  echo [ERROR] Full log: "%LOG_FILE%"
  echo.
  echo Common checks:
  echo   1) USB debugging is enabled
  echo   2) Device is connected ^(adb devices^)
  echo   3) Gradle sync/build is successful
  echo.
  echo Press any key to keep this window from closing...
  pause >nul
  exit /b 1
)

echo.
echo [DONE] App installed successfully.
echo [INFO] Full log: "%LOG_FILE%"
echo.
echo Press any key to close this window...
pause >nul
endlocal
