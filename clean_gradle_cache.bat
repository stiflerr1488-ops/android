@echo off
echo ========================================
echo  Очистка кэша Gradle и временных файлов
echo ========================================
echo.

echo [1/4] Остановка Gradle Daemon...
call gradlew --stop

echo [2/4] Очистка кэша Gradle...
if exist "%USERPROFILE%\.gradle\caches" (
    rmdir /s /q "%USERPROFILE%\.gradle\caches"
    echo Кэш Gradle удалён
) else (
    echo Кэш Gradle не найден
)

echo [3/4] Очистка build папок...
for /d /r %%d in (build) do @if exist "%%d" (
    rmdir /s /q "%%d"
    echo Удалено: %%d
)

echo [4/4] Очистка временных файлов...
del /q /f "%TEMP%\gradle*" 2>nul
del /q /f "%TEMP%\kotlin*" 2>nul

echo.
echo ========================================
echo  Очистка завершена!
echo ========================================
echo.
echo Свободно места на диске:
dir C:\ | findstr "free"
