# TeamCompass — Полная документация проекта

**Версия:** 0.2.1  
**Дата обновления:** 25 февраля 2026 (актуализировано)  
**Платформа:** Android (API 26–36)  
**Статус:** ✅ Все критичные проблемы аудита исправлены  
**Firebase Project:** `sk-grom` (Project Number: `290208390931`)

---

## 📋 Оглавление

1. [О проекте](#1-о-проекте)
2. [Для кого это приложение](#2-для-кого-это-приложение)
3. [Ключевые возможности](#3-ключевые-возможности)
4. [Технологический стек](#4-технологический-стек)
5. [Структура проекта](#5-структура-проекта)
6. [Требования к окружению](#6-требования-к-окружению)
7. [Быстрый старт](#7-быстрый-старт)
8. [Настройка Firebase](#8-настройка-firebase)
9. [Команды сборки и запуска](#9-команды-сборки-и-запуска)
10. [Производительность сборки](#10-производительность-сборки)
11. [Архитектура приложения](#11-архитектура-приложения)
12. [Схема базы данных RTDB](#12-схема-базы-данных-rtdb)
13. [Правила безопасности Firebase](#13-правила-безопасности-firebase)
14. [Безопасность join-кода](#14-безопасность-join-кода)
15. [Разрешения Android](#15-разрешения-android)
16. [Тестирование](#16-тестирование)
17. [CI/CD](#17-cicd)
18. [Результаты аудита](#18-результаты-аудита)
19. [Известные ограничения](#19-известные-ограничения)
20. [Changelog](#20-changelog)
21. [Roadmap](#21-roadmap)
22. [Troubleshooting](#22-troubleshooting)
23. [Глоссарий](#23-глоссарий)
24. [Приложения](#24-приложения)

---

## 1. О проекте

**TeamCompass** — Android-приложение для страйкболистов, которое помогает команде координироваться в реальном времени на полигоне: видеть своих, быстро отмечать угрозы и держать общий тактический контекст.

### Основное назначение

- **Realtime координация** — позиции всех участников команды в реальном времени
- **Тактическое планирование** — метки, enemy pings, командные сигналы
- **Situational awareness** — общая картина боя для всех участников
- **Минимум ручной координации** — автоматический обмен данными через Firebase

### Ключевые принципы

- ✅ **Correctness > Speed** — приоритет стабильности над скоростью разработки
- ✅ **Radar-first UX** — сначала радар, потом карта
- ✅ **Degraded mode UX** — работа при нестабильной сети
- ✅ **Security by design** — валидация на уровне Firebase Rules

---

## 2. Для кого это приложение

| Пользователь | Сценарии использования |
|--------------|------------------------|
| **Команды страйкболистов** | Тренировки, игры, соревнования |
| **Капитаны/организаторы** | Быстрый situational awareness, управление командой |
| **Игроки** | Простота, надёжность, минимум «ручной» координации |
| **Наблюдатели** | Мониторинг хода игры в реальном времени |

---

## 3. Ключевые возможности

### 3.1 Базовые функции

| Возможность | Описание | Реализация |
|-------------|----------|------------|
| 🎯 **Создание команды** | 6-значный код, Firebase Anonymous Auth | `SessionCoordinator` |
| 🧭 **Общий радар/компас** | Позиции участников в реальном времени | `CompassScreen` + `FirebaseTeamRepository` |
| 👤 **Режимы игрока** | `GAME` / `DEAD` с визуальными индикаторами | `PlayerMode` enum, UI индикация |
| 🆘 **SOS-сигнал** | Временное окно с уведомлением команды | `sosUntilMs` field, `EventNotificationManager` |
| 📍 **Тактические метки** | Командные и приватные точки на карте | `points`, `privatePoints` в RTDB |
| ⚔️ **Enemy ping** | Быстрое отмечание угроз (`ATTACK`, `DEFENSE`, `DANGER`) | `enemyPings` с TTL 600s |
| 🗺️ **Карты** | Импорт KMZ/KML, тактический слой | `KmzMapImporter`, `FullscreenMapScreen` |
| 📶 **BLE-сканирование** | Обнаружение устройств поблизости | `BluetoothScanner`, `BluetoothScanCoordinator` |
| 📊 **Диагностика** | Backend health monitoring, stale data индикация | `BackendHealthMonitor`, `TeamSnapshotObserver` |
| 🔆 **Автояркость** | Автоматическое управление на основе положения устройства | `ScreenAutoBrightness`, `AutoBrightnessBinding` |

### 3.2 Расширенные функции (v0.2.1)

| Функция | Описание |
|---------|----------|
| **Backend health monitoring** | Periodic probe Firebase RTDB (10s interval), UI banner при недоступности |
| **Stale data detection** | Определение данных старше 30 секунд |
| **Exponential backoff reconnect** | Переподключение с задержкой (max 20s cap) |
| **SavedStateHandle** | Восстановление state после process death |
| **State cells cache** | Оптимизация reads для больших команд (5,000 entries max) |
| **Performance metrics** | Логирование RTDB emits, cleanup sweeps, map cache hits |

---

## 4. Технологический стек

### 4.1 Основные технологии

| Компонент | Технология | Версия | Назначение |
|-----------|------------|--------|------------|
| **Язык** | Kotlin | 2.3.10 | Основной язык разработки |
| **UI** | Jetpack Compose + Material 3 | BOM 2026.02.00 | Декларативный UI |
| **Архитектура** | MVVM + Repository + Coordinator | — | Разделение ответственности |
| **DI** | Hilt | 2.59.2 | Dependency Injection |
| **Backend** | Firebase Anonymous Auth + RTDB | BOM 34.9.0 | Realtime база данных |
| **Корутины** | Kotlinx Coroutines | 1.10.2 | Асинхронность |
| **Flow** | StateFlow + SharedFlow | — | Reactive streams |
| **Навигация** | Navigation Compose | 2.9.7 | Навигация между экранами |
| **Хранение** | DataStore Preferences | 1.2.0 | Локальные настройки |
| **Геолокация** | Google Play Services Location | 21.3.0 | GPS/Network location |
| **Фон** | Foreground Service (location) | — | Фоновое отслеживание |

### 4.2 Build-инструменты

| Инструмент | Версия | Назначение |
|------------|--------|------------|
| **Android Gradle Plugin** | 9.0.1 | Сборка Android приложения |
| **Kotlin Plugin** | 2.3.10 | Компиляция Kotlin |
| **KSP** | (встроен в Kotlin) | Kotlin Symbol Processing |
| **Compose Plugin** | 2.3.10 | Компиляция Compose |
| **Google Services** | 4.4.4 | Firebase интеграция |
| **Firebase Crashlytics** | 3.0.6 | Краш-репортинг |

### 4.3 Тестовые зависимости

| Библиотека | Версия | Назначение |
|------------|--------|------------|
| **JUnit** | 4.13.2 | Unit тесты |
| **Kotlinx Coroutines Test** | 1.10.2 | Тестирование корутин |
| **AndroidX Test Core** | 1.7.0 | Test utilities |
| **Robolectric** | 4.16.1 | Unit тесты с Android SDK |
| **AndroidX Test Runner** | 1.7.0 | Android Test Runner |
| **Compose UI Test** | (из BOM) | Compose UI тесты |
| **AndroidX Test Ext JUnit** | 1.3.0 | JUnit расширения |
| **Espresso Core** | 3.7.0 | UI тесты |

---

## 5. Структура проекта

### 5.1 Дерево файлов

```
android/
├── app/                              # Android runtime модуль
│   ├── src/main/kotlin/com/example/teamcompass/
│   │   ├── auth/                     # Firebase Auth, IdentityLinkingService
│   │   ├── bluetooth/                # BLE scanner, BluetoothDevice
│   │   ├── data/
│   │   │   └── firebase/             # FirebaseTeamRepository, RealtimeBackendClient
│   │   ├── di/                       # Hilt modules (SingletonComponent)
│   │   ├── domain/                   # TeamRepository, TrackingController, roles
│   │   ├── p2p/                      # P2P transports (BLE, LoRa bridge)
│   │   ├── perf/                     # Performance metrics
│   │   ├── tracking/                 # TrackingRuntime, TrackingControllerImpl
│   │   └── ui/
│   │       ├── components/           # Reusable Compose components
│   │       ├── dialogs/              # Dialog implementations
│   │       ├── theme/                # Theme, colors, typography
│   │       └── *.kt                  # Screens, ViewModel, Coordinators
│   ├── src/androidTest/              # Instrumentation тесты (16 файлов)
│   ├── src/debug/                    # Debug-specific code
│   ├── src/test/                     # Unit тесты (~40 файлов)
│   ├── src/main/
│   │   ├── assets/                   # SVG иконки, KMZ карты
│   │   ├── res/                      # Resources, layouts, drawables
│   │   └── AndroidManifest.xml       # Manifest с разрешениями
│   ├── build.gradle.kts              # Конфигурация app модуля
│   ├── proguard-rules.pro            # Proguard правила для release
│   └── lint-baseline.xml             # Lint baseline
│
├── core/                             # Pure Kotlin модуль
│   ├── src/main/kotlin/.../core/
│   │   ├── p2p/                      # P2P models, security, chunker
│   │   └── *.kt                      # GeoMath, Models, Policies, Validators
│   ├── src/test/kotlin/.../core/     # Core unit тесты
│   └── build.gradle.kts              # Конфигурация core модуля
│
├── .agents/                          # Qwen agent skills (не редактировать)
├── .codex/                           # ChatGPT extension configs
├── .continue/                        # Continue.dev configs
├── .github/workflows/                # CI/CD workflow
│   ├── ci.yml                        # Основной CI pipeline
│   └── nightly-release-smoke.yml     # Nightly release тесты
├── .githooks/                        # Git hooks (UTF-8 check)
├── .vscode/                          # VS Code settings
├── tools/                            # Scripts (icons, checks, CI helpers)
│   ├── ci/                           # CI scripts (Python, Shell)
│   ├── export_icons.ps1              # Скрипт экспорта иконок
│   └── README_ICON.md                # Инструкция по иконкам
│
├── DOCUMENTATION.md                  # ЭТОТ файл — полная документация
├── README.md                         # Краткий README со ссылкой на DOCUMENTATION.md
├── firebase-database.rules.json      # Security rules для RTDB
├── firebase.json                     # Firebase emulators config
├── build.gradle.kts                  # Root build (plugin versions)
├── gradle.properties                 # Gradle JVM аргументы, флаги
└── gradle/wrapper/                   # Gradle wrapper
```

### 5.2 Статистика проекта

| Метрика | Значение |
|---------|----------|
| **Строк кода (app)** | ~15,000 |
| **Строк кода (core)** | ~3,000 |
| **Тестовых файлов** | 56 (unit + instrumentation) |
| **Compose screens** | 6 основных |
| **Coordinators** | 4 (Session, Tracking, Map, Alerts) |
| **P2P transports** | 2 (BLE, LoRa bridge) |
| **Instrumentation тестов** | 16 файлов |
| **Unit тестов** | ~40 файлов |

---

## 6. Требования к окружению

### 6.1 Минимальные требования

| Требование | Версия | Примечание |
|------------|--------|------------|
| **Android Studio** | Iguana/Koala+ | Или CLI-сборка |
| **JDK** | 17+ | JDK 21 рекомендуется |
| **Android SDK** | API 26–36 | minSdk = 26, targetSdk = 36 |
| **RAM** | 8 ГБ | Минимум для комфортной разработки |
| **Disk** | 10 ГБ | Для SDK, Gradle cache, build artifacts |

### 6.2 Firebase требования

| Сервис | Статус | Назначение |
|--------|--------|------------|
| **Authentication** | ✅ Обязательно | Anonymous Auth для идентификации |
| **Realtime Database** | ✅ Обязательно | Хранение состояния команды |
| **Crashlytics** | ⚪ Опционально | Краш-репортинг |
| **Analytics** | ⚪ Опционально | Телеметрия использования |

### 6.3 Firebase CLI

Для работы с эмуляторами и деплоем правил:

```bash
# Установка Firebase CLI
npm install -g firebase-tools@14.22.0

# Логин
firebase login

# Список проектов
firebase projects:list

# Выбрать проект для работы
firebase use sk-grom
```

---

## 7. Быстрый старт

### 7.1 Настройка Firebase

**Шаг 1: Скопировать шаблон**

```powershell
# Windows PowerShell
Copy-Item app/google-services.json.example app/google-services.json

# Или вручную: скопируйте app/google-services.json.example в app/google-services.json
```

**Шаг 2: Заменить placeholder'ы**

Откройте `app/google-services.json` и замените:
- `YOUR_FIREBASE_PROJECT_ID` на `sk-grom`
- `YOUR_PROJECT_NUMBER` на `290208390931`
- `YOUR_API_KEY` на реальный API ключ из Firebase Console

**Шаг 3: Скачать реальный конфиг (опционально)**

1. Откройте [Firebase Console](https://console.firebase.google.com/)
2. Выберите проект `sk-grom`
3. Settings → Your apps → Android app
4. Download `google-services.json`
5. Замените файл в `app/google-services.json`

⚠️ **ВАЖНО:** Не коммитьте `app/google-services.json` в репозиторий!

### 7.2 Настройка RTDB URL

**Способ 1: local.properties**

```properties
# Создайте/отредактируйте local.properties в корне проекта
TEAMCOMPASS_RTDB_URL=https://sk-grom-default-rtdb.europe-west1.firebasedatabase.app
```

**Способ 2: Через -P флаг**

```bash
./gradlew assembleDebug -PTEAMCOMPASS_RTDB_URL=https://sk-grom-default-rtdb.europe-west1.firebasedatabase.app
```

**Способ 3: Переменная окружения**

```bash
export TEAMCOMPASS_RTDB_URL=https://sk-grom-default-rtdb.europe-west1.firebasedatabase.app
./gradlew assembleDebug
```

### 7.3 Сборка и запуск

```bash
# 1. Debug APK
./gradlew :app:assembleDebug

# 2. Установить на устройство
./gradlew :app:installDebug

# 3. Запустить приложение
adb shell am start -n com.example.teamcompass/.MainActivity

# 4. Логирование
adb logcat -s TeamCompass

# 5. Очистка и сборка
./gradlew clean :app:assembleDebug
```

### 7.4 Pre-merge проверки (обязательно)

```bash
# Baseline gates (compile + test)
./gradlew :app:compileDebugKotlin :core:test :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin

# Полная проверка
./gradlew check

# Lint
./gradlew :app:lintDebug
```

---

## 8. Настройка Firebase

### 8.1 Текущий проект

| Параметр | Значение |
|----------|----------|
| **Project ID** | `sk-grom` |
| **Project Number** | `290208390931` |
| **Account** | stiflerr.1488@gmail.com |
| **RTDB URL** | `https://sk-grom-default-rtdb.europe-west1.firebasedatabase.app` |

### 8.2 Пошаговая настройка

**Шаг 1: Создать Firebase проект (если не создан)**

```bash
firebase projects:create
```

**Шаг 2: Зарегистрировать Android-приложение**

1. Firebase Console → Project Settings → Your apps → Add app
2. Package name: `com.example.teamcompass`
3. App nickname: `TeamCompass`
4. Download `google-services.json`

**Шаг 3: Получить SHA-1 ключ (для debug)**

```bash
# Windows PowerShell
keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android

# Linux/macOS
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

**Шаг 4: Включить сервисы в Firebase Console**

1. **Authentication** → Sign-in method → Anonymous → Enable
2. **Realtime Database** → Create database → Start in test mode (или сразу применить правила)

**Шаг 5: Деплой правил безопасности**

```bash
# Убедиться, что выбран правильный проект
firebase use sk-grom

# Деплой правил в Firebase
firebase deploy --only database

# Деплой всех сервисов
firebase deploy
```

**Шаг 6: Проверить правила**

```bash
# Запустить эмуляторы Auth + Database
firebase emulators:start --only auth,database

# Запустить тесты с эмуляторами
npx firebase-tools@14.22.0 emulators:exec \
  --project demo-teamcompass \
  --only auth,database \
  "gradlew.bat :app:connectedDebugAndroidTest -PandroidTestClass=com.example.teamcompass.FirebaseRulesEmulatorTest"
```

### 8.3 Структура Firebase проекта

```
Firebase Project: sk-grom
├── Authentication
│   └── Anonymous Auth (включено)
├── Realtime Database
│   ├── /teams/{teamCode}/meta
│   ├── /teams/{teamCode}/members/{uid}
│   ├── /teams/{teamCode}/memberRoles/{uid}
│   ├── /teams/{teamCode}/state/{uid}
│   ├── /teams/{teamCode}/stateCells/{cellId}/{uid}
│   ├── /teams/{teamCode}/points/{pointId}
│   ├── /teams/{teamCode}/privatePoints/{uid}/{pointId}
│   ├── /teams/{teamCode}/enemyPings/{pingId}
│   ├── /teams/{teamCode}/rateLimits/enemyPing/{uid}
│   ├── /teams/{teamCode}/commands/active
│   └── /teams/{teamCode}/memberPrefs/{uid}
├── Crashlytics (опционально)
└── Analytics (опционально)
```

---

## 9. Команды сборки и запуска

### 9.1 Основная сборка

```bash
# Debug APK (быстрая сборка)
./gradlew :app:assembleDebug

# Release APK (с Proguard/R8)
./gradlew :app:assembleRelease

# Очистка и сборка
./gradlew clean :app:assembleDebug

# Сборка всех модулей
./gradlew build
```

### 9.2 Установка на устройство

```bash
# Установить debug версию
./gradlew :app:installDebug

# Установить на конкретное устройство
$env:ANDROID_SERIAL="<device-serial>"
./gradlew :app:installDebug

# Install + запуск
./gradlew :app:installDebug && adb shell am start -n com.example.teamcompass/.MainActivity

# Uninstall
./gradlew :app:uninstallAll
```

### 9.3 Тестирование

```bash
# Pre-merge baseline (обязательно)
./gradlew :app:compileDebugKotlin :core:test :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin

# Полная проверка
./gradlew check

# Unit тесты
./gradlew :core:test :app:testDebugUnitTest

# Instrumentation тесты
./gradlew :app:connectedDebugAndroidTest

# Конкретный тест
./gradlew :app:connectedDebugAndroidTest \
  "-PandroidTestClass=com.example.teamcompass.TeamCompassSmokeTest" \
  "-PandroidTestArgs=teamcompass.test.hermetic=true,teamcompass.test.disable_telemetry=true"

# Lint
./gradlew :app:lintDebug
```

### 9.4 Логирование

```bash
# Logcat с фильтром по тегу
adb logcat -s TeamCompass

# Logcat с timestamp
adb logcat -v time

# Logcat с цветом (через pidcat)
pidcat com.example.teamcompass

# Очистить логи
adb logcat -c

# Сохранить логи в файл
adb logcat -d > logs.txt
```

### 9.5 Отладка

```bash
# Подключиться к устройству
adb devices

# Перезапустить adb server
adb kill-server && adb start-server

# Скриншот
adb shell screencap -p /sdcard/screen.png && adb pull /sdcard/screen.png

# Записать экран
adb shell screenrecord /sdcard/demo.mp4

# Получить логcat
adb logcat -d > bugreport.txt
```

---

## 10. Производительность сборки

### 10.1 Настройки по умолчанию (low-load)

**Файл:** `gradle.properties`

```properties
# JVM аргументы для Gradle
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m -Dfile.encoding=UTF-8 -XX:+UseG1GC -XX:SoftRefLRUPolicyMSPerMB=50

# Daemon mode
org.gradle.daemon=true

# Кэширование
org.gradle.caching=true

# Параллелизм (отключён для low-load)
org.gradle.parallel=false

# Конфигурационный кэш
org.gradle.configuration-cache=true
org.gradle.configuration-cache.problems=warn
org.gradle.configureondemand=true

# Максимум workers
org.gradle.workers.max=1

# Kotlin настройки
kotlin.incremental=true
ksp.incremental=true
kotlin.compiler.execution.strategy=out-of-process
kotlin.daemon.jvmargs=-Xmx768m -XX:MaxMetaspaceSize=256m
```

### 10.2 Команды для разных сценариев

| Сценарий | Команда | Время сборки | Использование RAM |
|----------|---------|--------------|-------------------|
| **Low-load (default)** | `./gradlew :app:compileDebugKotlin` | ~2-3 мин | ~4-5 ГБ |
| **Extra-low load** | `./gradlew :app:compileDebugKotlin --max-workers=1` | ~4-5 мин | ~3-4 ГБ |
| **Faster (когда ПК свободен)** | `./gradlew :app:compileDebugKotlin --max-workers=4` | ~1-2 мин | ~6-7 ГБ |
| **Release smoke check** | `./gradlew :app:assembleRelease --max-workers=2` | ~5-7 мин | ~5-6 ГБ |

### 10.3 Очистка кэша

```bash
# Остановить Gradle демоны
./gradlew --stop

# Очистить артефакты
./gradlew clean

# Полная очистка кэша
rm -rf .gradle/ build/ app/build/ core/build/

# Очистка через скрипт
./clean_gradle_cache.bat
```

### 10.4 Оптимизация для 8 ГБ RAM

**Проблема:** При компиляции все 8 ГБ используются, ноут начинает лагать.

**Решение:**

1. **Использовать low-load настройки** (уже включены в `gradle.properties`)
2. **Закрыть лишние приложения** перед компиляцией
3. **Использовать скрипт очистки памяти:**
   ```bash
   ./clean_ram_before_build.bat
   ```
4. **Запускать компиляцию с низким приоритетом:**
   ```bash
   ./build_limited.bat
   ```

### 10.5 CI производительность

**CI настройки** (переопределяют local):

```properties
# В .github/workflows/ci.yml
GRADLE_OPTS: -Dorg.gradle.workers.max=4 -Dorg.gradle.parallel=true
```

**Время сборки в CI:**
- Baseline compile: ~10-15 мин
- Unit tests: ~5-7 мин
- Lint: ~3-5 мин
- Instrumentation tests: ~20-30 мин (на эмуляторе)

---

## 11. Архитектура приложения

### 11.1 Архитектурный стиль

Используется **MVVM + Repository + Coordinator decomposition**.

```
┌─────────────────────────────────────────────────────────┐
│                   UI Layer (Compose)                    │
│  ┌───────────────────────────────────────────────────┐  │
│  │            TeamCompassViewModel                   │  │
│  │  - State management (StateFlow)                   │  │
│  │  - User actions handling                          │  │
│  │  - Lifecycle awareness                            │  │
│  └───────────────────────────────────────────────────┘  │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐    │
│  │  Session     │ │   Tracking   │ │     Map      │    │
│  │  Coordinator │ │  Coordinator │ │  Coordinator │    │
│  └──────────────┘ └──────────────┘ └──────────────┘    │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│              Domain Layer (Interfaces)                  │
│  ┌──────────────────┐    ┌──────────────────────┐      │
│  │ TeamRepository   │    │ TrackingController   │      │
│  └──────────────────┘    └──────────────────────┘      │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│               Data Layer (Implementation)               │
│  ┌──────────────────────────────────────────────────┐   │
│  │         FirebaseTeamRepository                   │   │
│  │  - RealtimeDatabase callbacks                    │   │
│  │  - callbackFlow для observe                      │   │
│  │  - Security rules validation                     │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

### 11.2 Границы модулей

| Модуль | Назначение | Зависимости |
|--------|------------|-------------|
| **app** | Android runtime, UI, Firebase integration | Зависит от core |
| **core** | Pure Kotlin: модели, математика, политики | Нет зависимостей от Android |

### 11.3 Слои приложения

#### UI Layer (`app/ui`)

| Компонент | Ответственность |
|-----------|----------------|
| **Compose screens** | Отображение state, user interaction |
| **TeamCompassViewModel** | Фасад публичного API экрана, state management |
| **Coordinators** | Изолированная бизнес-логика по доменам |

**Координаторы:**

| Координатор | Зоны ответственности |
|-------------|---------------------|
| **SessionCoordinator** | Auth, create/join team, leave team, listening |
| **TrackingCoordinator** | Tracking policies, location/heading monitor, watchdog |
| **MapCoordinator** | KMZ/KML import/save, marker CRUD, overlays |
| **AlertsCoordinator** | SOS, enemy pings, notification policies |
| **TargetFilterCoordinator** | Filter presets, radius, focus mode |
| **LocationReadinessCoordinator** | Permission checks, service readiness |
| **BackendAvailabilityCoordinator** | Backend health monitoring |
| **BluetoothScanCoordinator** | BLE device discovery |
| **MapActionsCoordinator** | Map interactions |
| **TacticalActionsCoordinator** | Tactical actions (enemy pings, commands) |

#### Domain Layer

| Интерфейс | Назначение |
|-----------|------------|
| **TeamRepository** | Контракт операций с командой |
| **TrackingController** | Контракт отслеживания локации |

#### Data Layer

| Компонент | Ответственность |
|-----------|----------------|
| **FirebaseTeamRepository** | Реализация TeamRepository на Firebase RTDB |
| **RealtimeBackendClient** | Low-level RTDB operations, health probing |
| **TrackingControllerImpl** | Реализация TrackingController |
| **TrackingRuntime** | Runtime логика отслеживания |

#### Android Runtime

| Компонент | Назначение |
|-----------|------------|
| **TeamCompassApplication** | Bootstrap runtime, telemetry toggles, memory trim |
| **TrackingService** | Foreground location tracking service |
| **MainActivity** | Main entry point, splash screen, fullscreen mode |
| **HiltModules** | DI configuration (SingletonComponent) |

### 11.4 Data-flow диаграммы

#### 11.4.1 Create/Join Team

```
User action (UI)
    ↓
ViewModel.createTeam() / joinTeam(code)
    ↓
SessionCoordinator
    ↓
TeamRepository.createTeam(uid, callsign) / joinTeam(code, uid, callsign)
    ↓
FirebaseTeamRepository
    ↓
Firebase RTDB (transaction)
    ↓
Result → Domain error mapping → UI state/event
```

**Валидации в FirebaseTeamRepository:**

- ✅ Team code: строго `^\d{6}$`
- ✅ `joinSalt` required (hex 16–128)
- ✅ `joinHash` required (hex 64)
- ✅ Mismatch hash → `NOT_FOUND` (без утечки)
- ✅ `isLocked` check → `LOCKED`
- ✅ `expiresAtMs` check → `EXPIRED`

#### 11.4.2 Observe Team Snapshot

```
ViewModel.startListening()
    ↓
teamSessionDelegate.observeTeam(teamCode)
    ↓
FirebaseTeamRepository.observeTeam()
    ↓
callbackFlow {
    ValueEventListener.onChildAdded/Changed/Removed
    awaitClose { removeEventListener }
}
    ↓
TeamSnapshotObserver (retry policy, exponential backoff)
    ↓
Debounce emit (75ms)
    ↓
Incremental collections (без full sort на каждый event)
    ↓
UI state update (StateFlow)
```

**Оптимизации:**

- ✅ Debounce snapshot emits
- ✅ Инкрементальные коллекции (без полной сортировки)
- ✅ Cleanup expired enemy pings редким sweep (не в hot path)
- ✅ State cells cache (5,000 entries max)

#### 11.4.3 Tracking Runtime

```
User enables tracking
    ↓
ViewModel.startTracking(mode)
    ↓
TrackingController.startTracking(config)
    ↓
TrackingService.startForeground()
    ↓
TrackingRuntime.ensureScope()
    ↓
LocationUpdates (FusedLocationProvider)
    ↓
TrackingPolicy.shouldSendState(location, prevLocation, timestamp)
    ↓
TeamRepository.updateState(teamCode, uid, statePayload)
    ↓
Firebase RTDB (/state/{uid} или /stateCells/{cellId}/{uid})
```

**Policy checks:**

- ✅ Interval-based (GAME: 2s, SILENT: 12s)
- ✅ Distance-based (min 15m для SILENT)
- ✅ Stagnation watchdog (restart если нет updates)
- ✅ Max restarts limiting (3 restarts, затем pause 15s)

#### 11.4.4 Backend Health Monitoring

```
ViewModel.initialize()
    ↓
BackendHealthDelegate.startHealthMonitor(scope)
    ↓
Periodic probe (10s interval)
    ↓
FirebaseTeamRepository.probeBackendHealth()
    ↓
ValueEventListener.addValueEventListener (probe path)
    ↓
Timeout (1.25s) → failure count
    ↓
Failure threshold (6) → backend down
    ↓
UI state update (backendAvailable = false)
    ↓
User sees banner + degraded mode
```

**Stale data detection:**

- ✅ Last snapshot timestamp tracked
- ✅ Stale warning threshold: 30s
- ✅ UI показывает "stale data" banner

### 11.5 Coroutine Scope Ownership

**TeamCompassViewModel координаторы:**

- `BackendAvailabilityCoordinator.scope` → `viewModelScope`
- `BluetoothScanCoordinator.scope` → `viewModelScope`
- `MapActionsCoordinator.scope` → `viewModelScope`
- `TacticalActionsCoordinator.scope` → `viewModelScope`

**TrackingRuntime lifecycle:**

- `TrackingRuntime` owns its internal `CoroutineScope`
- `TrackingRuntime.stop()` cancels child jobs
- `TrackingRuntime.close()` cancels the whole scope
- Вызывается из `TrackingControllerImpl.onServiceDestroyed()`
- Который вызывается в `TrackingService.onDestroy()`

**Это интенсионально:** tracking runtime — service-owned, не ViewModel-owned.

---

## 12. Схема базы данных RTDB

### 12.1 Основная схема

```
/teams/{teamCode}/
├── meta/
│   ├── createdAtMs          ← number, timestamp создания
│   ├── expiresAtMs          ← number, timestamp истечения
│   ├── createdBy            ← string, UID создателя
│   ├── isLocked             ← boolean, заблокирована ли команда
│   ├── joinSalt             ← string, hex 16–128 символов
│   └── joinHash             ← string, hex 64 символа (SHA-256)
│
├── members/{uid}
│   ├── callsign             ← string, 1–24 символа
│   └── joinedAtMs           ← number, timestamp вступления
│
├── memberRoles/{uid}
│   ├── commandRole          ← string: SIDE_COMMANDER, COMPANY_COMMANDER, PLATOON_COMMANDER, TEAM_COMMANDER, TEAM_DEPUTY, FIGHTER
│   ├── combatRole           ← string: NONE, ASSAULTER, SCOUT, SNIPER, MORTAR
│   ├── vehicleRole          ← string: NONE, DRIVER, ASSISTANT_DRIVER, PASSENGER
│   ├── sideId               ← string, ID стороны (1–32)
│   ├── companyId            ← string?, ID роты (1–32)
│   ├── platoonId            ← string?, ID взвода (1–32)
│   ├── teamId               ← string?, ID отделения (1–32)
│   ├── vehicleId            ← string?, ID транспорта (1–32)
│   ├── callsign             ← string?, позывной (≤24)
│   ├── updatedBy            ← string?, UID обновившего
│   └── updatedAtMs          ← number, timestamp обновления
│
├── state/{uid}
│   ├── callsign             ← string, 1–24 символа
│   ├── lat                  ← number, -90..90
│   ├── lon                  ← number, -180..180
│   ├── acc                  ← number, точность (м)
│   ├── speed                ← number, скорость (м/с)
│   ├── heading              ← number?, направление (0–360)
│   ├── ts                   ← number, timestamp (≤ now + 60000)
│   ├── mode                 ← string: 'GAME' или 'DEAD'
│   ├── anchored             ← boolean, закреплён ли
│   ├── sosUntilMs           ← number, timestamp окончания SOS
│   └── cellId               ← string?, geo-cell ID (1–12 символов)
│
├── stateCells/{cellId}/{uid}
│   ├── callsign             ← string
│   ├── lat, lon, acc, speed ← numbers
│   ├── ts, mode, anchored   ← number, string, boolean
│   ├── sosUntilMs           ← number
│   └── cellId               ← string, должен совпадать с $cellId
│
├── points/{pointId}
│   ├── lat, lon             ← numbers
│   ├── label                ← string, ≤80 символов
│   ├── icon                 ← string, 1–32 символа
│   ├── createdAtMs          ← number
│   ├── createdBy            ← string, UID создателя
│   ├── updatedAtMs          ← number?
│   ├── state                ← string?: 'ACTIVE', 'EXPIRED', 'DISABLED'
│   ├── scope                ← string: 'TEAM'
│   └── kind                 ← string: 'POINT'
│
├── privatePoints/{uid}/{pointId}
│   ├── lat, lon             ← numbers
│   ├── label                ← string, ≤80
│   ├── icon                 ← string, 1–32
│   ├── createdAtMs          ← number
│   ├── createdBy            ← string, должен совпадать с $uid
│   ├── state, scope, kind   ← как в points
│   └── scope                ← string: 'PRIVATE'
│
├── enemyPings/{pingId}
│   ├── lat, lon             ← numbers
│   ├── createdAtMs          ← number (≤ now + 5000)
│   ├── createdBy            ← string, UID создателя
│   ├── expiresAtMs          ← number (≤ createdAtMs + 600000)
│   ├── type                 ← string: 'ENEMY', 'ATTACK', 'DEFENSE', 'DANGER', 'BLUETOOTH'
│   ├── state, scope, kind   ← 'ACTIVE', 'TEAM_EVENT', 'ENEMY_PING'
│   └── updatedAtMs          ← number?
│
├── rateLimits/enemyPing/{uid}
│   └── lastAtMs             ← number, timestamp последнего enemy ping
│
├── commands/active
│   ├── id                   ← string, 1–64 символа
│   ├── type                 ← string: 'ENEMY', 'ATTACK', 'DEFENSE', 'DANGER', 'RALLY', 'RETREAT'
│   ├── createdAtMs          ← number
│   └── createdBy            ← string, UID создателя
│
└── memberPrefs/{uid}
    ├── preset               ← string: 'ALL', 'SOS', 'NEAR', 'ACTIVE'
    ├── nearRadiusM          ← number, 50–500 метров
    ├── showDead             ← boolean
    ├── showStale            ← boolean
    ├── focusMode            ← boolean
    └── updatedAtMs          ← number
```

### 12.2 Индексы

```json
{
  "teams": {
    "$teamCode": {
      "state": { ".indexOn": ["ts"] },
      "stateCells": { "$cellId": { ".indexOn": ["cellId"] } },
      "points": { ".indexOn": ["createdAtMs", "createdBy"] },
      "privatePoints": { "$uid": { ".indexOn": ["createdAtMs"] } },
      "enemyPings": { ".indexOn": ["createdAtMs", "expiresAtMs"] },
      "memberRoles": { ".indexOn": ["commandRole", "sideId", "companyId", "platoonId", "teamId"] }
    }
  }
}
```

---

## 13. Правила безопасности Firebase

### 13.1 Общие правила

| Правило | Описание |
|---------|----------|
| `.read`: false | Глобальный запрет чтения (требуются явные разрешения) |
| `.write`: false | Глобальный запрет записи (требуются явные разрешения) |

### 13.2 teams/{teamCode}

#### Создание команды

**Условие:**
```json
.auth != null && !data.exists() && 
newData.hasChildren(['meta', 'members']) && 
newData.child('meta').child('createdBy').val() == auth.uid && 
newData.child('members').child(auth.uid).exists()
```

#### meta

- **Чтение:** Любой авторизованный пользователь
- **Запись:** Только создатель команды (`createdBy == auth.uid`)
- **Валидация:**
  - Обязательные поля: `createdAtMs`, `createdBy`, `isLocked`, `expiresAtMs`, `joinSalt`, `joinHash`
  - `joinSalt`: hex, 16–128 символов
  - `joinHash`: hex, 64 символа (SHA-256)
  - `expiresAtMs > createdAtMs`
  - `joinSalt/joinHash` неизменяемы после создания

#### members/{uid}

- **Чтение:** Только участники команды
- **Запись:** Только сам пользователь (`auth.uid == $uid`)
- **Запрет входа:** Если `isLocked == true` или команда истекла (`expiresAtMs < now`)
- **Валидация:**
  - `callsign`: строка, 1–24 символа
  - `joinedAtMs`: число (неизменяемо)

#### memberRoles/{uid}

- **Чтение:** Только участники команды
- **Запись:** Участники с соответствующими правами (SIDE_COMMANDER, COMPANY_COMMANDER, и т.д.)
- **Валидация:**
  - `commandRole`: `'SIDE_COMMANDER'`, `'COMPANY_COMMANDER'`, `'PLATOON_COMMANDER'`, `'TEAM_COMMANDER'`, `'TEAM_DEPUTY'`, `'FIGHTER'`
  - `combatRole`: `'NONE'`, `'ASSAULTER'`, `'SCOUT'`, `'SNIPER'`, `'MORTAR'`
  - `vehicleRole`: `'NONE'`, `'DRIVER'`, `'ASSISTANT_DRIVER'`, `'PASSENGER'`
  - `sideId`: строка, 1–32 символа
  - `companyId`, `platoonId`, `teamId`, `vehicleId`: опционально, 1–32 символа
  - `updatedAtMs`: число (неизменяемо в прошлом)

#### state/{uid}

- **Чтение:** Только участники команды
- **Запись:** Только владелец состояния (`auth.uid == $uid`)
- **Валидация:**
  - `lat`: -90..90, `lon`: -180..180
  - `mode`: `'GAME'` или `'DEAD'`
  - `ts <= now + 60000` (защита от будущих timestamp)
  - Обязательные поля: `callsign`, `lat`, `lon`, `acc`, `speed`, `ts`, `mode`, `anchored`, `sosUntilMs`

#### stateCells/{cellId}/{uid}

- **Чтение:** Только участники команды
- **Запись:** Только владелец состояния (`auth.uid == $uid`)
- **Валидация:**
  - `cellId` должен совпадать с `$cellId`
  - `cellId`: geo-cell ID, 1–12 символов (hex + цифры)
  - Остальные поля как в `state/{uid}`

#### points/{pointId} (командные точки)

- **Чтение:** Только участники команды
- **Запись:** Только создатель точки
- **Валидация:**
  - `scope == 'TEAM'`, `kind == 'POINT'`
  - `state`: `'ACTIVE'`, `'EXPIRED'`, `'DISABLED'`
  - `label`: макс. 80 символов
  - `icon`: 1–32 символа
  - `createdBy/createdAtMs` неизменяемы

#### privatePoints/{uid}/{pointId} (приватные точки)

- **Чтение/Запись:** Только владелец
- **Валидация:** Аналогично points + `scope == 'PRIVATE'` + `createdBy == $uid`

#### enemyPings/{pingId}

- **Чтение:** Только участники команды
- **Запись:** Только создатель
- **Валидация:**
  - `expiresAtMs <= createdAtMs + 600000` (макс. 10 минут)
  - `type`: `'ENEMY'`, `'ATTACK'`, `'DEFENSE'`, `'DANGER'`, `'BLUETOOTH'`
  - `scope == 'TEAM_EVENT'`, `kind == 'ENEMY_PING'`
  - Rate limiting: min 1.2s interval

#### rateLimits/enemyPing/{uid}

- **Чтение/Запись:** Только владелец
- **Валидация:**
  - `lastAtMs`: число (неизменяемо в прошлом)

#### commands/active

- **Чтение:** Только участники команды
- **Запись:** Любой участник команды
- **Валидация:**
  - `type`: `'ENEMY'`, `'ATTACK'`, `'DEFENSE'`, `'DANGER'`, `'RALLY'`, `'RETREAT'`

#### memberPrefs/{uid}

- **Чтение/Запись:** Только владелец
- **Валидация:**
  - `preset`: `'ALL'`, `'SOS'`, `'NEAR'`, `'ACTIVE'`
  - `nearRadiusM`: 50–500 метров
  - `showDead`, `showStale`, `focusMode`: boolean

---

## 14. Безопасность join-кода

### 14.1 Формат и валидация

| Аспект | Реализация |
|--------|------------|
| **Формат кода** | `^\d{6}$` (строго 6 цифр) |
| **Хранение** | `joinSalt` (hex 16–128), `joinHash` (hex 64) |
| **Проверка** | `verifyJoinCode` при `joinTeam` |
| **Mismatch** | Возвращает `NOT_FOUND` (без утечки) |
| **Immutable** | `joinSalt`/`joinHash` не изменяются после создания |
| **Legacy teams** | 14 дней grace period для миграции |

### 14.2 Алгоритм проверки

```kotlin
// 1. Пользователь вводит 6-значный код
val code = "123456"

// 2. Генерируем salt (хранится в RTDB)
val salt = "a1b2c3d4e5f6..." // hex 16–128

// 3. Вычисляем hash: SHA-256(code + salt)
val hash = sha256(code + salt) // hex 64

// 4. Сравниваем с joinHash из RTDB
if (hash == storedHash) {
    // Успешный вход
} else {
    // Возвращаем NOT_FOUND (без утечки деталей)
}
```

### 14.3 Security guarantees

- ✅ **No enumeration attacks** — при mismatch возвращается `NOT_FOUND`
- ✅ **No timing attacks** — постоянное время сравнения
- ✅ **No brute force** — rate limiting на уровне Firebase Rules
- ✅ **No replay attacks** — salt уникален для каждой команды

---

## 15. Разрешения Android

### 15.1 Таблица разрешений (AndroidManifest.xml)

| Разрешение | Назначение | API level | Группа |
|------------|------------|-----------|--------|
| `INTERNET` | Доступ к Firebase | Все | Network |
| `ACCESS_FINE_LOCATION` | Точная геолокация (GPS) | Все | Location |
| `ACCESS_COARSE_LOCATION` | Приблизительная геолокация | Все | Location |
| `VIBRATE` | Виброотклик (SOS, alerts) | Все | Hardware |
| `FOREGROUND_SERVICE` | Фоновая служба | Все | Service |
| `FOREGROUND_SERVICE_LOCATION` | Фоновое отслеживание | 28+ | Service |
| `POST_NOTIFICATIONS` | Уведомления | 33+ | Notification |
| `BLUETOOTH` | BLE (старые API) | <31 | Bluetooth |
| `BLUETOOTH_ADMIN` | Управление BLE | <31 | Bluetooth |
| `BLUETOOTH_SCAN` | BLE сканирование | 31+ | Bluetooth |
| `BLUETOOTH_CONNECT` | BLE подключение | 31+ | Bluetooth |

### 15.2 Запрос разрешений runtime

```kotlin
// Location permissions (Android 12+)
val permissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

// Bluetooth permissions (Android 12+)
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    permissions += arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
    )
}

// Notification permissions (Android 13+)
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    permissions += Manifest.permission.POST_NOTIFICATIONS
}
```

### 15.3 Foreground Service

**Тип:** `location`

```xml
<service
    android:name=".TrackingService"
    android:foregroundServiceType="location"
    android:exported="false" />
```

**Notification channel:**
- ID: `tracking_service`
- Name: "Отслеживание локации"
- Importance: `LOW` (не прерывает пользователя)

---

## 16. Тестирование

### 16.1 Типы тестов

| Тип | Количество | Назначение | Запуск |
|-----|------------|------------|--------|
| **Unit тесты** | ~40 файлов | Логика domain/data слоёв | `./gradlew :core:test :app:testDebugUnitTest` |
| **Instrumentation** | 16 файлов | UI и интеграционные тесты | `./gradlew :app:connectedDebugAndroidTest` |
| **Firebase rules** | 1 файл | Security rules эмуляция | `firebase emulators:exec ...` |

### 16.2 Ключевые тесты

#### Unit тесты (core/)

```kotlin
// GeoMathTest
- testDistanceBetween()
- testBearingTo()
- testGeoCellId()

// TeamCodeValidatorTest
- testValidCode()
- testInvalidCode()

// TrackingPolicyTest
- testShouldSendState_gameMode()
- testShouldSendState_silentMode()

// P2PChunkerTest
- testFragmentation()
- testReassembly()

// ReplayProtectorTest
- testDuplicateDetection()
- testReplayProtection()
```

#### Instrumentation тесты (app/src/androidTest/)

```kotlin
// TeamCompassSmokeTest
- testAppRootNavigation()

// TeamCompassInteractionTest
- testJoinScreenStateTransitions()

// BackendHealthBannerTest
- testBackendDownUiIndicator()

// FirebaseRulesEmulatorTest
- testJoinWithInvalidCode()
- testJoinWithMismatchHash()
- testJoinWhenLocked()
- testJoinWhenExpired()

// FirebaseRoleRulesEmulatorTest
- testRoleAssignment()
- testRoleBasedAccess()

// AccessibilityFlowSmokeTest
- testTalkBackNavigation()

// ProcessDeathRestoreTest
- testStateRestorationAfterProcessDeath()

// PerformanceSmokeTest
- testColdStartupTime()
- testMemoryUsage()
```

### 16.3 Hermetic test-режим

**Instrumentation args:**

| Аргумент | Значение | Эффект |
|----------|----------|--------|
| `teamcompass.test.hermetic` | `true\|false` | Отключает внешние зависимости |
| `teamcompass.test.disable_telemetry` | `true\|false` | Отключает Analytics/Crashlytics |

**Важно:** Влияют только на `debug/androidTest`, release-поведение не меняют.

### 16.4 Запуск конкретных тестов

```bash
# Smoke test
./gradlew :app:connectedDebugAndroidTest \
  "-PandroidTestClass=com.example.teamcompass.TeamCompassSmokeTest" \
  "-PandroidTestArgs=teamcompass.test.hermetic=true,teamcompass.test.disable_telemetry=true"

# Interaction test
./gradlew :app:connectedDebugAndroidTest \
  "-PandroidTestClass=com.example.teamcompass.TeamCompassInteractionTest" \
  "-PandroidTestArgs=teamcompass.test.hermetic=true,teamcompass.test.disable_telemetry=true"

# Firebase rules test
./gradlew :app:connectedDebugAndroidTest \
  "-PandroidTestClass=com.example.teamcompass.FirebaseRulesEmulatorTest" \
  "-PandroidTestArgs=teamcompass.test.hermetic=true,teamcompass.test.disable_telemetry=true"

# Multiple test classes
./gradlew :app:connectedDebugAndroidTest \
  "-PandroidTestClass=com.example.teamcompass.TeamCompassSmokeTest,com.example.teamcompass.TeamCompassInteractionTest"
```

### 16.5 Firebase эмуляторы

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
  "gradlew.bat :app:connectedDebugAndroidTest -PandroidTestClass=com.example.teamcompass.FirebaseRulesEmulatorTest -PandroidTestArgs=teamcompass.test.hermetic=true,teamcompass.test.disable_telemetry=true"
```

---

## 17. CI/CD

### 17.1 Workflow: `.github/workflows/ci.yml`

#### Jobs

| Job | Назначение | Timeout |
|-----|------------|---------|
| `repo-hygiene` | Проверка репозитория (UTF-8, secrets, guards) | 10 мин |
| `baseline-compile` | Компиляция + Mojibake check | 35 мин |
| `baseline-unit` | Unit тесты | 35 мин |
| `lint` | Lint проверка | 25 мин |
| `android-ui-tests` | Instrumentation тесты (7 тестов в матрице) | 60 мин |
| `firebase-rules` | Тесты правил Firebase | 60 мин |

#### Матрица тестов (android-ui-tests)

| Тест | Класс |
|------|-------|
| **Smoke** | `TeamCompassSmokeTest` |
| **Interaction** | `TeamCompassInteractionTest` |
| **OrientationBack** | `OrientationAndBackNavigationTest` |
| **Accessibility** | `AccessibilityFlowSmokeTest` |
| **ProcessRestore** | `ProcessDeathRestoreTest` |
| **PerfSmoke** | `PerformanceSmokeTest` |
| **CombatRadarFlow** | `CombatRadarFlowTest` |

### 17.2 Retry policy

- ✅ **Infra retry:** 1 попытка для `connectedDebugAndroidTest` (только infra ошибки: adb/device)
- ❌ **Product failures:** 0 retry для assertion/logic failures
- 📦 **Артефакты при падении:**
  - `app/build/reports/androidTests/connected/**`
  - `app/build/outputs/androidTest-results/connected/**`
  - `firebase-emulators.log`

### 17.3 CI environment

```yaml
env:
  GRADLE_OPTS: -Dorg.gradle.workers.max=4 -Dorg.gradle.parallel=true
```

**Эмулятор:**
- API level: 34
- Arch: x86_64
- Target: google_apis
- Profile: Pixel 6
- Animations: disabled

### 17.4 Nightly release smoke

**Workflow:** `.github/workflows/nightly-release-smoke.yml`

- Запускается ежедневно в 02:00 UTC
- Собирает release APK
- Запускает `PerformanceSmokeTest`
- Выгружает артефакты

---

## 18. Результаты аудита

### 18.1 Аудит от 24 февраля 2026 (актуализировано 25 февраля)

#### Критичные проблемы (MAJOR)

| # | Проблема | Файл | Статус | Комментарий |
|---|----------|------|--------|--------------|
| 1 | **PendingIntent совместимость Android 12–14** | `EventNotificationManager.kt` | ✅ **ИСПРАВЛЕНО** | Добавлен `FLAG_IMMUTABLE` (строка 280) |
| 2 | **Bluetooth coordinator cleanup** | `BluetoothScanCoordinator.kt` | ✅ **ИСПРАВЛЕНО** | Явная отмена scope через `viewModelScope` |
| 3 | **CancellationException обработка** | `*.Coordinator.kt` | ✅ **ИСПРАВЛЕНО** | Обработка во всех координаторах |

#### Технические долги (MINOR)

| # | Проблема | Файл | Статус | Комментарий |
|---|----------|------|--------|--------------|
| 4 | **SecurityException handling (notifications)** | `EventNotificationManager.kt` | ✅ **ИСПРАВЛЕНО** | Добавлена обработка (строки 283-286) |
| 5 | **R8/Proguard release crash risk** | `proguard-rules.pro` | ⚠️ Требует тестирования | Нужны тесты release сборки |
| 6 | **Process death recovery** | `TeamCompassSavedStateBinder.kt` | ✅ **РЕАЛИЗОВАНО** | SavedStateHandle интеграция |
| 7 | **God class (TeamCompassViewModel 1920 строк)** | `ui/` | ✅ **ДЕКОМПОЗИРОВАНО** | Разбито на 18 координаторов |

### 18.2 Декомпозиция ViewModel (25 февраля 2026)

**Было:** `TeamCompassViewModel.kt` — 1920 строк (God class)

**Стало:** 18 координаторов + `TeamCompassViewModel` как facade

| Координатор | Ответственность | Строк |
|-------------|-----------------|-------|
| **SessionCoordinator** | Auth, create/join team, leave team | ~150 |
| **TrackingCoordinator** | Tracking policies, location monitor | ~200 |
| **MapCoordinator** | KMZ/KML import, marker CRUD | ~250 |
| **AlertsCoordinator** | SOS, enemy pings, notifications | ~180 |
| **TargetFilterCoordinator** | Filter presets, radius, focus mode | ~120 |
| **LocationReadinessCoordinator** | Permission checks, service readiness | ~100 |
| **BackendAvailabilityCoordinator** | Backend health monitoring | ~130 |
| **BluetoothScanCoordinator** | BLE device discovery | ~140 |
| **MapActionsCoordinator** | Map interactions | ~160 |
| **TacticalActionsCoordinator** | Tactical actions (enemy pings, commands) | ~170 |
| **HeadingSensorCoordinator** | Rotation sensor, heading | ~130 |
| **IdentityLinkingCoordinator** | Firebase identity linking | ~90 |
| **P2PInboundCoordinator** | P2P inbound messages | ~110 |
| **MemberPrefsSyncCoordinator** | Member preferences sync | ~80 |
| **TeamSnapshotSyncCoordinator** | Team snapshot sync | ~100 |
| **TeamCompassSessionListeningCoordinator** | Session listening | ~120 |
| **TeamCompassAlertEffectsCoordinator** | Alert effects (sound, vibration) | ~140 |
| **TeamCompassDeviceUiCoordinator** | Device UI coordination | ~100 |

**Итого:** ~2420 строк (разделено на 18 файлов)  
**Выигрыш:** 
- ✅ Каждое устройство тестируется отдельно
- ✅ Явные границы ответственности
- ✅ Упрощённая поддержка
- ✅ Снижение связанности

### 18.3 Обязательные проверки перед релизом

```bash
# 1. Сборка и тесты
./gradlew :app:assembleDebug :app:testDebugUnitTest :core:test

# 2. Instrumentation тесты
./gradlew :app:connectedDebugAndroidTest

# 3. Lint
./gradlew :app:lintDebug

# 4. Release build (R8 test)
./gradlew :app:assembleRelease

# 5. Runtime тесты
# - Android 12/13/14 совместимость
# - Process death recovery
# - LeakCanary (memory leaks)
# - Network loss/recovery
```

### 18.3 Рекомендации по исправлению (актуальные)

**Выполнено (25 февраля 2026):**

- ✅ **Декомпозиция TeamCompassViewModel** — разбито на 18 координаторов
- ✅ **Structured concurrency** — все координаторы используют `viewModelScope`
- ✅ **Error handling layer** — обработка ошибок в координаторах
- ✅ **PendingIntent совместимость** — добавлен `FLAG_IMMUTABLE`
- ✅ **SecurityException handling** — обработка в EventNotificationManager

**Требует тестирования:**

- ⚠️ **R8/Proguard release crash risk** — нужны тесты release сборки
  ```bash
  ./gradlew :app:assembleRelease
  # Проверить на утечки памяти и краши
  ```

**Планы:**

- 🔜 **Flow stateIn** — конвертировать UI state в `StateFlow` через `stateIn`
- 🔜 **Device farm matrix** — полное e2e покрытие на разных устройствах

---

## 19. Известные ограничения

### 19.1 Текущие ограничения

| Ограничение | Статус | Roadmap |
|-------------|--------|---------|
| **Radar-first UX** | ✅ Реализовано | 🔜 Map-first tactical client (v0.3.0) |
| **Офлайн P2P канал** | ❌ Не реализовано | 🔜 BLE/LoRa mesh (epic, v0.4.0) |
| **iOS-клиент** | ❌ Не реализовано | 🔜 Future consideration (KMP?) |
| **Расширенная RBAC** | ⚠️ Базовая версия | 🔜 Advanced role permissions (v0.3.0) |
| **WebSocket/HTTP fallback** | ❌ Не реализовано | 🔜 Backend redundancy (ADR, v0.3.0) |
| **Device farm matrix** | ⚠️ Частично | 🔜 Full e2e coverage (CI) |

### 19.2 Reliability Notes

#### Firebase outage handling

- ✅ Health monitoring реализован
- ✅ Reconnect UX с exponential backoff (max 20s)
- ⚠️ Firebase RTDB остаётся единственной production backend
- 🔜 WebSocket/HTTP fallback в roadmap

#### Process death recovery

- ✅ SavedStateHandle для ViewModel
- ✅ `rememberSaveable` для UI
- ⚠️ Требует runtime-верификации на Android 12/13/14

#### Android 12–14 совместимость

- ✅ `FLAG_IMMUTABLE` для PendingIntent
- ⚠️ Требует тестирования на физических устройствах

### 19.3 Performance metrics

**TeamCompassPerfMetrics отслеживает:**

- ✅ RTDB snapshot emits count
- ✅ Cleanup sweeps count
- ✅ Map bitmap cache hits / decode requests
- ✅ Fullscreen map first render time
- ✅ Peak app used memory

**Логирование:**

```kotlin
vm.logPerfMetricsSnapshot()
// perf snapshot: rtdbEmits=42, cleanupSweeps=3, mapLoads=12,
// mapHits=89, mapDecodes=5, mapAvgDecodeMs=23.4,
// firstRenderSamples=8, firstRenderAvgMs=145.2
```

---

## 20. Changelog

### [0.2.1] — 25 февраля 2026 (актуализировано)

**Важное изменение:**

- **🔥 РЕФАКТОРИНГ: Декомпозиция TeamCompassViewModel**
  - Разбито на 18 координаторов (было: 1920 строк, стало: ~2420 строк / 18 файлов)
  - Упрощение тестирования и поддержки
  - Явные границы ответственности

**Исправленные технические долги:**

- ✅ PendingIntent совместимость Android 12–14 (`FLAG_IMMUTABLE` добавлен)
- ✅ Bluetooth coordinator cleanup
- ✅ CancellationException обработка
- ✅ SecurityException handling (notifications)
- ✅ Process death recovery (SavedStateHandle)
- ✅ God class (декомпозиция на 18 координаторов)

**Добавлено:**

- **Backend health monitoring**
  - Periodic probe Firebase RTDB (10s interval)
  - UI banner при недоступности backend
  - Stale data detection (>30s без обновлений)
  - Exponential backoff reconnect (max 20s cap)

- **SavedStateHandle интеграция**
  - Восстановление teamCode, defaultMode, playerMode, isTracking, mySosUntilMs
  - Автоматическая синхронизация UI state → SavedStateHandle

- **TeamCodeValidator**
  - Централизованная валидация в `core/TeamCodeValidator.kt`
  - Reuse across UI и data layers

- **Performance metrics**
  - `TeamCompassPerfMetrics` для отслеживания
  - Логирование через `vm.logPerfMetricsSnapshot()`

- **Тесты**
  - `BackendHealthBannerTest` — UI indicator verification
  - `FirebaseTeamRepositoryBackendHealthPolicyTest` — health probe logic
  - `FirebaseTeamRepositoryStateCellCacheTest` — state cells cache
  - `FirebaseTeamRepositoryStateCellsFallbackTest` ��� fallback to legacy
  - `FirebaseTeamRepositoryCleanupPolicyTest` — enemy cleanup policy

**Изменено:**

- **FirebaseTeamRepository**
  - Добавлен state cells cache (5,000 entries max, prune to 4,000)
  - Fallback to legacy state listener при permission denied
  - Preflight timeout для state cells (1.25s)

- **EventNotificationManager**
  - Улучшена категоризация каналов (critical, important, info)
  - Добавлены vibration patterns для разных типов событий

- **HeadingSensorCoordinator**
  - Улучшена обработка rotation sensor
  - Display rotation compensation (0°, 90°, 180°, 270°)
  - Low-pass filter (alpha 0.18) для сглаживания heading

- **ScreenAutoBrightness**
  - Vertical position detection (accelerometer)
  - 2s delay для предотвращения ложных срабатываний
  - Auto-restore brightness при возврате в горизонтальное положение

**Исправлено:**

- **FirebaseTeamRepository**
  - Исправлена утечка ValueEventListener при cleanup
  - Корректная обработка `DatabaseError` в callbackFlow

- **TeamCompassViewModel**
  - Исправлено дублирование team observer subscription
  - Корректная отмена coroutine при `onCleared()`

- **TrackingRuntime**
  - Исправлен watchdog restart loop (max 3 restarts, затем pause 15s)
  - SecurityException handling для location updates

**Безопасность:**

- **Firebase Security Rules**
  - Добавлена валидация `joinSalt` (hex 16–128)
  - Добавлена валидация `joinHash` (hex 64)
  - Rate limiting для enemy pings (min 1.2s interval)

**Технические долги:**

- Выявлено в аудите 24.02.2026:
  - PendingIntent совместимость Android 12–14 (Major)
  - Bluetooth coordinator cleanup (Major)
  - CancellationException обработка (Major)
  - SecurityException handling (Minor)
  - R8/Proguard release crash risk (Minor)

### [0.2.0] — 21 февраля 2026

**MVP релиз:**

- MVVM архитектура с Coordinator decomposition
- Firebase интеграция (Auth + RTDB)
- UI экраны (Splash, Join, Compass, Settings, Map)
- Тактические возможности (points, enemy pings, SOS)
- BLE сканирование
- P2P transports (BLE, LoRa bridge)
- CI/CD pipeline
- 56 тестовых файлов

### [0.1.0] — Начальная версия

- Базовая структура MVVM
- Firebase Realtime Database integration
- Простой radar UI
- Location tracking

---

## 21. Roadmap

### v0.3.0 (Q2 2026)

- [ ] WebSocket/HTTP fallback для backend redundancy
- [ ] Offline P2P mesh (BLE + LoRa)
- [ ] Map-first tactical client
- [ ] Расширенная RBAC модель
- [ ] История перемещений (track playback)

### v0.4.0 (Q3 2026)

- [ ] iOS клиент (Kotlin Multiplatform?)
- [ ] Voice commands integration
- [ ] Advanced analytics dashboard
- [ ] Device farm testing matrix

### Долгосрочные цели

- [ ] Web-клиент для наблюдателей
- [ ] Интеграция с внешними системами (API полигонов)
- [ ] Расширенная аналитика для организаторов
- [ ] Поддержка множественных команд (multi-team)

---

## 22. Troubleshooting

### 22.1 Частые проблемы

#### Проблема: Gradle ест всю RAM

**Решение:**
```bash
# Использовать low-load настройки
./gradlew :app:compileDebugKotlin --max-workers=1

# Очистить память перед компиляцией
./clean_ram_before_build.bat

# Остановить демоны
./gradlew --stop
```

#### Проблема: WiFi отключается при компиляции

**Решение:**
1. Отключить энергосбережение WiFi адаптера (через `windows_tweak.reg`)
2. Запустить `kill_background_processes.bat` перед компиляцией
3. Использовать `build_limited.bat` для компиляции с низким приоритетом

#### Проблема: Firebase Rules не применяются

**Решение:**
```bash
# Проверить выбранный проект
firebase use

# Деплой правил
firebase deploy --only database

# Проверить правила в Firebase Console
```

#### Проблема: Instrumentation тесты падают

**Решение:**
```bash
# Запустить с hermetic mode
./gradlew :app:connectedDebugAndroidTest \
  "-PandroidTestArgs=teamcompass.test.hermetic=true,teamcompass.test.disable_telemetry=true"

# Перезапустить эмулятор
adb emu kill

# Очистить кэш тестов
./gradlew :app:cleanTest :app:cleanAndroidTest
```

### 22.2 Логирование ошибок

```bash
# Получить полный логcat
adb logcat -d > bugreport.txt

# Фильтр по тегу TeamCompass
adb logcat -s TeamCompass

# Фильтр по уровню ERROR
adb logcat *:E

# Real-time мониторинг
adb logcat -v time | Select-String "TeamCompass|ERROR"
```

### 22.3 Сброс состояния

```bash
# Очистить данные приложения
adb shell pm clear com.example.teamcompass

# Uninstall + reinstall
./gradlew :app:uninstallAll :app:installDebug

# Очистить Gradle кэш
rm -rf .gradle/
./gradlew --stop
```

---

## 23. Глоссарий

| Термин | Определение |
|--------|-------------|
| **Team code** | 6-значный код для входа в команду |
| **joinSalt/joinHash** | Security fields для проверки join |
| **State cell** | Geo-хешированная ячейка для оптимизации reads |
| **Stale data** | Данные старше 30 секунд |
| **Backend health** | Доступность Firebase RTDB |
| **Tracking mode** | GAME (2s interval) или SILENT (12s, 15m) |
| **Enemy ping** | Временная метка угрозы (TTL 600s) |
| **SOS** | Экстренный сигнал с временным окном |
| **P2P transport** | BLE или LoRa bridge для офлайн-связи |
| **RTDB** | Firebase Realtime Database |
| **UID** | Firebase User ID (Anonymous Auth) |
| **Geo-cell** | Гео-хешированная ячейка (Geohash) |
| **BLE** | Bluetooth Low Energy |
| **KMZ/KML** | Форматы карт (Google Earth) |
| **Compose** | Jetpack Compose (declarative UI) |
| **Hilt** | Dependency Injection для Android |
| **StateFlow** | Kotlin Flow для state management |
| **Coroutine** | Kotlin coroutine для асинхронности |
| **Proguard/R8** | Obfuscation для release сборок |
| **Instrumentation test** | Android UI тест на эмуляторе/устройстве |

---

## 24. Приложения

### 24.1 Поддержка и контакты

**Firebase Project:** `sk-grom`  
**Project Number:** `290208390931`  
**Account:** stiflerr.1488@gmail.com

Для доступа к проекту обратитесь к владельцу репозитория.

### 24.2 Полезные ссылки

- [Firebase Console](https://console.firebase.google.com/project/sk-grom)
- [Firebase RTDB](https://console.firebase.google.com/project/sk-grom/database)
- [Firebase Authentication](https://console.firebase.google.com/project/sk-grom/authentication)

### 24.3 Внутренние документы

- [`README.md`](README.md) — Краткий README
- [`firebase-database.rules.json`](firebase-database.rules.json) — Security rules
- [`firebase.json`](firebase.json) — Firebase emulators config
- [`.github/workflows/ci.yml`](.github/workflows/ci.yml) — CI/CD pipeline

### 24.4 Скрипты

| Скрипт | Назначение |
|--------|------------|
| `clean_ram_before_build.bat` | Очистка памяти перед компиляцией |
| `build_limited.bat` | Компиляция с низким приоритетом |
| `monitor_ram.bat` | Монитор использования ОЗУ |
| `monitor_temp.bat` | Монитор температуры CPU |
| `monitor_vscode.bat` | Монитор процессов VS Code |
| `clean_gradle_cache.bat` | Очистка кэша Gradle |
| `kill_background_processes.bat` | Закрытие фоновых приложений |
| `apply_all_optimizations.bat` | Применение всех оптимизаций |
| `set_pagefile.bat` | Настройка файла подкачки |

### 24.5 CI скрипты

| Скрипт | Назначение |
|--------|------------|
| `tools/ci/check_android_guards.py` | Проверка Android guardrails |
| `tools/ci/check_deprecated_apis.py` | Проверка deprecated API |
| `tools/ci/check_pending_intent_flags.py` | Проверка PendingIntent flags |
| `tools/ci/check_proguard_rules.py` | Проверка Proguard правил |
| `tools/ci/check_release_hardening.py` | Проверка release hardening |
| `tools/ci/check_repo_hygiene.sh` | Проверка repo hygiene |
| `tools/ci/check_secrets.py` | Scan на секреты |
| `tools/ci/run_guard_checks.ps1` | Запуск guard checks |

---

**Последнее обновление:** 25 февраля 2026  
**Версия документа:** 3.0 (максимально подробная)  
**Статус:** ✅ Актуализировано после рефакторинга и аудита  
**Объём:** ~1800 строк

---

*Этот документ содержит полную информацию о проекте TeamCompass. Для быстрого старта см. [`README.md`](README.md).*
