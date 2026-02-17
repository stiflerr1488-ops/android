# TeamCompass MVP Prototype

Этот коммит начинает реализацию приложения по `MVP_SPEC.md`.

## Что реализовано сейчас
- `core/` — доменные модели и логика MVP:
  - вычисления distance/bearing/relative angle;
  - политика staleness;
  - режимы трекинга (`Игра` / `Тихо`);
  - backoff для ретраев;
  - in-memory репозитории матча/состояний;
  - use-case классы (`StartMatch`, `JoinMatch`, `StartTracking`, `StopTracking`).
- `app/` — минимальный JVM entrypoint (`main`) для демонстрации расчёта целей.

## Почему без полноценного Android UI в этом шаге
В текущем окружении не разрешаются Gradle plugin-артефакты (Kotlin/Android plugins),
поэтому сборка Android-модуля блокируется на уровне toolchain.
Логика вынесена в независимый модуль `core`, чтобы продолжить реализацию
и покрыть правила MVP тестами.

## Следующий шаг
При рабочем доступе к Android Gradle plugins:
1. перевести `app` обратно на Android + Compose;
2. подключить Firebase Auth/RTDB;
3. добавить Foreground Service для location tracking;
4. связать UI экранов Lobby/Compass/List с use-case и Flow-состоянием.
