# TeamCompass — Полная документация

> **Для агента:** Этот файл содержит **полную** информацию о проекте. Читайте его первым делом. Все команды сборки, зависимости, правила безопасности, архитектура и инструкции по отладке — здесь.

**Версия:** 0.2  
**Дата последнего обновления:** 21 февраля 2026  
**Платформа:** Android

---

## Содержание

1. [О проекте](#о-проекте)
2. [Для кого это приложение](#для-кого-это-приложение)
3. [Ключевые возможности](#ключевые-возможности)
4. [Технологический стек](#технологический-стек)
5. [Структура проекта](#структура-проекта)
6. [Ключевые файлы проекта](#klyuchevyye-fayly-proyekta)
7. [Требования к окружению](#trebovaniya-k-okruzheniyu)
8. [Настройка Firebase](#nastroyka-firebase)
9. [Разрешения и компоненты приложения](#razresheniya-i-komponenty-prilozheniya)
10. [Быстрый старт](#bystryy-start)
11. [Команды сборки и запуска](#komandy-sborki-i-zapuska)
12. [Зависимости](#zavisimosti)
13. [Архитектура](#arkhitektura)
14. [MVP спецификация](#mvp-spetsifikatsiya)
15. [Безопасность join-кода](#bezopasnost-join-koda)
16. [Схема RTDB](#skhema-rtdb)
17. [Правила Firebase Security Rules](#pravila-firebase-security-rules)
18. [Тестирование](#testirovaniye)
19. [CI/CD](#cicd)
20. [Работа с иконками](#rabota-s-ikonkami)
21. [Шпаргалка разработчика](#shpargalka-razrabotchika)
22. [Инструкция для агента: скриншоты UI](#instruktsiya-dlya-agenta-skrinshoty-ui)
23. [Работа с Android-устройством через ADB](#rabota-s-android-ustroystvom-cherez-adb)
24. [Ограничения](#ogranicheniya)
25. [Глоссарий терминов](#glossariy-terminov)
26. [UI экраны](#ui-ekrany)

---

## О проекте

**TeamCompass** — Android-приложение для страйкболистов, которое помогает команде координироваться в реальном времени на полигоне: видеть своих, быстро отмечать угрозы и держать общий тактический контекст.

---

## Для кого это приложение

- Команды страйкболистов на тренировках и играх
- Капитаны/организаторы, которым нужен быстрый situational awareness
- Игроки, которым важны простота, надежность и минимум «ручной» координации

---

## Ключевые возможности

- Создание команды и вход по 6-значному коду
- Общий радар/компас с позициями участников
- Режимы игрока `GAME` / `DEAD`
- SOS-сигнал с временным окном
- Тактические метки: командные и приватные точки
- Enemy ping и quick commands (`ATTACK`, `DEFENSE`, `DANGER`)
- Импорт KMZ/KML-карт и отображение тактического слоя
- BLE-сканирование устройств поблизости
- Foreground tracking с профилями обновления
- Базовая встроенная диагностика стабильности

---

## Технологический стек

| Компонент | Технология |
|-----------|------------|
| Язык | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Архитектура | MVVM + репозиторный слой |
| Backend | Firebase Anonymous Auth + Firebase Realtime Database |
| Хранение настроек | DataStore |
| Геолокация | FusedLocationProvider |
| Фон | Android Foreground Service (`location`) |

---

## Структура проекта

```text
.
├── app/
│   ├── src/main/kotlin/com/example/teamcompass/
│   │   ├── data/              ← Data layer: Firebase, репозитории
│   │   ├── domain/            ← Domain models, business logic
│   │   ├── tracking/          ← Location tracking, policies
│   │   ├── bluetooth/         ← BLE scanner
│   │   └── ui/                ← Compose UI, ViewModel, навигация
│   ├── src/androidTest/       ← Instrumentation тесты
│   ├── src/debug/             ← Debug-специфичный код
│   └── src/main/assets/       ← SVG иконки, KMZ карты
├── core/
│   └── src/main/kotlin/...    ← Общие модели, интерфейсы, утилиты
├── .agents/
│   └── skills/android-development/  ← Skill для агента (не редактировать)
├── .github/
│   └── workflows/ci.yml             ← CI конфигурация
├── tools/
│   ├── export_icons.ps1             ← Скрипт экспорта иконок
│   └── README_ICON.md
├── .gitignore                       ← Игнорируемые файлы
├── ARCHITECTURE.md                  ← Архитектурное руководство
├── firebase-database.rules.json     ← Правила безопасности RTDB
├── firebase.json                    ← Конфигурация эмуляторов Firebase
├── MVP_SPEC.md                      ← MVP спецификация
├── build.gradle.kts                 ← Корневой build (версии плагинов)
├── app/build.gradle.kts             ← Зависимости и настройки приложения
└── DOCS.md                          ← ЭТОТ ФАЙЛ
```

---

## Ключевые файлы проекта

| Файл | Назначение |
|------|------------|
| `app/build.gradle.kts` | Зависимости, SDK версии, build config |
| `build.gradle.kts` | Версии плагинов (Android, Kotlin, Firebase) |
| `app/google-services.json` | Firebase конфигурация (не хранить в репо!) |
| `firebase-database.rules.json` | Правила безопасности Realtime Database |
| `firebase.json` | Настройки эмуляторов Firebase |
| `.github/workflows/ci.yml` | CI/CD пайплайны |
| `gradle.properties` | Gradle JVM аргументы, флаги |
| `local.properties` | Путь к Android SDK (локальный) |
| `DOCS.md` | Полная документация (этот файл) |

---

## Требования к окружению

- **Android Studio:** Iguana/Koala+ или CLI-сборка с JDK 17+
- **JDK:** 17+
- **Android SDK:** минимум API 26 (targetSdk = 34)
- **Firebase-проект** с включенными:
  - `Authentication (Anonymous)`
  - `Realtime Database`
  - `Crashlytics` (опционально)
  - `Analytics` (опционально)

### Firebase CLI

Для работы с эмуляторами и деплоем правил:

```bash
# Установка Firebase CLI
npm install -g firebase-tools

# Логин
firebase login

# Список проектов
firebase projects:list

# Выбрать проект для работы
firebase use sk-grom
```

### Переменные окружения

```properties
# В local.properties или через -P флаг
TEAMCOMPASS_RTDB_URL=https://<your-db>.firebasedatabase.app
```

---

## Настройка Firebase

### Текущий проект

| Параметр | Значение |
|----------|----------|
| **Project ID** | `sk-grom` |
| **Project Number** | `290208390931` |
| **Account** | stiflerr.1488@gmail.com |

---

## Разрешения и компоненты приложения

### Разрешения (AndroidManifest.xml)

| Разрешение | Назначение |
|------------|------------|
| `INTERNET` | Доступ к Firebase |
| `ACCESS_FINE_LOCATION` | Точная геолокация (GPS) |
| `ACCESS_COARSE_LOCATION` | Приблизительная геолокация (сеть) |
| `VIBRATE` | Виброотклик |
| `FOREGROUND_SERVICE` | Фоновая служба |
| `FOREGROUND_SERVICE_LOCATION` | Фоновое отслеживание локации |
| `POST_NOTIFICATIONS` | Уведомления (Android 13+) |
| `BLUETOOTH` | BLE сканирование (старые API) |
| `BLUETOOTH_ADMIN` | Управление BLE (старые API) |
| `BLUETOOTH_SCAN` | BLE сканирование (Android 12+) |
| `BLUETOOTH_CONNECT` | BLE подключение (Android 12+) |

### Компоненты приложения

| Компонент | Тип | Назначение |
|-----------|-----|------------|
| `TeamCompassApplication` | Application | Инициализация приложения |
| `MainActivity` | Activity | Главная активность (landscape) |
| `TrackingService` | Foreground Service | Фоновое отслеживание локации |

### Конфигурация Activity

```xml
<activity
    android:name=".MainActivity"
    android:screenOrientation="landscape"  ← Только альбомная ориентация!
    android:theme="@style/Theme.TeamCompass.Splash"
    android:exported="true">
```

---

### Необходимые шаги для настройки

1. **Создать Firebase проект** (если не создан):
   ```bash
   firebase projects:create
   ```

2. **Зарегистрировать Android-приложение** в Firebase Console:
   - Package name: `com.example.teamcompass`
   - SHA-1 ключ (для debug):
     ```bash
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
     ```

3. **Скачать `google-services.json`**:
   - Firebase Console → Project Settings → Your apps → Download `google-services.json`
   - Положить в `app/google-services.json`
   - ⚠️ **Не коммитить** в репозиторий (добавить в `.gitignore`)

4. **Включить сервисы в Firebase Console**:
   - **Authentication** → Sign-in method → Anonymous → Enable
   - **Realtime Database** → Create database → Start in test mode (или сразу применить правила)

5. **Деплой правил безопасности**:
   ```bash
   firebase use sk-grom
   firebase deploy --only database
   ```

6. **Проверить правила**:
   ```bash
   firebase emulators:exec --only auth,database "echo Testing..."
   ```

### Структура Firebase проекта

```
Firebase Project: sk-grom
├── Authentication
│   └── Anonymous Auth (включено)
├── Realtime Database
│   ├── /teams/{teamCode}/meta
│   ├── /teams/{teamCode}/members/{uid}
│   ├── /teams/{teamCode}/state/{uid}
│   ├── /teams/{teamCode}/points/{pointId}
│   ├── /teams/{teamCode}/privatePoints/{uid}/{pointId}
│   ├── /teams/{teamCode}/enemyPings/{pingId}
│   └── /teams/{teamCode}/commands/active
├── Crashlytics (опционально)
└── Analytics (опционально)
```

### Деплой правил безопасности

```bash
# Убедиться, что выбран правильный проект
firebase use sk-grom

# Деплой правил в Firebase
firebase deploy --only database

# Деплой всех сервисов
firebase deploy
```

### Тестирование на эмуляторах

```bash
# Запустить эмуляторы Auth + Database
firebase emulators:start --only auth,database

# Запустить тесты с эмуляторами
npx firebase-tools@14.22.0 emulators:exec \
  --project demo-teamcompass \
  --only auth,database \
  "gradlew.bat :app:connectedDebugAndroidTest -PandroidTestClass=com.example.teamcompass.FirebaseRulesEmulatorTest"
```

---

## Быстрый старт

1. Положите `google-services.json` в `app/`.

2. При необходимости задайте URL базы:

```properties
TEAMCOMPASS_RTDB_URL=https://<your-db>.firebasedatabase.app
```

3. Соберите debug APK:

```bash
./gradlew :app:assembleDebug
```

---

## Команды сборки и запуска

### Сборка

```bash
# Debug APK
./gradlew :app:assembleDebug

# Release APK
./gradlew :app:assembleRelease

# Очистка и сборка
./gradlew clean :app:assembleDebug

# Сборка всех модулей
./gradlew build
```

### Установка на устройство

```bash
# Установить debug версию
./gradlew :app:installDebug

# Установить на конкретное устройство
$env:ANDROID_SERIAL="<device-serial>"
./gradlew :app:installDebug

# Install + запуск
./gradlew :app:installDebug && adb shell am start -n com.example.teamcompass/.MainActivity
```

### Pre-merge проверки

```bash
# Обязательный baseline (compile + test)
./gradlew :app:compileDebugKotlin :core:test :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin

# Полная проверка
./gradlew check
```

### Логирование

```bash
# Logcat с фильтром по тегу
adb logcat -s TeamCompass

# Logcat с timestamp
adb logcat -v time

# Очистить логи
adb logcat -c
```

---

## Зависимости

### Версии плагинов (root `build.gradle.kts`)

| Плагин | Версия |
|--------|--------|
| com.android.application | 8.5.0 |
| com.android.library | 8.5.0 |
| org.jetbrains.kotlin.android | 2.0.21 |
| org.jetbrains.kotlin.jvm | 2.0.21 |
| org.jetbrains.kotlin.plugin.serialization | 2.0.21 |
| org.jetbrains.kotlin.plugin.compose | 2.0.21 |
| com.google.gms.google-services | 4.4.4 |
| com.google.firebase.crashlytics | 3.0.6 |

### Основные зависимости (app/build.gradle.kts)

| Библиотека | Версия | Назначение |
|------------|--------|------------|
| androidx.core:core-ktx | 1.13.1 | AndroidX ядро |
| androidx.activity:activity-compose | 1.9.2 | Compose Activity |
| androidx.compose.bom | 2024.06.00 | Compose Bill of Materials |
| androidx.compose.ui:ui | - | Compose UI |
| androidx.compose.material3:material3 | - | Material 3 компоненты |
| androidx.compose.material:material-icons-extended | - | Иконки |
| com.google.android.material:material | 1.13.0 | Material Components |
| androidx.navigation:navigation-compose | 2.7.7 | Навигация |
| androidx.lifecycle:lifecycle-viewmodel-compose | 2.8.4 | ViewModel |
| androidx.lifecycle:lifecycle-runtime-ktx | 2.8.4 | Lifecycle |
| com.google.android.gms:play-services-location | 21.2.0 | Геолокация |
| kotlinx-coroutines-android | 1.9.0 | Корутины |
| androidx.datastore:datastore-preferences | 1.2.0 | Хранение настроек |
| com.google.zxing:core | 3.5.3 | QR коды |
| com.journeyapps:zxing-android-embedded | 4.3.0 | ZXing интеграция |
| com.google.firebase:firebase-bom | 34.9.0 | Firebase BOM |
| com.google.firebase:firebase-auth | - | Firebase Auth |
| com.google.firebase:firebase-database | - | Firebase Realtime Database |
| com.google.firebase:firebase-crashlytics | - | Crashlytics |
| com.google.firebase:firebase-analytics | - | Analytics |

### Тестовые зависимости

| Библиотека | Назначение |
|------------|------------|
| junit:junit:4.13.2 | Unit тесты |
| kotlinx-coroutines-test:1.9.0 | Тестирование корутин |
| androidx.test:core:1.6.1 | Test utilities |
| org.robolectric:robolectric:4.13 | Robolectric тесты |
| androidx.test:runner:1.6.1 | Android Test Runner |
| androidx.compose.ui:ui-test-junit4 | Compose UI тесты |
| androidx.test.ext:junit:1.2.1 | JUnit расширения |
| androidx.test.espresso:espresso-core:3.6.1 | Espresso UI тесты |

---

## Архитектура

### Цель архитектуры

Архитектура TeamCompass ориентирована на практическую надежность в полевых условиях страйкбола:

- быстрый и понятный join-flow;
- устойчивый realtime-контур даже при нестабильной сети;
- предсказуемое поведение UI и фонового трекинга;
- явные security-границы доступа к данным команды;
- минимизация флейков в тестах и CI.

### Архитектурный стиль

Используется `MVVM + Repository + Coordinator decomposition`.

- `ViewModel` управляет UI-состоянием и orchestrates сценарии экрана.
- `Repository` инкапсулирует backend-операции и контракты данных.
- Внутренние `Coordinator`-компоненты разрезают крупную логику по зонам ответственности (session/tracking/map/alerts).

### Границы модулей

```text
app/   -> Android runtime, Compose UI, ViewModel, Firebase/Location/BLE integration
core/  -> доменные модели, pure-логика, политики, математика, часть контрактов
```

#### `core` слой

Содержит код, который:
- легко тестировать unit-тестами;
- не должен зависеть от Android framework;
- задает доменные инварианты (например, security/валидации/математика).

#### `app` слой

Содержит:
- Android lifecycle и сервисы;
- UI и навигацию;
- инфраструктурные адаптеры (Firebase, sensors, BLE, imports);
- runtime/diagnostics/perf hooks.

### Компоненты и ответственность

#### UI слой (`app/ui`)

- Compose screens и composable-компоненты.
- Контракты экрана в формате `State + Actions` для снижения parameter explosion.
- `TeamCompassViewModel` как фасад публичного API экрана.

#### Координаторы внутри UI runtime

- `SessionCoordinator`: auth/create/join/leave/listening.
- `TrackingCoordinator`: tracking policies, location/heading monitor, watchdog policy.
- `MapCoordinator`: KMZ/KML import/save, marker CRUD, map overlays.
- `AlertsCoordinator`: SOS/enemy/notification policies.

#### Data слой

- Контракт: `TeamRepository`.
- Реализация: `FirebaseTeamRepository`.
- Реализует security-инварианты join-flow и realtime observe-поток команды.

#### Android runtime компоненты

- `TeamCompassApplication`: bootstrap runtime и telemetry toggles.
- `TrackingService`: foreground location tracking.
- BLE scanner / sensor readers / map importer.

---

## MVP спецификация

### Назначение

TeamCompass — мобильное приложение специально для страйкболистов. Цель — дать команде надежный и быстрый тактический контур координации во время игры:
- где находятся свои
- где отмечены угрозы
- какие актуальны командные сигналы

### Цели MVP

- Обеспечить стабильный join-flow команды по 6-значному коду
- Обеспечить надежный обмен позициями игроков в реальном времени
- Дать удобный радар/компас с тактическими метками и сигналами
- Гарантировать базовый security-контур для доступа к данным команды
- Закрыть baseline quality gates (`compile + test`)

### Не-цели MVP

- Полноценная офлайн mesh-сеть
- iOS-версия
- Сложная role-based модель прав внутри команды
- Полноценная историческая аналитика перемещений

### Пользователи и роли

- Игрок страйкбольной команды (основной пользователь)
- Организатор/капитан команды (технически те же права в MVP)

### Основные пользовательские сценарии

#### Вход и создание команды
1. Пользователь вводит позывной
2. Пользователь создает команду или входит по 6-значному коду
3. После входа попадает на основной экран радара

#### Игровой радар
- Пользователь видит себя и членов команды
- Пользователь видит дистанцию/направление, staleness данных
- Пользователь может включить/выключить tracking

#### Тактические действия
- Поставить командную или приватную точку
- Отправить enemy ping
- Отправить quick command
- Активировать SOS

#### Карта
- Импортировать KMZ/KML
- Включать/выключать слой
- Настроить прозрачность
- Сохранить изменения меток

---

## Безопасность join-кода

- Формат кода команды: строго `^\d{6}$`
- В RTDB `meta` хранит `joinSalt` и `joinHash`
- `joinHash` проверяется на `joinTeam`
- При несовпадении хеша возвращается `NOT_FOUND` (без утечки деталей)
- `joinSalt`/`joinHash` неизменяемы после создания команды

---

## Схема RTDB

```text
/teams/{teamCode}/meta
  createdAtMs, expiresAtMs, createdBy, isLocked, joinSalt, joinHash

/teams/{teamCode}/members/{uid}
  callsign, joinedAtMs

/teams/{teamCode}/state/{uid}
  callsign, lat, lon, acc, speed, heading, ts, mode, anchored, sosUntilMs

/teams/{teamCode}/points/{pointId}
/teams/{teamCode}/privatePoints/{uid}/{pointId}
/teams/{teamCode}/enemyPings/{pingId}
/teams/{teamCode}/commands/active
/teams/{teamCode}/memberPrefs/{uid}
```

**Source of truth** по правилам доступа: `firebase-database.rules.json`.

---

## Правила Firebase Security Rules

**Файл:** `firebase-database.rules.json`

### Общие правила

| Правило | Описание |
|---------|----------|
| `.read`: false | Глобальный запрет чтения (требуются явные разрешения) |
| `.write`: false | Глобальный запрет записи (требуются явные разрешения) |

### teams/{teamCode}

#### Создание команды
- **Условие:** `auth != null && !data.exists() && newData.hasChildren(['meta', 'members'])`
- **createdBy:** Должен совпадать с `auth.uid`

#### meta
- **Чтение:** Любой авторизованный пользователь
- **Запись:** Только создатель команды (`createdBy == auth.uid`)
- **Валидация:**
  - Обязательные поля: `createdAtMs`, `createdBy`, `isLocked`, `expiresAtMs`, `joinSalt`, `joinHash`
  - `joinSalt`: hex, 16-128 символов
  - `joinHash`: hex, 64 символа (SHA-256)
  - `expiresAtMs > createdAtMs`
  - `joinSalt/joinHash` неизменяемы после создания

#### members/{uid}
- **Чтение:** Только участники команды
- **Запись:** Только сам пользователь (`auth.uid == $uid`)
- **Запрет входа:** Если `isLocked == true` или команда истекла (`expiresAtMs < now`)
- **Валидация:**
  - `callsign`: строка, 1-24 символа
  - `joinedAtMs`: число (неизменяемо)

#### state/{uid}
- **Чтение:** Только участники команды
- **Запись:** Только владелец состояния (`auth.uid == $uid`)
- **Валидация:**
  - `lat`: -90..90, `lon`: -180..180
  - `mode`: `'GAME'` или `'DEAD'`
  - `ts <= now + 60000` (защита от будущих timestamp)
  - Обязательные поля: `callsign`, `lat`, `lon`, `acc`, `speed`, `ts`, `mode`, `anchored`, `sosUntilMs`

#### points/{pointId} (командные точки)
- **Чтение:** Только участники команды
- **Запись:** Только создатель точки
- **Валидация:**
  - `scope == 'TEAM'`, `kind == 'POINT'`
  - `state`: `'ACTIVE'`, `'EXPIRED'`, `'DISABLED'`
  - `label`: макс. 80 символов
  - `icon`: 1-32 символа
  - `createdBy/createdAtMs` неизменяемы

#### privatePoints/{uid}/{pointId} (приватные точки)
- **Чтение/Запись:** Только владелец
- **Валидация:** Аналогично points + `scope == 'PRIVATE'`

#### enemyPings/{pingId}
- **Чтение:** Только участники команды
- **Запись:** Только создатель
- **Валидация:**
  - `expiresAtMs <= createdAtMs + 600000` (макс. 10 минут)
  - `type`: `'ATTACK'`, `'DEFENSE'`, `'DANGER'`
  - `scope == 'TEAM_EVENT'`, `kind == 'ENEMY_PING'`

#### commands/active
- **Чтение:** Только участники команды
- **Запись:** Любой участник команды
- **Валидация:**
  - `type`: `'RALLY'`, `'RETREAT'`, `'ATTACK'`

#### memberPrefs/{uid}
- **Чтение/Запись:** Только владелец
- **Валидация:**
  - `preset`: `'ALL'`, `'SOS'`, `'NEAR'`, `'ACTIVE'`
  - `nearRadiusM`: 50-500 метров

---

## Тестирование

### Обязательный pre-merge baseline

```bash
./gradlew :app:compileDebugKotlin :core:test :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin
```

### Instrumentation (Smoke / Interaction)

```bash
./gradlew :app:connectedDebugAndroidTest \
  "-PandroidTestClass=com.example.teamcompass.TeamCompassSmokeTest" \
  "-PandroidTestArgs=teamcompass.test.hermetic=true,teamcompass.test.disable_telemetry=true" \
  --stacktrace

./gradlew :app:connectedDebugAndroidTest \
  "-PandroidTestClass=com.example.teamcompass.TeamCompassInteractionTest" \
  "-PandroidTestArgs=teamcompass.test.hermetic=true,teamcompass.test.disable_telemetry=true" \
  --stacktrace
```

### Firebase rules tests

```bash
npx -y firebase-tools@14.22.0 emulators:exec \
  --project demo-teamcompass \
  --config firebase.json \
  --only auth,database \
  "gradlew.bat :app:connectedDebugAndroidTest -PandroidTestClass=com.example.teamcompass.FirebaseRulesEmulatorTest -PandroidTestArgs=teamcompass.test.hermetic=true,teamcompass.test.disable_telemetry=true --stacktrace"
```

### Hermetic test-режим

Поддерживаются debug-only instrumentation args:
- `teamcompass.test.hermetic=true|false`
- `teamcompass.test.disable_telemetry=true|false`

При включении отключаются внешние побочные эффекты телеметрии (Analytics/Crashlytics) для детерминированных androidTest.

---

## CI/CD

**Workflow:** `.github/workflows/ci.yml`

### Jobs

| Job | Назначение |
|-----|------------|
| `baseline` | Компиляция + unit тесты |
| `android-ui-tests` | Instrumentation тесты на эмуляторе |
| `firebase-rules` | Тесты правил Firebase на эмуляторе |

### Конфигурация

- **Infra-only retry:** 1 попытка для `connectedDebugAndroidTest` (только при device/adb ошибках)
- **Нет ретрая** для product assertion failures
- **Артефакты при падении:**
  - `app/build/reports/androidTests/connected/**`
  - `app/build/outputs/androidTest-results/connected/**`
  - `firebase-emulators.log`

### Firebase эмуляторы

**Конфигурация:** `firebase.json`

```json
{
  "emulators": {
    "auth": { "port": 9099 },
    "database": { "port": 9000 },
    "singleProjectMode": true
  }
}
```

**Запуск тестов правил:**
```bash
npx -y firebase-tools@14.22.0 emulators:exec \
  --project demo-teamcompass \
  --config firebase.json \
  --only auth,database \
  "gradlew.bat :app:connectedDebugAndroidTest -PandroidTestClass=com.example.teamcompass.FirebaseRulesEmulatorTest"
```

---

## Работа с иконками

### Источник иконок

Проект использует векторную графику (`VectorDrawable`) для современных Adaptive Icons (Android 8.0+, API 26).

### Структура ресурсов

- `app/src/main/assets/icon_source.svg` — мастер-копия иконки
- `app/src/main/assets/playstore-icon.svg` — SVG для Play Store (1024x1024)
- `app/src/main/res/drawable/ic_launcher_foreground.xml` — передний план
- `app/src/main/res/drawable/ic_launcher_background.xml` — фон (#1A1C16)
- `app/src/main/res/drawable/ic_launcher_monochrome.xml` — монохромная версия (Android 13+)
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` — адаптивная иконка
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml` — круглая версия

### Экспорт PNG для старых устройств

Для устройств API < 26 требуются PNG в папках `mipmap-*`.

**Требования:** Inkscape должен быть установлен и доступен в PATH.

```powershell
# Установить Inkscape (Windows)
winget install Inkscape.Inkscape

# Экспортировать все PNG
.\tools\export_icons.ps1

# Принудительно использовать .NET рендерер
.\tools\export_icons.ps1 -ForceDotNet
```

---

## Инструкция для агента: скриншоты UI

**Перед началом работы с UI обязательно сделай скриншоты!**

Агент должен самостоятельно:
1. Подключиться к устройству
2. Запустить приложение
3. Сделать скриншот текущего состояния
4. Проанализировать UI по скриншоту
5. Только после этого вносить изменения

### Команды для скриншотов

```bash
# 1. Убедиться, что устройство подключено
adb devices

# 2. Запустить приложение
adb shell am start -n com.example.teamcompass/.MainActivity

# 3. Подождать 2-3 секунды на загрузку
# PowerShell:
Start-Sleep -Seconds 3

# 4. Сделать скриншот текущего экрана
adb shell screencap -p /sdcard/screen.png
adb pull /sdcard/screen.png ./screenshots/current_state.png

# 5. Открыть и проанализировать скриншот
# Windows:
start ./screenshots/current_state.png
```

### Навигация по приложению для скриншотов

```bash
# Главное меню / радар
# (приложение запускается сразу на радаре после авторизации)

# Открыть меню настроек (правый верхний угол)
adb shell input tap 1800 50

# Открыть меню команды
adb shell input tap 1800 150

# Вернуться назад
adb shell input keyevent 4
```

### Анализ UI по скриншоту

1. Открой скриншот
2. Определи текущий экран (Radar / Settings / Join / etc.)
3. Проверь видимость ключевых элементов:
   - Игроки на радаре (кружки с позывными)
   - Кнопки управления (меню, настройки, трекинг)
   - Статус бар (режим GAME/DEAD, SOS)
   - Тактические метки
4. Задокументируй проблемы (если есть)

### Автосохранение скриншотов

```powershell
# Создать папку для скриншотов с датой
$timestamp = Get-Date -Format "yyyy-MM-dd_HH-mm-ss"
New-Item -ItemType Directory -Force -Path "./screenshots/$timestamp"

# Сделать скриншот с именем-временем
adb shell screencap -p /sdcard/screen.png
adb pull /sdcard/screen.png "./screenshots/$timestamp/screen_$timestamp.png"
```

---

## Шпаргалка разработчика

### Типичные задачи и команды

#### Git: настройка после клонирования

```bash
# Убедиться, что .gitignore работает
git status

# Если видишь .gradle/, build/ и другие игнорируемые файлы:
git rm -rf --cached .gradle/ build/
git commit -m "Remove generated files from tracking"
```

#### Git: первый коммит после настройки

```bash
# Добавить все файлы
git add .

# Проверить что будет закоммичено
git status

# Сделать коммит
git commit -m "Описание изменений"

# Отправить на сервер
git push
```

#### Добавить новую зависимость
1. Открой `app/build.gradle.kts`
2. Добавь в `dependencies {}`
3. Sync Gradle

#### Изменить версию SDK
1. Открой `app/build.gradle.kts`
2. Измени `compileSdk`, `minSdk`, `targetSdk`
3. Sync Gradle

#### Запустить тесты конкретного класса
```bash
./gradlew :app:connectedDebugAndroidTest \
  -PandroidTestClass=com.example.teamcompass.MyTestClass
```

#### Отладка Firebase на эмуляторе
```bash
# Запустить эмуляторы
npx firebase-tools emulators:start --only auth,database

# В приложении установить RTDB URL на localhost
# TEAMCOMPASS_RTDB_URL=http://localhost:9000
```

#### Проверить правила Firebase
```bash
npx firebase-tools emulators:exec \
  --only auth,database \
  "gradlew.bat :app:connectedDebugAndroidTest -PandroidTestClass=com.example.teamcompass.FirebaseRulesEmulatorTest"
```

#### Собрать и установить на устройство
```bash
./gradlew :app:installDebug
```

#### Получить логи приложения
```bash
adb logcat -s TeamCompass:v
```

#### Скриншоты для документации
```bash
adb shell screencap -p /sdcard/screen.png
adb pull /sdcard/screen.png ./screenshots/
```

#### Проверка текущей активности
```bash
adb shell dumpsys window windows | grep -E 'mCurrentFocus|mFocusedApp'
```

### Поиск и устранение проблем

| Проблема | Решение |
|----------|---------|
| `Inconsistent JVM-target` | Проверь `compileOptions` и `kotlinOptions` в `build.gradle.kts` |
| `Missing google-services.json` | Положи файл в `app/` (не коммить в репо!) |
| `Firebase permission denied` | Проверь правила в `firebase-database.rules.json` |
| `Test failed on device` | Запусти с `--stacktrace`, проверь логи |
| `ADB device not found` | `adb kill-server && adb start-server`, переподключи USB |
| `Gradle sync failed` | `File → Invalidate кэш → Restart` в Android Studio |

---

## Работа с Android-устройством через ADB

### Установка ADB

**Windows:**
1. Скачайте [Android SDK Platform Tools](https://developer.android.com/studio/releases/platform-tools)
2. Распакуйте в удобное место (например, `C:\adb`)
3. Добавьте путь к `adb.exe` в переменную среды PATH

**Проверка установки:**
```bash
adb version
```

### Включение отладки по USB на устройстве

1. Откройте **Настройки** → **О телефоне**
2. Нажмите 7 раз на **Номер сборки** (появится уведомление «Вы стали разработчиком»)
3. Вернитесь в главное меню настроек
4. Откройте **Для разработчиков** (или **Система** → **Для разработчиков**)
5. Включите **Отладка по USB**
6. Подключите устройство к компьютеру
7. На устройстве подтвердите разрешение на отладку с этого компьютера

### Основные команды ADB

#### Подключение и проверка

```bash
# Показать все подключенные устройства
adb devices

# Перезапустить ADB сервер
adb kill-server && adb start-server

# Проверить подключение к конкретному устройству
adb -s <serial> devices
```

#### Установка и запуск приложений

```bash
# Установить APK
adb install path/to/app.apk

# Установить с заменой существующего
adb install -r path/to/app.apk

# Удалить приложение
adb uninstall com.example.teamcompass

# Запустить приложение
adb shell am start -n com.example.teamcompass/.MainActivity

# Остановить приложение
adb shell am force-stop com.example.teamcompass

# Очистить данные приложения
adb shell pm clear com.example.teamcompass
```

#### Логирование

```bash
# Вывод логов в реальном времени
adb logcat

# Фильтрация по тегу
adb logcat -s TeamCompass

# Фильтрация по приоритету (V/D/I/W/E/F)
adb logcat *:W

# Сохранить логи в файл
adb logcat -d > logcat.txt

# Очистить буфер логов
adb logcat -c

# Логирование с timestamp
adb logcat -v time

# Фильтр по PID процесса
adb logcat --pid=$(adb shell pidof -s com.example.teamcompass)
```

#### Работа с файлами

```bash
# Скопировать файл с устройства
adb pull /sdcard/Download/file.txt ./file.txt

# Скопировать файл на устройство
adb push ./file.txt /sdcard/Download/

# Сделать скриншот
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png

# Записать экран (до 3 минут)
adb shell screenrecord /sdcard/video.mp4
adb pull /sdcard/video.mp4
```

#### Сетевые команды

```bash
# Проброс портов (forward)
adb forward tcp:8080 tcp:8080

# Проброс для конкретного устройства
adb -s <serial> forward tcp:8080 tcp:8080

# Список всех пробросов
adb forward --list

# Удалить проброс
adb forward --remove tcp:8080

# Wi-Fi отладка (требуется Android 11+)
adb pair <IP>:<port>
adb connect <IP>:<port>
```

#### Работа с несколькими устройствами

```bash
# Показать все устройства с серийными номерами
adb devices -l

# Выполнить команду на конкретном устройстве
adb -s <serial> shell getprop

# Пример серийного номера: emulator-5554, 192.168.1.100:5555, ABC123XYZ
```

#### Эмулятор

```bash
# Запустить эмулятор
emulator -avd <avd_name>

# Список доступных AVD
emulator -list-avds

# Скриншот эмулятора
adb emu screenrecord start --time-limit 180 video.mp4
```

#### Разрешения

```bash
# Предоставить разрешение
adb shell pm grant com.example.teamcompass android.permission.ACCESS_FINE_LOCATION

# Отозвать разрешение
adb shell pm revoke com.example.teamcompass android.permission.ACCESS_FINE_LOCATION

# Показать все разрешения приложения
adb shell dumpsys package com.example.teamcompass | grep permissions
```

#### Отладка UI

```bash
# Показать текущую активность
adb shell dumpsys window windows | grep -E 'mCurrentFocus|mFocusedApp'

# Получить иерархию UI (для UI Automator Viewer)
adb shell uiautomator dump
adb pull /sdcard/window_dump.xml

# Включить отображение нажатий
adb shell settings put system show_touches on

# Отключить отображение нажатий
adb shell settings put system show_touches off
```

#### Производительность

```bash
# Показать использование CPU
adb shell top -m 10

# Показать использование памяти
adb shell dumpsys meminfo com.example.teamcompass

# Получить информацию о батарее
adb shell dumpsys batterystats

# Сбросить статистику батареи
adb shell dumpsys batterystats --reset
```

### Отладка через Wi-Fi (Android 11+)

```bash
# 1. Подключите устройство по USB
# 2. Включите отладку по Wi-Fi в настройках разработчика
# 3. Получите код сопряжения и IP:порт

# Сопряжение (один раз)
adb pair 192.168.1.100:12345

# Подключение
adb connect 192.168.1.100:5555

# Проверка
adb devices

# Отключение
adb disconnect 192.168.1.100:5555
```

### Отладка через Wi-Fi (Android 10 и старше)

```bash
# 1. Подключите по USB
# 2. Включите TCP/IP режим
adb tcpip 5555

# 3. Отключите USB
# 4. Подключитесь по сети
adb connect <device-ip>:5555

# 5. Для возврата к USB
adb usb
```

### Частые проблемы и решения

| Проблема | Решение |
|----------|---------|
| `device unauthorized` | Разблокируйте устройство и подтвердите отладку |
| `device not found` | Проверьте кабель, включите отладку по USB |
| `adb not recognized` | Добавьте ADB в PATH или используйте полный путь |
| `connection refused` | Перезапустите ADB: `adb kill-server && adb start-server` |
| Устройство не определяется | Установите драйверы производителя |

### Полезные утилиты

```bash
# Scrcpy — зеркалирование экрана (требует установки)
scrcpy

# Scrcpy с записью
scrcpy --record file.mp4

# Bugreport (полный отчет об устройстве)
adb bugreport > report.zip
```

---

## Ограничения

- Основной UX — radar-first, не map-first
- Нет полноценного офлайн P2P-канала
- Нет iOS-версии
- Полные e2e-матрицы device farm не входят в MVP-цикл

---

## Глоссарий терминов

| Термин | Значение |
|--------|----------|
| **TeamCompass** | Название приложения |
| **Radar** | Основной экран с позициями игроков (радар/компас) |
| **Join Code** | 6-значный код для входа в команду |
| **Callsign** | Позывной игрока |
| **GAME/DEAD** | Режимы игрока (в игре / мёртв) |
| **SOS** | Сигнал бедствия с таймером |
| **Enemy Ping** | Метка врага с типом (ATTACK/DEFENSE/DANGER) |
| **Quick Command** | Быстрая команда (RALLY/RETREAT/ATTACK) |
| **Private Point** | Приватная метка (видна только создателю) |
| **Team Point** | Командная метка (видна всем) |
| **KMZ/KML** | Форматы карт для импорта |
| **BLE** | Bluetooth Low Energy |
| **Foreground Service** | Фоновая служба с уведомлением |
| **RTDB** | Firebase Realtime Database |
| **Uid** | Уникальный ID пользователя (Firebase Auth) |
| **TTL** | Time To Live (время жизни enemy ping) |

---

## UI экраны

| Экран | Описание |
|-------|----------|
| **Splash** | Экран заставки (350мс анимация компаса) |
| **Join/Create** | Вход/создание команды (позывной, код) |
| **Radar** | Основной экран: радар с игроками, дистанции, азимуты |
| **Settings** | Настройки: профиль, точность, фильтры отображения |
| **Map** | Карта с KMZ/KML слоем, тактические метки |
| **TeamManagement** | Управление командой: участники, блокировка, завершение |

---

## Документация

- Архитектура (MVVM и границы слоев): `ARCHITECTURE.md`
- Продуктовая спецификация: `MVP_SPEC.md`
- Правила RTDB: `firebase-database.rules.json`

---

## Версия

- Текущая версия: `0.2`
- Последнее обновление README: `21 февраля 2026`
