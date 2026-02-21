# TeamCompass — Архитектура

Версия: 0.2  
Дата: 21 февраля 2026  
Контекст: Android-приложение специально для страйкболистов

## 1. Цель архитектуры

Архитектура TeamCompass ориентирована на практическую надежность в полевых условиях страйкбола:

- быстрый и понятный join-flow;
- устойчивый realtime-контур даже при нестабильной сети;
- предсказуемое поведение UI и фонового трекинга;
- явные security-границы доступа к данным команды;
- минимизация флейков в тестах и CI.

## 2. Архитектурный стиль

Используется `MVVM + Repository + Coordinator decomposition`.

- `ViewModel` управляет UI-состоянием и orchestrates сценарии экрана.
- `Repository` инкапсулирует backend-операции и контракты данных.
- Внутренние `Coordinator`-компоненты разрезают крупную логику по зонам ответственности (session/tracking/map/alerts).

### Почему не классический MVC

В проекте нет классического `Controller`, который напрямую обновляет View и данные.  
Вместо этого:

- Compose UI получает `state` и вызывает `actions`;
- `ViewModel` — единственная точка управления состоянием;
- data source скрыт за интерфейсами репозиториев.

Это упрощает тестирование и снижает связанность по сравнению с MVC-подходом на Android.

## 3. Границы модулей

```text
app/   -> Android runtime, Compose UI, ViewModel, Firebase/Location/BLE integration
core/  -> доменные модели, pure-логика, политики, математика, часть контрактов
```

### `core` слой

Содержит код, который:

- легко тестировать unit-тестами;
- не должен зависеть от Android framework;
- задает доменные инварианты (например, security/валидации/математика).

### `app` слой

Содержит:

- Android lifecycle и сервисы;
- UI и навигацию;
- инфраструктурные адаптеры (Firebase, sensors, BLE, imports);
- runtime/diagnostics/perf hooks.

## 4. Компоненты и ответственность

### UI слой (`app/ui`)

- Compose screens и composable-компоненты.
- Контракты экрана в формате `State + Actions` для снижения parameter explosion.
- `TeamCompassViewModel` как фасад публичного API экрана.

### Координаторы внутри UI runtime

- `SessionCoordinator`: auth/create/join/leave/listening.
- `TrackingCoordinator`: tracking policies, location/heading monitor, watchdog policy.
- `MapCoordinator`: KMZ/KML import/save, marker CRUD, map overlays.
- `AlertsCoordinator`: SOS/enemy/notification policies.

### Data слой

- Контракт: `TeamRepository`.
- Реализация: `FirebaseTeamRepository`.
- Реализует security-инварианты join-flow и realtime observe-поток команды.

### Android runtime компоненты

- `TeamCompassApplication`: bootstrap runtime и telemetry toggles.
- `TrackingService`: foreground location tracking.
- BLE scanner / sensor readers / map importer.

## 5. Ключевые data-flow

### 5.1 Create/Join Team

1. UI вызывает action (`createTeam`/`joinTeam`).
2. ViewModel делегирует в `SessionCoordinator`.
3. Координатор вызывает `TeamRepository`.
4. `FirebaseTeamRepository` проверяет контракты:
- teamCode строго `^\\d{6}$`;
- `joinSalt/joinHash` обязательны;
- mismatch hash -> `NOT_FOUND`.
5. Результат маппится в доменную ошибку и UI state/event.

### 5.2 Observe Team Snapshot

1. `observeTeam` подписывается на RTDB ветки (`state`, `points`, `privatePoints`, `enemyPings`, `commands/active`).
2. Частые child-события коалесцируются (debounce emit).
3. Списки обновляются инкрементально (без полной сортировки на каждый event).
4. Cleanup expired enemy pings выполняется редким sweep, а не в hot path emit.

### 5.3 Tracking Runtime

1. Пользователь включает tracking.
2. Применяется профиль (`GAME`/`SILENT`) с policy-настройками.
3. Отправка state идет по условиям интервала/дистанции.
4. Watchdog контролирует стагнацию и ограничивает частоту restart-циклов.

## 6. Security boundaries

Основные границы:

- доступ к тактическим данным только для участников команды;
- запись state только владельцем uid;
- immutable-поля для критичных сущностей (`createdBy`, `createdAtMs`, join security fields);
- join-контракт не раскрывает детали существования команды при hash mismatch.

Source of truth: `firebase-database.rules.json` + доменные проверки в репозитории.

## 7. Надежность и производительность

Применяемые инженерные решения:

- debounce snapshot emits в realtime observe;
- инкрементальные коллекции вместо full sort on every update;
- вынесенный cleanup TTL из hot path;
- кеширование map bitmap/геометрии и контроль тяжелых пересчетов;
- адаптивные фоновые циклы и throttling уведомлений.

## 8. Тестовый контур и CI

### Baseline quality gates

- `:app:compileDebugKotlin`
- `:core:test`
- `:app:testDebugUnitTest`
- `:app:compileDebugAndroidTestKotlin`

### AndroidTest контур

- `TeamCompassSmokeTest`: быстрый smoke (`app_root`).
- `TeamCompassInteractionTest`: только детерминированные UI state transitions join-экрана.
- `FirebaseRulesEmulatorTest`: security/rules негативные и позитивные кейсы.

### Hermetic runtime (debug-only)

Instrumentation args:

- `teamcompass.test.hermetic`
- `teamcompass.test.disable_telemetry`

Влияют только на `debug/androidTest`, release-поведение не меняют.

### CI hardening

- infra-only retry для connected instrumentation (максимум 1 ретрай);
- без ретрая на assertion/logic failures;
- обязательная выгрузка test-артефактов при падении.

## 9. Принципы эволюции

- Без big-bang миграций.
- Приоритет correctness/stability над скоростью добавления фич.
- Публичные контракты меняются минимально и осознанно.
- Любая декомпозиция должна оставлять compile/test gates зелеными.

## 10. Текущие ограничения

- Radar-first UX (не полноценный map-first tactical client).
- Нет офлайн P2P канала.
- Нет iOS-клиента.
- Нет расширенной RBAC-модели в рамках текущего цикла.
