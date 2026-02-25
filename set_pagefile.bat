@echo off
echo Настройка файла подкачки...
wmic pagefileset where name="C:\\pagefile.sys" set InitialSize=8192,MaximumSize=12288
if %errorlevel% neq 0 (
    echo Не удалось найти pagefile.sys. Проверка...
    dir C:\pagefile.sys /a
)
echo.
echo Готово! Требуется перезагрузка.
pause
