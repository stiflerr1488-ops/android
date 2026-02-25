#!/bin/bash

# Commit script for documentation updates
# TeamCompass v0.2.1 Documentation Refresh
# Date: 24 February 2026

set -e

echo "🔍 Checking documentation files..."

# Check if files exist
FILES=(
    "README.md"
    "ARCHITECTURE.md"
    "DOCS.md"
    "CHANGELOG.md"
    "MVP_SPEC.md"
    "DOCUMENTATION_UPDATE.md"
    "DOCUMENTATION_SUMMARY.md"
)

for file in "${FILES[@]}"; do
    if [ ! -f "$file" ]; then
        echo "❌ Error: $file not found!"
        exit 1
    fi
    echo "✅ $file exists"
done

echo ""
echo "📊 Documentation files status:"
git status --short README.md ARCHITECTURE.md DOCS.md CHANGELOG.md MVP_SPEC.md DOCUMENTATION_UPDATE.md DOCUMENTATION_SUMMARY.md 2>/dev/null || echo "Files not yet tracked"

echo ""
echo "📝 Preparing commit message..."

COMMIT_MSG="docs: актуализация документации v0.2.1

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
# Status: Ready for review"

echo ""
echo "📋 Commit message preview:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "$COMMIT_MSG"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

read -p "Proceed with commit? (y/n) " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "❌ Commit cancelled"
    exit 0
fi

echo ""
echo "📦 Staging files..."
git add README.md ARCHITECTURE.md DOCS.md CHANGELOG.md MVP_SPEC.md DOCUMENTATION_UPDATE.md DOCUMENTATION_SUMMARY.md

echo "✅ Files staged"

echo ""
echo "💾 Creating commit..."
git commit -m "$COMMIT_MSG"

echo ""
echo "✅ Commit created successfully!"
echo ""
echo "📊 Git status:"
git status --short

echo ""
echo "🚀 To push to remote, run:"
echo "   git push origin main"
echo ""
echo "📝 Don't forget to:"
echo "   1. Run ./gradlew :app:assembleDebug to verify build"
echo "   2. Run ./gradlew :app:testDebugUnitTest :core:test"
echo "   3. Address MAJOR issues from audit before release"
echo ""
