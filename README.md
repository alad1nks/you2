# start-kmp

Стартовый шаблон Kotlin Multiplatform для новых приложений под Android, iOS, Desktop (JVM) и Web (Kotlin/JS).
Репозиторий предназначен для клонирования: после замены имени, идентификаторов и настроек публикации
можно развивать приложение на готовой модульной основе с общим интерфейсом Compose Multiplatform.

## Что уже есть

- Общий UI на Compose Multiplatform и Material 3; отдельные точки входа для четырёх платформ.
- Разделение на UI, сценарии использования, репозиторий, модели и хранилище настроек.
- Koin для внедрения зависимостей, ViewModel и Flow для состояния интерфейса.
- Чтение и сохранение темы и языка: DataStore на Android/iOS/Desktop, `localStorage` в браузере.
- Светлая и тёмная палитры, инфраструктура выбора системного, русского, английского и казахского языка.
- Каталог версий Gradle и ktlint для Kotlin-кода.

Текущий экран — демонстрация с кнопкой и приветствием. Экрана настроек, навигации, сетевого слоя и базы данных
в шаблоне нет. Файлы строковых ресурсов пока пустые; текст демонстрационного экрана задан прямо в коде.
Workflow-файлы GitHub Actions требуют адаптации перед использованием в новом репозитории.

## Окружение

Версии ниже взяты из конфигурации проекта. Основной источник версий зависимостей —
[gradle/libs.versions.toml](gradle/libs.versions.toml).

| Компонент | Значение в проекте |
| --- | --- |
| Kotlin | 2.4.0 |
| Compose Multiplatform | 1.11.1 |
| Material 3 | 1.9.0 |
| Android Gradle Plugin | 9.2.1 |
| Gradle Wrapper | 9.6.0 |
| JVM для Gradle daemon | Amazon JDK 21 |
| Android SDK | compile/target 37, min 28 |
| iOS deployment target | 18.2 |
| Koin / DataStore / Coroutines | 4.2.2 / 1.2.1 / 1.11.0 |

Для работы нужны Git и установленный JDK. Критерии JVM для Gradle daemon заданы в
[gradle/gradle-daemon-jvm.properties](gradle/gradle-daemon-jvm.properties): версия 21, поставщик Amazon.
При отсутствии подходящей JVM Gradle может загрузить её по указанным там URL; первая сборка требует доступа к сети.
Настройка Android `jvmTarget = JVM_11` относится к байткоду, а не к версии JVM для запуска Gradle.

Для Android установите Android SDK с платформой API 37 и задайте путь через Android Studio
или `sdk.dir` в локальном `local.properties` (этот файл исключён из Git).
Для iOS нужны macOS и Xcode с SDK, поддерживающим deployment target проекта.
Настроены `iosArm64` и `iosSimulatorArm64`; цели для симулятора Intel (`iosX64`) нет.
Для Web нужен браузер; в Gradle настроен Kotlin/JS, цель Wasm отсутствует.

## Создание своего проекта

1. Склонируйте шаблон в каталог нового приложения:

   ```bash
   git clone https://github.com/alad1nks/start-kmp.git my-app
   cd my-app
   ```

2. Создайте свой пустой репозиторий и замените `origin` (подставьте свой URL):

   ```bash
   git remote set-url origin https://github.com/YOUR_ACCOUNT/YOUR_REPOSITORY.git
   ```

   Такой способ сохраняет историю шаблона. До первого push адаптируйте или удалите workflow-файлы,
   описанные в разделе «GitHub Actions».

3. Переименуйте приложение по таблице ниже. Например, используйте `myapp` как имя проекта
   и `com.example.myapp` как базовый пакет. Для Kotlin-пакетов удобно использовать рефакторинг IDE,
   чтобы вместе с объявлениями обновить импорты и каталоги исходников.

| Что заменить | Где |
| --- | --- |
| `rootProject.name = "startkmp"` | [settings.gradle.kts](settings.gradle.kts) |
| `com.alad1nks.startkmp`: пакеты, импорты и соответствующие каталоги | Kotlin-исходники во всех модулях |
| `namespace` во всех Android/KMP-модулях; `applicationId`, `versionCode`, `versionName` приложения | Модульные `build.gradle.kts`, в том числе [androidApp/build.gradle.kts](androidApp/build.gradle.kts) |
| Android-название и иконки | [strings.xml](androidApp/src/main/res/values/strings.xml), ресурсы `mipmap*` и `drawable*` в `androidApp/src/main/res` |
| `PRODUCT_NAME`, `PRODUCT_BUNDLE_IDENTIFIER`, `TEAM_ID`, версии | [Config.xcconfig](iosApp/Configuration/Config.xcconfig) |
| Ссылки на `startkmp.app`, иконка iOS | [project.pbxproj](iosApp/iosApp.xcodeproj/project.pbxproj), [AppIcon.appiconset](iosApp/iosApp/Assets.xcassets/AppIcon.appiconset) |
| Desktop `mainClass`, `packageName`, `packageVersion` | [desktopApp/build.gradle.kts](desktopApp/build.gradle.kts) |
| Заголовок окна Desktop | [main.kt](desktopApp/src/main/kotlin/com/alad1nks/startkmp/main.kt) |
| Заголовок страницы Web | [index.html](webApp/src/webMain/resources/index.html) |
| Имя файла настроек `startkmp.preferences_pb` | [StorageDataStoreFileName.kt](core/storage/datastore/src/commonMain/kotlin/com/alad1nks/startkmp/core/storage/datastore/StorageDataStoreFileName.kt) |
| Репозиторий публикации, пути и имена артефактов, автор коммитов | [.github/workflows](.github/workflows) |

В iOS bundle identifier сейчас используется `com.alad1nks.startkmp.startkmp$(TEAM_ID)` —
задайте свой идентификатор явно. `TEAM_ID` используется также для подписи приложения.

Импорты ресурсов в `App.kt` сейчас начинаются с `startkmp.shared.generated.resources`.
После изменения имени проекта синхронизируйте Gradle и обновите импорты под сгенерированный пакет.
Сгенерированные файлы в `build/` редактировать не нужно.

Названия модулей `shared`, `androidApp`, `desktopApp`, `webApp` и framework `Shared` можно сохранить.
Если переименовываете `Shared`, согласованно измените `baseName` в `shared/build.gradle.kts`
и `import Shared` в `iosApp/iosApp/ContentView.swift`. При переименовании `webApp` проверьте также
имя скрипта `webApp.js` в HTML, а при переименовании `shared` — Gradle-команду в build phase Xcode.

4. Проверьте оставшиеся упоминания исходного проекта (команда использует ripgrep):

   ```bash
   rg -n --hidden -g '!.git' -g '!**/build/**' -g '!.gradle' -g '!.kotlin' \
     'startkmp|start-kmp|alad1nks' .
   ```

5. Синхронизируйте проект в IDE, соберите нужные платформы и замените демонстрационный экран своим.
   Обновите этот README под новое приложение.

## Запуск и сборка

Команды выполняются из корня репозитория. На Windows используйте `gradlew.bat` вместо `./gradlew`.
Если в Unix потерян исполняемый бит, выполните `chmod +x gradlew`.
Команды ниже соответствуют настроенным модулям и плагинам; их выполнение зависит от установленного SDK/JDK.

### Android

```bash
./gradlew :androidApp:assembleDebug
```

APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.
Для запуска выберите модуль `androidApp` и устройство/эмулятор в Android Studio либо установите сборку
на подключённое устройство через `./gradlew :androidApp:installDebug`.

В `androidApp/build.gradle.kts` пока нет release signing configuration.
Подпись нужно настроить отдельно до выпуска приложения; переменные `SIGNING_*` из CI сейчас в сборке не читаются.

### Desktop

```bash
./gradlew :desktopApp:run
```

В настройках дистрибутивов перечислены DMG, MSI и DEB.
Пакет для текущей ОС собирается командой `./gradlew :desktopApp:packageDistributionForCurrentOS`;
результаты находятся в `desktopApp/build/compose/binaries`.
Каждый формат следует собирать на соответствующей ОС.
Плагин Compose Hot Reload в текущей конфигурации не подключён, поэтому команда `hotRun` не входит в инструкции.

### Web

```bash
./gradlew :webApp:jsBrowserDevelopmentRun
```

Для production-сборки:

```bash
./gradlew :webApp:jsBrowserDistribution
```

Статические файлы сборки находятся в `webApp/build/dist/js/productionExecutable`.
Точка входа и HTML расположены в `webApp/src/webMain`.

### iOS

Откройте [iosApp/iosApp.xcodeproj](iosApp/iosApp.xcodeproj) в Xcode, выберите схему `iosApp`
и устройство или ARM64-симулятор. Для запуска на устройстве настройте команду разработчика и bundle identifier
в `Config.xcconfig`. Затем запустите приложение из Xcode.

Build phase `Compile Kotlin Framework` вызывает `:shared:embedAndSignAppleFrameworkForXcode`.
Общий UI экспортируется в статическом framework `Shared`, который подключён из SwiftUI.
Отдельная проверка сборки framework на macOS:

```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
./gradlew :shared:linkReleaseFrameworkIosArm64
```

Эти команды собирают framework; готовое iOS-приложение и архив для публикации собираются в Xcode.

## Устройство проекта

| Модуль / каталог | Назначение |
| --- | --- |
| [androidApp](androidApp) | Android-приложение, `MainActivity`, манифест и ресурсы лаунчера |
| [iosApp](iosApp) | Xcode-проект и SwiftUI-обёртка над `MainViewController` |
| [desktopApp](desktopApp) | JVM-точка входа, окно и настройки установщиков |
| [webApp](webApp) | Браузерная точка входа `ComposeViewport`, HTML и CSS |
| [shared](shared) | Общий `App`, демонстрационный UI, `MainViewModel`, сборка DI и платформенные адаптеры |
| [core/model](core/model) | Общие модели, сейчас `Language` |
| [core/domain](core/domain) | `GetDarkTheme`, `SetDarkTheme`, `GetLanguage`, `SetLanguage` и их реализации |
| [core/data](core/data) | `SettingsRepository`, преобразование сохранённого кода языка в модель |
| [core/storage/common](core/storage/common) | Контракты `AppPreferences`, `Storage` и общая реализация `StorageImpl` |
| [core/storage/datastore](core/storage/datastore) | DataStore Preferences для Android, iOS и JVM |
| [core/storage/js](core/storage/js) | Реализация `AppPreferences` поверх браузерного `localStorage` |
| [core/designsystem](core/designsystem) | `AppTheme` и палитры Material 3 |
| [resources](resources) | Общие строковые ресурсы и заготовка `AppResource` |

Поток обращения к настройкам:

```text
App → MainViewModel → GetDarkTheme / GetLanguage
                           ↓
                    SettingsRepository
                           ↓
                    Storage → AppPreferences
                                  ├─ DataStore (Android / iOS / JVM)
                                  └─ localStorage (JS)
```

`core/domain` зависит от `core/data` и `core/model`; `core/data` — от `core/model` и `core/storage/common`.
Конкретное хранилище подключается через платформенные зависимости `shared`.

Общий код размещается в `src/commonMain`, платформенный — в `androidMain`, `iosMain`, `jvmMain`, `jsMain`.
В `webApp` точка входа находится в `webMain`, а в `desktopApp` — в `main`.
Для платформенных различий используются `expect`/`actual`: `getPlatform`, `LocalAppLocale`,
`PlatformModules` и путь к файлу DataStore.

`App()` создаёт `KoinApplication` и подключает `CommonModules + PlatformModules`.
Общие модули регистрируют ViewModel, сценарии использования, репозиторий и `Storage`;
платформенные — реализацию `AppPreferences`.

### Тема, язык и сохранение настроек

`MainViewModel` отдаёт потоки темы и языка. `App` применяет тему через `AppTheme`, а язык — через `LocalAppLocale`.
Сценарии `SetDarkTheme` и `SetLanguage` уже зарегистрированы в Koin, но UI для их вызова нужно добавить.

При отсутствии сохранённой темы `GetDarkThemeImpl` возвращает `false`: после чтения настроек выбирается светлая тема.
Системная тема используется только как начальное значение при подписке в UI.
Неизвестный или отсутствующий код языка преобразуется в `Language.SYSTEM`.

| Платформа | Где хранятся настройки |
| --- | --- |
| Android | `filesDir/startkmp.preferences_pb` |
| iOS | Каталог Documents приложения, `startkmp.preferences_pb` |
| Desktop | `java.io.tmpdir/startkmp.preferences_pb` |
| Web | `localStorage` текущего origin, ключи `dark_theme` и `language` |

Для Desktop перед выпуском приложения выберите постоянный каталог данных пользователя:
временная папка может очищаться, а копии шаблона с прежним именем файла используют один путь.
В Web приложения на одном origin используют одинаковые ключи — при необходимости добавьте префикс своего приложения.

### Как развивать шаблон

- Общий экран и его ViewModel добавляйте в `shared`; общие визуальные компоненты и тему — в `core/designsystem`.
- Модели размещайте в `core/model`, сценарии использования — в `core/domain`, доступ к данным — в `core/data`.
- Для новой настройки расширьте `Storage`/`StorageImpl`, репозиторий и сценарии использования.
  Если достаточно строк и Boolean, существующий `AppPreferences` уже содержит нужные операции.
- Регистрируйте зависимости в DI-модуле соответствующего слоя. Новый Koin-модуль подключайте через
  `CommonModules` или платформенный `PlatformModules`; новый Gradle-модуль — через `settings.gradle.kts`.
- Заполняйте строки в `resources/src/commonMain/composeResources/values/strings.xml`,
  переводы — в `values-ru` и `values-kk`. При добавлении языка расширьте `Language` и добавьте каталог ресурсов.
  Демонстрационная картинка пока хранится отдельно в `shared/src/commonMain/composeResources`.

## Проверки

```bash
./gradlew ktlintCheck
./gradlew ktlintFormat
```

Первая команда проверяет Kotlin-код, вторая исправляет форматирование и изменяет файлы.
Правила находятся в [.editorconfig](.editorconfig); ktlint подключён ко всем Gradle-подпроектам.

Для `commonTest` модуля `shared` настроена зависимость `kotlin-test`, но исходников тестов в репозитории пока нет.
Общие тесты можно добавлять в `shared/src/commonTest/kotlin` и запускать на JVM:

```bash
./gradlew :shared:jvmTest
```

Для тестов в других модулях сначала добавьте соответствующие зависимости в их `build.gradle.kts`.
Успешное выполнение задачи без тестовых исходников не означает наличие тестового покрытия.

## GitHub Actions

Текущие workflow-файлы содержат настройки исходного проекта и требуют изменений при клонировании.

| Workflow | Триггер | Что описано |
| --- | --- | --- |
| [pr-checks.yml](.github/workflows/pr-checks.yml) | PR в `main` или `master` | Сборки Android, iOS framework, JS, JVM и ktlint |
| [build-and-deploy.yml](.github/workflows/build-and-deploy.yml) | Push в `main` | Release APK и коммит артефакта в `alad1nks/alad1nks.github.io` |
| [release.yml](.github/workflows/release.yml) | Push в `release/*` | Release AAB и коммит артефакта в тот же внешний репозиторий |

Перед включением CI в своём проекте:

1. Замените или удалите `post-build`, публикующий файлы в `alad1nks/alad1nks.github.io`.
   Обновите имя и email автора коммитов, пути `startkmp` и имена артефактов.
2. Удалите шаги декодирования Firebase, если интеграция не нужна. Firebase-зависимости и плагин Google Services
   в текущих Gradle-файлах не подключены. JS-шаг обращается к отсутствующему
   `composeApp/src/jsMain/resources/firebaseConfig.js`.
3. Согласуйте Java в CI с критериями daemon JVM: workflows устанавливают Temurin 17, проект задаёт Amazon 21.
4. Настройте Android release signing и реальные пути выходных файлов. Сейчас workflows декодируют keystore
   и передают `SIGNING_*`, но Gradle их не использует; ожидаемый подписанный APK таким способом не создаётся.
5. Уточните задачи по модулям: для JVM-приложения используйте `:desktopApp:jar` или задачу упаковки,
   для общего JVM-кода — `:shared:jvmJar`. Текущий вызов `jvmJar` не проверяет Desktop-точку входа.
   Сборка `linkReleaseFrameworkIosArm64` проверяет framework, а не SwiftUI-приложение целиком.

В существующих workflows используются секреты `FIREBASE_CONFIG_ANDROID`, `FIREBASE_CONFIG_IOS`,
`FIREBASE_CONFIG_JS`, `KEYSTORE_BASE64`, `KEY_ALIAS`, `KEY_PASSWORD`, `KEYSTORE_PASSWORD` и `ACCESS_TOKEN`.
Они относятся к этим сценариям CI; для локальной debug-сборки шаблона они не нужны.
Если сохраняете release-сценарии, добавьте keystore и приватные конфиги в `.gitignore`
до создания таких файлов локально — сейчас специальных правил для них нет.
