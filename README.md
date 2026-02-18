# TeamCompass

TeamCompass — Android-приложение для тактической координации команды в реальном времени: лобби, общий «радар/компас», трекинг участников, быстрые команды, тактические метки и импорт KMZ/KML-карт.

---

## 1) Что это за проект

**Назначение:** дать команде быстрый situational awareness «где свои / где отмечен противник / какие текущие команды» без тяжёлой картографической платформы.

**Текущий статус:** MVP+прототип с рабочим end-to-end флоу:
- анонимная авторизация,
- создание/вход в команду,
- обмен координатами через Firebase RTDB,
- отображение целей на радаре,
- foreground-трекинг,
- набор тактических инструментов (точки, enemy ping, quick commands, SOS, KMZ overlay).

---

## 2) Технологический стек

- **Платформа:** Android (minSdk 26, targetSdk 34, compileSdk 34)
- **Язык:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Навигация:** Navigation Compose
- **Backend:** Firebase Anonymous Auth + Firebase Realtime Database
- **Локация:** Google Play Services Location (FusedLocationProvider)
- **Локальное хранение:** DataStore Preferences
- **Фоновая работа:** Foreground Service (`location` type)
- **Модульность:**
  - `core/` — доменная логика, математика, политики, use-case и unit-тесты
  - `app/` — Android UI, ViewModel, интеграция с Firebase/Location/Sensors

---

## 3) Архитектура и структура репозитория

```text
.
├── app/
│   ├── src/main/kotlin/com/example/teamcompass/
│   │   ├── MainActivity.kt
│   │   ├── TrackingService.kt
│   │   └── ui/
│   │       ├── TeamCompassApp.kt
│   │       ├── TeamCompassViewModel.kt
│   │       ├── SettingsScreen.kt
│   │       ├── RadarOverlay.kt
│   │       ├── TacticalMapRender.kt
│   │       ├── TacticalIcons.kt
│   │       ├── KmzMapImporter.kt
│   │       ├── UserPrefs.kt
│   │       └── ...
│   └── src/main/res/...
├── core/
│   └── src/main/kotlin/com/example/teamcompass/core/
│       ├── GeoMath.kt
│       ├── CompassCalculator.kt
│       ├── Models.kt
│       ├── StalenessPolicy.kt
│       ├── TrackingPolicies.kt
│       ├── TeamCodeSecurity.kt
│       ├── UseCases.kt
│       └── ...
├── docs/
│   └── UI_UX_AUDIT.md
├── MVP_SPEC.md
└── firebase-database.rules.json
```

---

## 4) Полный функционал (по подсистемам)

### 4.1. Авторизация, команда и лобби

- Firebase Anonymous Auth на старте.
- Создание новой команды (`createTeam`) с генерацией:
  - 6-значного join-кода,
  - `salt + SHA-256 hash` для безопасной проверки кода.
- Вход в команду по коду (`joinTeam`) с проверкой хеша.
- Хранение текущего `teamCode` и позывного в DataStore.
- Валидация позывного в UI (3–16 символов, буквы/цифры/`_`/`-`).
- Экран входа разделён на 2 действия:
  - **Создать команду**
  - **Войти по коду**

### 4.2. Трекинг и геопозиция

- Управление трекингом из UI:
  - `startTracking(mode)`
  - `stopTracking()`
- Профили трекинга:
  - **Игра** (по умолчанию 3с / 10м)
  - **Тихо** (по умолчанию 10с / 30м)
- Профили полностью настраиваются в Settings и сохраняются локально.
- Foreground Service (`TrackingService`) с постоянной нотификацией.
- Режим перезапуска сервиса: `START_REDELIVER_INTENT`.
- Watchdog во ViewModel: при «зависшем» потоке локации трекинг может быть перезапущен автоматически, телеметрия фиксирует причину.

### 4.3. Компас/радар и цели

- Для каждого участника рассчитываются:
  - дистанция (haversine),
  - азимут,
  - относительный угол относительно текущего heading.
- Стрелки/цели визуализируются на радаре.
- Поддержка staleness-статусов (fresh/stale/old/hidden).
- Сортировка списка целей:
  - по дистанции,
  - по свежести.
- Поиск по позывным.

### 4.4. Heading, режимы игрока и сигнализация

- Получение heading через `TYPE_ROTATION_VECTOR`.
- Коррекция угла с учётом поворота экрана.
- Переключение статуса игрока:
  - `GAME`
  - `DEAD`
- SOS-механика:
  - `triggerSos()` / `toggleSos()` / `clearSos()`
  - SOS отражается у участников.
- Локальные сигналы тревоги:
  - вибрация,
  - звуковой тон (ALARM stream).

### 4.5. Тактические слои и командные отметки

- **Точки на карте/радаре**:
  - командные (shared)
  - приватные (личные)
- CRUD для точек:
  - `addPointHere` / `addPointAt`
  - `updatePoint`
  - `deletePoint`
- Набор тактических иконок (флаг, цель, атака, оборона, опасность, медик, база, связь и др.).
- **Enemy ping**:
  - постановка метки противника,
  - отображение на радаре,
  - авто-очистка устаревших пингов.
- **Quick commands**:
  - `RALLY`, `RETREAT`, `ATTACK`
  - отображение активной команды с ограниченным временем жизни.

### 4.6. Импорт KMZ/KML тактической карты

- Импорт `.kmz` и `.kml` документов.
- Безопасная распаковка KMZ (защита от path traversal).
- Парсинг KML-сущностей:
  - `GroundOverlay` (растровая подложка),
  - `Point`,
  - `LineString`,
  - `Polygon` (outer boundary).
- Управление слоем карты в UI:
  - включить/выключить,
  - изменить прозрачность,
  - очистить карту.

### 4.7. Настройки и диагностика

- Экран Settings:
  - дефолтный профиль трекинга,
  - интервалы и distance threshold для Game/Silent,
  - пояснения по профилям.
- Встроенная телеметрия:
  - число ошибок чтения RTDB,
  - число ошибок записи RTDB,
  - число перезапусков трекинга,
  - причина последнего перезапуска.

### 4.8. Устойчивость и UX-поведение

- Splash не задерживает лишнее время (краткая адаптивная пауза).
- Глобальные ошибки выводятся через `UiEvent.Error` + snackbar.
- Обработка отсутствия разрешения на локацию.
- Онбординг-подсказка для компаса (`showCompassHelpOnce`).

---

## 5) Полный визуал и пользовательский флоу

### Экран 1: Splash

- Центральная карточка-логотип (`ic_compass`), название TeamCompass.
- Подзаголовок: «ТАКТИЧЕСКИЙ РАДАР • КОМАНДА».
- Анимация состояния подключения (`CircularProgressIndicator`).

### Экран 2: Join (вход)

- Хедер с иконкой и названием продукта.
- Карточка «Вход в команду»:
  - поле позывного,
  - подсказка формата,
  - создание команды,
  - поле кода,
  - вход по коду,
  - состояние загрузки/блокировки кнопок.

### Экран 3: Compass (основной)

Основной экран построен вокруг полноэкранного радара.

Визуальные элементы:
- кольца и сетка радара,
- маркер собственного положения в центре,
- цели-команда с дистанцией/степенью устаревания,
- tactical overlays (точки/пинги/иконки),
- действия на экране (tracking, режимы, quick actions),
- диалоги:
  - статусы,
  - быстрые команды,
  - карты,
  - help.

Жесты и интеракции:
- pinch-to-zoom для радиуса радара,
- tap/long-press для добавления/редактирования тактических точек,
- переключатели режимов игрока и enemy mark.

### Экран 4: Settings

- TopAppBar с кнопкой «Назад».
- Блок «Режим по умолчанию» (Игра/Тихо).
- Две policy-карточки с `Slider`:
  - частота отправки,
  - минимальная дистанция триггера.
- Блок «Диагностика» с телеметрией сети/трекинга.

### Дизайн-система

- Material 3 палитра + custom spacing tokens (`Spacing`).
- Единая русскоязычная терминология интерфейса.
- Иконографика на основе Material Icons + кастомный compass-логотип.

---

## 6) Доменная логика (`core`) — что реализовано

- **GeoMath**:
  - `distanceMeters`
  - `bearingDegrees`
  - `normalizeRelativeDegrees`
  - `normalizeDegrees0to360`
- **CompassCalculator**:
  - сбор и фильтрация целей для UI-компаса
- **StalenessPolicy**:
  - классификация свежести данных по времени
- **TrackingPolicies**:
  - выбор профиля по режиму
  - экспоненциальный backoff для ретраев
- **TeamCodeSecurity**:
  - генерация соли
  - хеш join-кода
  - проверка join-кода
- **UseCases + Repositories**:
  - старт/вход/закрытие матча
  - запись/чтение состояния игроков
- **InMemory реализации и unit-тесты** для ключевых политик и математики.

---

## 7) Firebase RTDB схема

```text
/matches/{matchId}/meta
  createdAt, expiresAt, createdBy, isLocked, joinSalt, joinHash

/matches/{matchId}/members/{uid}
  nick, team, joinedAt

/matches/{matchId}/state/{uid}
  lat, lon, acc, speed, heading, ts, mode, sosUntilMs

/matches/{matchId}/points/team/{pointId}
/matches/{matchId}/points/private/{uid}/{pointId}

/matches/{matchId}/enemy/{pingId}

/matches/{matchId}/commands/active
```

В репозитории есть шаблон правил безопасности: `firebase-database.rules.json`.

---

## 8) Разрешения Android

Используются разрешения:
- `INTERNET`
- `ACCESS_FINE_LOCATION`
- `ACCESS_COARSE_LOCATION`
- `FOREGROUND_SERVICE`
- `FOREGROUND_SERVICE_LOCATION`

`MainActivity` зафиксирована в landscape-ориентации.

---

## 9) Сборка и запуск

### Требования
- Android Studio Iguana+ / Koala+
- JDK 17+ (в проекте используется JVM target 17)
- Firebase проект с включёнными Auth (Anonymous) и Realtime Database

### Шаги
1. Положить рабочий `google-services.json` в `app/`.
2. (Опционально) прописать RTDB URL:
   ```properties
   TEAMCOMPASS_RTDB_URL=https://<your-db>.firebasedatabase.app
   ```
   в `~/.gradle/gradle.properties` или `gradle.properties` проекта.
3. Собрать:
   ```bash
   ./gradlew :app:assembleDebug
   ```
4. Запустить на устройстве (желательно реальном для точного теста локации/сенсоров).

---

## 10) Тестирование

Основные unit-тесты расположены в `core/src/test/kotlin/...`:
- `GeoMathTest`
- `StalenessPolicyTest`
- `TrackingPoliciesTest`
- `TeamCodeSecurityTest`
- `InMemoryRepositoriesTest`

Запуск:
```bash
./gradlew :core:test
```

---

## 11) Ограничения текущего MVP

- Нет полноценного «картоцентричного» экрана (основа — radar-first).
- Нет истории перемещений/трекинг-треков.
- Нет офлайн P2P-канала обмена (только сеть + Firebase).
- Нет полноценной системы ролей/прав внутри команды.
- Нет e2e/UI-тестов app-слоя (в основном unit-тесты `core`).

---

## 12) Документы в репозитории

- `MVP_SPEC.md` — функциональная спецификация MVP.
- `docs/UI_UX_AUDIT.md` — UX/UI аудит и план улучшений.
- `firebase-database.rules.json` — шаблон RTDB правил.

---

## 13) Быстрый roadmap

- Усилить и формализовать RTDB rules под production-схему.
- Добавить интеграционные тесты ViewModel + Firebase.
- Доработать onboarding на радаре и accessibility baseline.
- Добавить системный dark/light с ручным override.
- Расширить диагностику (сетевая деградация, автозапуск/восстановление).
