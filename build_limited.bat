@echo off
echo ========================================
echo  Компиляция с ограничением ресурсов
echo ========================================
echo.

REM Ограничиваем память Java через переменную окружения
set GRADLE_OPTS=-Xmx1024m -XX:MaxMetaspaceSize=512m

REM Запускаем Gradle в фоне с низким приоритетом
echo Запуск компиляции...
echo.

powershell -Command "
$process = Start-Process -FilePath 'gradlew.bat' -ArgumentList 'assembleDebug' -PassThru -WindowStyle Normal
Start-Sleep -Seconds 2
$process.PriorityClass = 'BelowNormal'
Write-Host 'Процесс запущен с приоритетом BelowNormal'
Write-Host 'PID:' \$process.Id
"

echo.
echo Компиляция запущена. Окно можно закрыть.
