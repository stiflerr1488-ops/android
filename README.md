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

## RTDB Rules (рекомендуется)
- В репозитории добавлен базовый шаблон правил: `firebase-database.rules.json`.
- Перед деплоем проверьте под свою схему/прод-потоки и примените:
  - `firebase deploy --only database`
- Что усилили в шаблоне:
  - read только для участников команды,
  - write в `state`/`members` только для собственного `uid`,
  - валидация форматов `joinSalt`/`joinHash` и ключевых полей.

## Observability и устойчивость трекинга
- Добавлена телеметрия в `UiState.telemetry` (ошибки чтения/записи RTDB, время последней локации, перезапуски трекинга).
- Добавлен watchdog: если локация не обновлялась >45с при активном трекинге, трекинг перезапускается автоматически.
- `TrackingService` переведён на `START_REDELIVER_INTENT` и пишет lifecycle-логи для диагностики.
