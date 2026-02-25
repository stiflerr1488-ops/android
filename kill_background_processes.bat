@echo off
chcp 65001 >nul
echo ========================================
echo  БЫСТРАЯ ОЧИСТКА ФОНОВЫХ ПРОЦЕССОВ
echo ========================================
echo.
echo Это закроет ненужные процессы и освободит память
echo.

echo [1/5] Закрытие Skrinshoter...
taskkill /F /IM Skrinshoter.exe >nul 2>&1 && echo   OK: Skrinshoter закрыт || echo   Пропущено: не найден

echo [2/5] Закрытие Hone лаунчера...
taskkill /F /IM Hone.exe >nul 2>&1 && echo   OK: Hone закрыт || echo   Пропущено: не найден

echo [3/5] Закрытие BatteryMode...
taskkill /F /IM BatteryMode64.exe >nul 2>&1 && echo   OK: BatteryMode закрыт || echo   Пропущено: не найден

echo [4/5] Закрытие Widgets...
taskkill /F /IM Widgets.exe >nul 2>&1 && echo   OK: Widgets закрыты || echo   Пропущено: не найден

echo [5/5] Перезапуск проводника...
taskkill /F /IM explorer.exe >nul 2>&1
timeout /t 2 /nobreak >nul
start explorer.exe
echo   OK: Проводник перезапущен

echo.
echo ========================================
echo  ГОТОВО!
echo ========================================
echo.
echo Текущее использование памяти:
powershell -Command "Get-Process | Measure-Object -Property WorkingSet64 -Sum | Select-Object @{N='RAM_GB';E={[math]::Round($_.Sum/1GB,2)}}"
echo.
echo ВАЖНО: VS Code (4.65 ГБ) требует ручной перезагрузки!
echo Закройте и откройте VS Code для сброса памяти.
echo.
pause
