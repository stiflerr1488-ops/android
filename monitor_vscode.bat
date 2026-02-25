@echo off
chcp 65001 >nul
echo ========================================
echo  МОНИТОР VS CODE
echo ========================================
echo.

:loop
cls
echo ========================================
echo  МОНИТОР VS CODE
echo ========================================
echo.
echo Дата: %date% %time%
echo.

powershell -Command "
$code = Get-Process Code -ErrorAction SilentlyContinue
if ($code) {
    $totalRam = ($code | Measure-Object WorkingSet64 -Sum).Sum / 1GB
    $totalCpu = ($code | Measure-Object CPU -Sum).Sum
    Write-Host 'Процессов VS Code:' $code.Count
    Write-Host 'RAM:' ([math]::Round($totalRam, 2)) 'ГБ'
    Write-Host 'CPU (всего):' ([math]::Round($totalCpu, 1)) 'сек'
} else {
    Write-Host 'VS Code не запущен' -ForegroundColor Red
}
"

echo.
echo Температура CPU:
wmic /namespace:\\root\wmi PATH MSAcpi_ThermalZoneTemperature get CurrentTemperature | findstr "^[0-9]" > %TEMP%\temp.txt
for /f %%a in (%TEMP%\temp.txt) do set /a temp_c=(%%a-2731)/10
echo %temp_c% C
del %TEMP%\temp.txt 2>nul

echo.
echo Нажмите Ctrl+C для выхода
timeout /t 5 /nobreak >nul
goto loop
