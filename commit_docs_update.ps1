# Commit script for documentation updates
# TeamCompass v0.2.1 Documentation Refresh
# Date: 24 February 2026

$ErrorActionPreference = "Stop"

Write-Host "🔍 Checking documentation files..." -ForegroundColor Cyan

# Check if files exist
$files = @(
    "README.md",
    "ARCHITECTURE.md",
    "DOCS.md",
    "CHANGELOG.md",
    "MVP_SPEC.md",
    "DOCUMENTATION_UPDATE.md",
    "DOCUMENTATION_SUMMARY.md"
)

foreach ($file in $files) {
    if (Test-Path $file) {
        Write-Host "✅ $file exists" -ForegroundColor Green
    } else {
        Write-Host "❌ Error: $file not found!" -ForegroundColor Red
        exit 1
    }
}

Write-Host ""
Write-Host "📊 Documentation files status:" -ForegroundColor Cyan
git status --short README.md ARCHITECTURE.md DOCS.md CHANGELOG.md MVP_SPEC.md DOCUMENTATION_UPDATE.md DOCUMENTATION_SUMMARY.md 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "Files not yet tracked" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "📝 Preparing commit message..." -ForegroundColor Cyan

$commitMsg = @"
docs: актуализация документации v0.2.1

Обновлены все основные документы проекта по результатам аудита.

Изменения:
- README.md: полная переработка, версия 0.2.1, добавлены Release Notes
- ARCHITECTURE.md: полная переработка, добавлены диаграммы и data-flows
- DOCS.md: обновлён, добавлены результаты аудита (24.02.2026)
- CHANGELOG.md: создан новый файл с историей изменений
- MVP_SPEC.md: обновлён, добавлен раздел v0.2.1 improvements
- DOCUMENTATION_UPDATE.md: отчёт об обновлениях
- DOCUMENTATION_SUMMARY.md: итоговая сводка

Результаты аудита включены:
- 3 MAJOR проблемы (PendingIntent, Bluetooth cleanup, CancellationException)
- 4 MINOR проблемы (SecurityException, R8, Process death, God class)
- Обязательные проверки перед релизом
- Рекомендации по исправлению

Технический долг задокументирован:
- PendingIntent совместимость Android 12-14
- Bluetooth coordinator cleanup
- CancellationException обработка
- SecurityException handling
- R8/Proguard release crash risk
- Process death recovery
- TeamCompassViewModel декомпозиция

Следующие шаги:
- Исправить MAJOR проблемы перед релизом
- Выполнить обязательные проверки (сборка, тесты, lint)
- Протестировать на Android 12/13/14
- Проверить LeakCanary
- Обновить CHANGELOG при релизе v0.3.0

# Documentation audit completed: 24 February 2026
# Version: 0.2.1
# Status: Ready for review
"@

Write-Host ""
Write-Host "📋 Commit message preview:" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
Write-Host $commitMsg
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
Write-Host ""

$response = Read-Host "Proceed with commit? (y/n)"

if ($response -notmatch '^[Yy]$') {
    Write-Host "❌ Commit cancelled" -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "📦 Staging files..." -ForegroundColor Cyan
git add README.md ARCHITECTURE.md DOCS.md CHANGELOG.md MVP_SPEC.md DOCUMENTATION_UPDATE.md DOCUMENTATION_SUMMARY.md

Write-Host "✅ Files staged" -ForegroundColor Green

Write-Host ""
Write-Host "💾 Creating commit..." -ForegroundColor Cyan
git commit -m $commitMsg

Write-Host ""
Write-Host "✅ Commit created successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "📊 Git status:" -ForegroundColor Cyan
git status --short

Write-Host ""
Write-Host "🚀 To push to remote, run:" -ForegroundColor Cyan
Write-Host "   git push origin main"
Write-Host ""
Write-Host "📝 Don't forget to:" -ForegroundColor Yellow
Write-Host "   1. Run ./gradlew :app:assembleDebug to verify build"
Write-Host "   2. Run ./gradlew :app:testDebugUnitTest :core:test"
Write-Host "   3. Address MAJOR issues from audit before release"
Write-Host ""
