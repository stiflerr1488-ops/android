@echo off
setlocal enabledelayedexpansion

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
