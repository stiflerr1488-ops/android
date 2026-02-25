@echo off
setlocal enabledelayedexpansion

echo ========================================
echo  Монитор памяти (Ctrl+C для выхода)
echo ========================================
echo.

:loop
powershell -Command "
$java = Get-Process java -ErrorAction SilentlyContinue | Measure-Object WorkingSet64 -Sum | Select-Object -ExpandProperty Sum
$gradle = Get-Process gradle -ErrorAction SilentlyContinue | Measure-Object WorkingSet64 -Sum | Select-Object -ExpandProperty Sum
$code = Get-Process Code -ErrorAction SilentlyContinue | Measure-Object WorkingSet64 -Sum | Select-Object -ExpandProperty Sum
$total = ($java + $gradle + $code) / 1GB
Write-Host 'Java:  ' ([math]::Round(\$java/1MB, 0)) 'MB' -ForegroundColor Yellow
Write-Host 'Gradle:' ([math]::Round(\$gradle/1MB, 0)) 'MB' -ForegroundColor Cyan
Write-Host 'VSCode:' ([math]::Round(\$code/1MB, 0)) 'MB' -ForegroundColor Green
Write-Host 'Вместе:' ([math]::Round(\$total, 2)) 'GB' -ForegroundColor White
Write-Host '--------'
"
timeout /t 3 /nobreak >nul
cls
goto loop
