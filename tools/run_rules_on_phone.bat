@echo off
setlocal

set "ADB=%ANDROID_SDK_ROOT%\platform-tools\adb.exe"
if not exist "%ADB%" (
  set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
)

if not exist "%ADB%" (
  echo adb.exe not found. Set ANDROID_SDK_ROOT or install Android SDK platform-tools.
  exit /b 1
)

"%ADB%" reverse tcp:9000 tcp:9000
if errorlevel 1 exit /b 1

"%ADB%" reverse tcp:9099 tcp:9099
if errorlevel 1 exit /b 1

call gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.teamcompass.FirebaseRulesEmulatorTest --no-daemon
exit /b %errorlevel%
