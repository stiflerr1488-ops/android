# TeamCompass MVP Prototype

Android-приложение (Jetpack Compose + Firebase RTDB) для командной координации в матче: лобби, трекинг, компас и тактические отметки.

## Что реализовано сейчас
- `core/` — доменная логика и тесты:
  - гео-математика (`distance/bearing/relative`),
  - staleness-политика,
  - политики трекинга и backoff,
  - in-memory репозитории и use-cases,
  - hash-проверка join-кода (`SHA-256 + salt`).
- `app/` — Android-приложение:
  - экраны лобби/компаса/списка,
  - Firebase Anonymous Auth + RTDB,
  - запуск трекинга с foreground service,
  - хранение настроек и профиля в DataStore,
  - импорт KMZ/KML и тактические точки.

## Настройка окружения
1. Положите рабочий `google-services.json` в `app/`.
2. (Опционально) задайте URL RTDB через Gradle property:
   - `TEAMCOMPASS_RTDB_URL=https://<your-db>.firebasedatabase.app`
   - можно добавить в `~/.gradle/gradle.properties`.
3. Соберите проект в Android Studio или Gradle.

## Трекинг
- Режим `Игра`: по умолчанию 3 сек или 10 м (настраивается в UI).
- Режим `Тихо`: по умолчанию 10 сек или 30 м (настраивается в UI).
- Во время трекинга запускается foreground service с постоянной нотификацией.

## Тесты
- Основные unit-тесты находятся в `core/src/test`.
- Запуск: `./gradlew :core:test`.

## Дальше по roadmap
- Усиление RTDB Security Rules.
- Интеграционные тесты app-слоя (ViewModel + Firebase).
- Полировка UX и диагностика сетевой деградации.
