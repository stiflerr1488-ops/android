@echo off
echo ========================================
echo  Очистка памяти перед компиляцией
echo ========================================
echo.

echo [1/5] Остановка ненужных служб...
sc stop "WSearch" >nul 2>&1
sc stop "SysMain" >nul 2>&1

echo [2/5] Завершение процессов Yandex...
taskkill /F /IM Yandex.exe >nul 2>&1
taskkill /F /IM browser.exe >nul 2>&1

echo [3/5] Завершение процессов Steam...
taskkill /F /IM steam.exe >nul 2>&1

echo [4/5] Очистка рабочей памяти...
powershell -Command "[System.GC]::Collect()"

echo [5/5] Ожидание 5 секунд...
timeout /t 5 /nobreak >nul

echo.
echo ========================================
echo  Готово! Можно запускать компиляцию
echo ========================================
echo.
echo Текущее использование памяти:
powershell -Command "Get-Process | Measure-Object -Property WorkingSet64 -Sum | Select-Object @{N='RAM_GB';E={[math]::Round($_.Sum/1GB,2)}}"
