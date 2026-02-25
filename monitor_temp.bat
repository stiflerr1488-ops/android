@echo off
setlocal enabledelayedexpansion

echo ========================================
echo  Монитор температуры CPU
echo ========================================
echo.
echo Нажмите Ctrl+C для выхода
echo.

:loop
for /f "tokens=2" %%a in ('wmic /namespace:\\root\wmi PATH MSAcpi_ThermalZoneTemperature get CurrentTemperature ^| findstr "^[0-9]"') do (
    set /a temp_c=(%%a-2731)/10
    set /a temp_f=%%a
)

powershell -Command "
$java = Get-Process java -ErrorAction SilentlyContinue | Measure-Object WorkingSet64 -Sum | Select-Object -ExpandProperty Sum
$gradle = Get-Process gradle -ErrorAction SilentlyContinue | Measure-Object WorkingSet64 -Sum | Select-Object -ExpandProperty Sum
$code = Get-Process Code -ErrorAction SilentlyContinue | Measure-Object WorkingSet64 -Sum | Select-Object -ExpandProperty Sum
$totalRam = ($java + $gradle + $code) / 1MB
Write-Host 'RAM (Java+Gradle+VSCode): ' ([math]::Round(\$totalRam, 0)) 'MB' -ForegroundColor Cyan
"

echo CPU Температура: %temp_c% C
if %temp_c% LSS 60 (
    echo Статус: [OK] Норма
) else if %temp_c% LSS 80 (
    echo Статус: [WARNING] Нагрузка
) else (
    echo Статус: [CRITICAL] Перегрев!
)
echo --------
timeout /t 5 /nobreak >nul
cls
goto loop
