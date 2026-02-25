@echo off
chcp 65001 >nul
echo ========================================
echo  ГЛАВНЫЙ СКРИПТ ОПТИМИЗАЦИИ
echo  Для Android-разработки на 8GB RAM
echo ========================================
echo.
echo Этот скрипт:
echo  1. Применит все настройки реестра
echo  2. Отключит ненужные службы
echo  3. Настроит файл подкачки
echo  4. Оптимизирует электропитание
echo.
echo ВНИМАНИЕ: Требуется перезагрузка после применения!
echo.
pause

echo.
echo ========================================
echo [1/6] Применение настроек реестра...
echo ========================================
reg import "%~dp0windows_tweak.reg"

echo.
echo ========================================
echo [2/6] Отключение служб...
echo ========================================
sc config "SysMain" start=disabled
sc config "WSearch" start=disabled
sc stop "WSearch"
sc stop "SysMain"

echo.
echo ========================================
echo [3/6] Настройка файла подкачки...
echo ========================================
powershell -Command "
$computer = Get-CimInstance Win32_ComputerSystem
$computer.AutomaticManagedPagefile = $false
$computer.Put()
"
wmic pagefileset where name="C:\\pagefile.sys" set InitialSize=8192,MaximumSize=12288

echo.
echo ========================================
echo [4/6] Настройка электропитания CPU...
echo ========================================
powercfg /setacvalueindex SCHEME_CURRENT SUB_PROCESSOR PROCTHROTTLEMAX 99
powercfg /setactive SCHEME_CURRENT

echo.
echo ========================================
echo [5/6] Отключение гибернации...
echo ========================================
powercfg /h off

echo.
echo ========================================
echo [6/6] Очистка системы...
echo ========================================
del /q /f "%TEMP%\*" 2>nul
echo Временные файлы удалены

echo.
echo ========================================
echo  ГОТОВО!
echo ========================================
echo.
echo Все оптимизации применены.
echo Для вступления в силу ТРЕБУЕТСЯ ПЕРЕЗАГРУЗКА!
echo.
set /p reboot="Перезагрузить сейчас? (Y/N): "
if /i "%reboot%"=="Y" (
    shutdown /r /t 0
) else (
    echo Перезагрузитесь вручную, когда будете готовы.
)
pause
