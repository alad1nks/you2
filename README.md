# you2

you2 is an application for couples in a relationship to keep their shared wishes and plans in one place.
Each partner registers an account, then they add each other to form a couple.
Once connected, both partners can add wishes with descriptions to a shared list.

The list can include adventures, presents, shared plans, movies, TV series, and other things
the couple would like to experience or enjoy together.

The current implementation is an interactive paired-user prototype in Kotlin Multiplatform
for Android, iOS, Desktop (JVM), and Web (Kotlin/JS), with a shared
Compose Multiplatform interface. A paired demo starts with Alex and Mia, 12 active wishes and four memories.
Registration, couple linking, networking and persistent wish storage are not yet implemented.

## Design

[Open the you2 mockup in Figma](https://www.figma.com/design/4NzqzLDkOMaaGQGKYhEeuT/you2-%E2%80%94-Our-wishes?m=auto&t=vTKmz2XhR6KV8954-1).

The file groups screens by user flow: guest experience, Us and pairing, Wishes, adding a wish,
wish details, and Memories. It also includes the color palette, typography, form elements,
components, and category illustrations. These mockups describe the intended interface.

## Included features

- Shared UI built with Compose Multiplatform and Material 3, with separate entry points for four platforms.
- Separate layers for UI, use cases, the repository, models, and settings storage.
- Koin dependency injection, with ViewModel and Flow for UI state.
- Theme and language persistence using DataStore on Android/iOS/Desktop and `localStorage` in the browser.
- Light and dark color schemes, with infrastructure for system, Russian, English, and Kazakh language selection.
- A Gradle version catalog and ktlint for Kotlin code.

The app provides Wishes, Memories and Us, category filters, validated add/edit forms with dates and demo images,
wish details, completion, memory editing, restoration and confirmed deletion.
Us shows shared counters, profile editing, theme and language settings. English, Russian and Kazakh UI resources,
Roboto and bundled Figma illustrations are included. Sign out is unavailable in this paired demo.
A Koin singleton repository exposes the demo through StateFlow and keeps all screens in sync.
The injected demo date is October 28, 2026; restarting resets wishes, while theme/language preferences persist.
JVM UI tests cover the complete wish lifecycle, narrow screens, enlarged text and screenshot regressions.
Real mobile keyboard/inset/back-gesture behavior and iOS still need device/macOS validation.
The GitHub Actions workflows need to be adapted before use in a new repository.

## Environment

Dependencies were checked on October 4, 2026 against Google Maven, Maven Central, Gradle Plugin Portal,
and official releases. The selected versions were the latest stable releases; alpha, beta, and RC releases were excluded.
The dependency version source of truth is
[gradle/libs.versions.toml](gradle/libs.versions.toml).

| Component | Project value |
| --- | --- |
| Kotlin (including Compose Compiler and kotlin-test) | 2.4.20 |
| Compose Multiplatform | 1.12.1 |
| Material 3 | 1.9.0 |
| Android Gradle Plugin | 9.4.1 |
| Gradle Wrapper | 9.8.0 |
| Gradle daemon JVM | Amazon JDK 21 |
| Android SDK | compile/target 37, min 28 |
| iOS deployment target | 18.2 |
| Koin / DataStore / Coroutines | 4.2.2 / 1.2.1 / 1.11.0 |
| AndroidX Activity | 1.13.0 |
| JetBrains Lifecycle | 2.11.0 |
| Kotlin Wrappers (kotlin-browser) | 2026.9.3 |
| ktlint Gradle plugin / ktlint CLI | 14.2.0 / 1.8.0 |
| Foojay resolver convention | 1.0.0 |

Material 3 is versioned separately from Compose Multiplatform: `1.9.0` was the latest stable release of the
[Material 3 artifact](https://repo.maven.apache.org/maven2/org/jetbrains/compose/material3/material3/maven-metadata.xml)
at the time of the check. Koin module versions are aligned through the BOM. The ktlint engine version is set explicitly:
updating the Gradle plugin alone does not guarantee that its CLI is updated.

To check for updates, see the [Kotlin releases](https://kotlinlang.org/docs/releases.html),
[Compose Multiplatform releases](https://github.com/JetBrains/compose-multiplatform/releases),
[AGP releases](https://developer.android.com/build/releases/about-agp), and
[current Gradle release](https://services.gradle.org/versions/current).
According to the [Kotlin compatibility table](https://kotlinlang.org/docs/gradle-configure-project.html),
Kotlin 2.4.20 is fully tested with Gradle up to 9.7.0 and AGP up to 9.3.1.
This project uses newer stable versions, Gradle 9.8.0 and AGP 9.4.1. This combination is outside the stated
fully supported range, so verify builds for all targets when updating dependencies.

Git and an installed JDK are required. Gradle daemon JVM criteria are defined in
[gradle/gradle-daemon-jvm.properties](gradle/gradle-daemon-jvm.properties): version 21, vendor Amazon.
If a matching JVM is unavailable, Gradle can download it from the URLs in that file; the first build requires network access.
Android's `jvmTarget = JVM_11` setting controls bytecode compatibility, not the JVM version used to run Gradle.

For Android, install the Android SDK with API 37 and configure its location through Android Studio
or `sdk.dir` in a local `local.properties` file (excluded from Git).
iOS requires macOS and Xcode with an SDK that supports the project's deployment target.
The configured targets are `iosArm64` and `iosSimulatorArm64`; there is no Intel simulator target (`iosX64`).
Web requires a browser. Gradle is configured for Kotlin/JS; there is no Wasm target.

## Creating your own project

1. Clone the template into a directory for your new application:

   ```bash
   git clone https://github.com/alad1nks/you2.git my-app
   cd my-app
   ```

2. Create an empty repository and replace `origin` with your URL:

   ```bash
   git remote set-url origin https://github.com/YOUR_ACCOUNT/YOUR_REPOSITORY.git
   ```

   This preserves the template's history. Before the first push, adapt or remove the workflows
   described in the GitHub Actions section.

3. Rename the application using the table below. For example, use `myapp` as the project name
   and `com.example.myapp` as the base package. IDE refactoring can update Kotlin package declarations,
   imports, and source directories together.

| What to replace | Location |
| --- | --- |
| `rootProject.name = "you2"` | [settings.gradle.kts](settings.gradle.kts) |
| `com.alad1nks.you2`: packages, imports, and matching directories | Kotlin sources across all modules |
| `namespace` in all Android/KMP modules; the application's `applicationId`, `versionCode`, and `versionName` | Module `build.gradle.kts` files, including [androidApp/build.gradle.kts](androidApp/build.gradle.kts) |
| Android app name and icons | [strings.xml](androidApp/src/main/res/values/strings.xml), `mipmap*` and `drawable*` resources in `androidApp/src/main/res` |
| `PRODUCT_NAME`, `PRODUCT_BUNDLE_IDENTIFIER`, `TEAM_ID`, and versions | [Config.xcconfig](iosApp/Configuration/Config.xcconfig) |
| References to `you2.app` and the iOS icon | [project.pbxproj](iosApp/iosApp.xcodeproj/project.pbxproj), [AppIcon.appiconset](iosApp/iosApp/Assets.xcassets/AppIcon.appiconset) |
| Desktop `mainClass`, `packageName`, and `packageVersion` | [desktopApp/build.gradle.kts](desktopApp/build.gradle.kts) |
| Desktop window title | [main.kt](desktopApp/src/main/kotlin/com/alad1nks/you2/main.kt) |
| Web page title | [index.html](webApp/src/webMain/resources/index.html) |
| Settings filename `you2.preferences_pb` | [StorageDataStoreFileName.kt](core/storage/datastore/src/commonMain/kotlin/com/alad1nks/you2/core/storage/datastore/StorageDataStoreFileName.kt) |
| Publishing repository, artifact paths and names, and commit author | [.github/workflows](.github/workflows) |

The current iOS bundle identifier is `com.alad1nks.you2.you2$(TEAM_ID)`;
set your own identifier explicitly. `TEAM_ID` is also used for application signing.

Product resources use the explicit package `com.alad1nks.you2.resources.generated`.
After changing the package name in `resources/build.gradle.kts`, update the resource imports.
Do not edit generated files in `build/`.

You can keep the module names `shared`, `androidApp`, `desktopApp`, and `webApp`, and the framework name `Shared`.
If you rename `Shared`, update both `baseName` in `shared/build.gradle.kts`
and `import Shared` in `iosApp/iosApp/ContentView.swift`. If you rename `webApp`, also check
the `webApp.js` script name in the HTML. If you rename `shared`, update the Gradle command in the Xcode build phase.

4. Check for remaining references to the original project (this command uses ripgrep):

   ```bash
   rg -n --hidden -g '!.git' -g '!**/build/**' -g '!.gradle' -g '!.kotlin' \
     'you2|alad1nks' .
   ```

5. Sync the project in your IDE, build the required platforms, and replace the demo screen with your own.
   Update this README for your application.

## Running and building

Run commands from the repository root. On Windows, use `gradlew.bat` instead of `./gradlew`.
If the executable bit is missing on Unix, run `chmod +x gradlew`.
The commands below match the configured modules and plugins; running them requires the appropriate SDK/JDK.

### Android

```bash
./gradlew :androidApp:assembleDebug
```

APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.
To run the application, select the `androidApp` module and a device/emulator in Android Studio, or install the build
on a connected device with `./gradlew :androidApp:installDebug`.

There is currently no release signing configuration in `androidApp/build.gradle.kts`.
Configure signing before publishing the application; the build does not currently read the CI `SIGNING_*` variables.

### Desktop

```bash
./gradlew :desktopApp:run
```

The distribution settings include DMG, MSI, and DEB.
Build a package for the current OS with `./gradlew :desktopApp:packageDistributionForCurrentOS`;
outputs are placed in `desktopApp/build/compose/binaries`.
Build each format on its corresponding OS.
The Compose Hot Reload plugin is not enabled in the current configuration, so `hotRun` is not included in these instructions.

### Web

```bash
./gradlew :webApp:jsBrowserDevelopmentRun
```

For a production build:

```bash
./gradlew :webApp:jsBrowserDistribution
```

Static build files are placed in `webApp/build/dist/js/productionExecutable`.
The entry point and HTML are in `webApp/src/webMain`.

### iOS

Open [iosApp/iosApp.xcodeproj](iosApp/iosApp.xcodeproj) in Xcode, select the `iosApp` scheme,
and choose a device or ARM64 simulator. To run on a device, configure the development team and bundle identifier
in `Config.xcconfig`, then run the application from Xcode.

The `Compile Kotlin Framework` build phase calls `:shared:embedAndSignAppleFrameworkForXcode`.
The shared UI is exported in the static `Shared` framework, which is used by SwiftUI.
To check the framework build separately on macOS:

```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
./gradlew :shared:linkReleaseFrameworkIosArm64
```

These commands build the framework. Build the complete iOS application and distribution archive in Xcode.

## Project structure

| Module / directory | Purpose |
| --- | --- |
| [androidApp](androidApp) | Android application, `MainActivity`, manifest, and launcher resources |
| [iosApp](iosApp) | Xcode project and SwiftUI wrapper around `MainViewController` |
| [desktopApp](desktopApp) | JVM entry point, window, and installer settings |
| [webApp](webApp) | Browser entry point using `ComposeViewport`, HTML, and CSS |
| [shared](shared) | Paired UI scaffold, navigation, `MainViewModel`, DI setup, and platform adapters |
| [core/model](core/model) | Shared models, currently `Language` |
| [core/domain](core/domain) | `GetDarkTheme`, `SetDarkTheme`, `GetLanguage`, `SetLanguage`, and their implementations |
| [core/data](core/data) | `SettingsRepository`, which converts the stored language code into a model |
| [core/storage/common](core/storage/common) | `AppPreferences` and `Storage` contracts, and the shared `StorageImpl` implementation |
| [core/storage/datastore](core/storage/datastore) | DataStore Preferences for Android, iOS, and JVM |
| [core/storage/js](core/storage/js) | `AppPreferences` implementation using browser `localStorage` |
| [core/designsystem](core/designsystem) | `AppTheme` and Material 3 color schemes |
| [resources](resources) | Shared string resources and the `AppResource` placeholder |

Settings access flow:

```text
App → MainViewModel → GetDarkTheme / GetLanguage
                           ↓
                    SettingsRepository
                           ↓
                    Storage → AppPreferences
                                  ├─ DataStore (Android / iOS / JVM)
                                  └─ localStorage (JS)
```

`core/domain` depends on `core/data` and `core/model`; `core/data` depends on `core/model` and `core/storage/common`.
The concrete storage implementation is connected through the platform dependencies of `shared`.

Shared code lives in `src/commonMain`; platform code lives in `androidMain`, `iosMain`, `jvmMain`, and `jsMain`.
The entry point is in `webMain` for `webApp` and in `main` for `desktopApp`.
Platform differences use `expect`/`actual`: `getPlatform`, `LocalAppLocale`,
`PlatformModules`, and the DataStore file path.

`App()` creates a `KoinApplication` and loads `CommonModules + PlatformModules`.
Shared modules register the ViewModel, use cases, repository, and `Storage`;
platform modules register the `AppPreferences` implementation.

### Theme, language, and settings persistence

`MainViewModel` exposes theme and language flows. `App` applies the theme through `AppTheme` and the language through `LocalAppLocale`.
The `SetDarkTheme` and `SetLanguage` use cases are already registered in Koin, but UI to invoke them still needs to be added.

If no theme is saved, `GetDarkThemeImpl` returns `false`: the light theme is selected after settings are read.
The system theme is only used as the initial value when the UI subscribes.
An unknown or missing language code maps to `Language.SYSTEM`.

| Platform | Settings location |
| --- | --- |
| Android | `filesDir/you2.preferences_pb` |
| iOS | The application's Documents directory, `you2.preferences_pb` |
| Desktop | `java.io.tmpdir/you2.preferences_pb` |
| Web | `localStorage` for the current origin, using the keys `dark_theme` and `language` |

For Desktop, choose a persistent user data directory before releasing the application:
the temporary directory may be cleared, and template copies that retain the same filename share the same path.
On Web, applications on the same origin use the same keys; add an application-specific prefix if needed.

### Extending the template

- Add shared screens and their ViewModels to `shared`; place shared visual components and themes in `core/designsystem`.
- Place models in `core/model`, use cases in `core/domain`, and data access in `core/data`.
- For a new setting, extend `Storage`/`StorageImpl`, the repository, and the use cases.
  If strings and Booleans are sufficient, the existing `AppPreferences` already provides the required operations.
- Register dependencies in the DI module for the relevant layer. Connect new Koin modules through
  `CommonModules` or the platform `PlatformModules`; add new Gradle modules through `settings.gradle.kts`.
- Add strings to `resources/src/commonMain/composeResources/values/strings.xml`
  and translations to `values-ru` and `values-kk`. To add a language, extend `Language` and add a resource directory.
  The demo image is currently stored separately in `shared/src/commonMain/composeResources`.

## Checks

```bash
./gradlew ktlintCheck
./gradlew ktlintFormat
```

The first command checks Kotlin code; the second fixes formatting and modifies files.
Rules are defined in [.editorconfig](.editorconfig); ktlint is enabled for all Gradle subprojects.
The CLI version is set by `ktlint-cli` in the version catalog, independently of the `ktlint` Gradle plugin version.

The `shared` module's `commonTest` source set has a `kotlin-test` dependency.
Add shared tests to `shared/src/commonTest/kotlin` and run them on the JVM:

```bash
./gradlew :shared:jvmTest
```

For tests in other modules, first add the appropriate dependencies to their `build.gradle.kts` files.
A successful task with no test sources does not indicate test coverage.

### Screenshot tests

Shared Compose UI screenshots use [Roborazzi](https://github.com/takahirom/roborazzi) on the JVM,
without an emulator. `AppScreenshotTest` covers the paired scaffold in light and dark themes at 412 x 917,
a narrow 320 x 917 viewport and all six category illustrations. It renders production `AppContent`
with fixed shared demo data, density and font scale of 1. It also exercises tab switching,
filter retention, form dismissal and returning from details without losing the list position.
Settings storage and DI are bypassed. `PairedFoundationTest` checks date boundaries, validation,
repository transitions and navigation state.
These tests cover shared layout and styling; they do not check native Android/iOS rendering.

Run on Windows with JDK 21, matching the `windows-2025` PR runner. Desktop font rendering can differ
across operating systems; do not regenerate these reference images on macOS or Linux.

```powershell
# Compare against committed images; missing or changed references fail the build.
./gradlew.bat :shared:verifyRoborazziJvm

# After an intentional UI change, regenerate and visually review the PNG diff before committing.
./gradlew.bat :shared:recordRoborazziJvm
./gradlew.bat :shared:verifyRoborazziJvm
```

Reference PNGs live in `shared/src/jvmTest/screenshots` and must be committed with UI changes.
Verification never updates them. A plain `:shared:jvmTest` runs the interactions but does not verify pixels;
use `verifyRoborazziJvm` for visual regression checking. Add new scenarios under `shared/src/jvmTest/kotlin`
with unique screenshot names in the same reference directory.

PRs run the **Screenshot tests** job independently of platform builds. Its `screenshot-test-reports`
artifact contains Roborazzi comparison images/reports and JVM test results, including on failure.
Local reports are in `shared/build/reports/roborazzi` and `shared/build/reports/tests/jvmTest`;
comparison images are in `shared/build/outputs/roborazzi`.

## GitHub Actions

The current workflows contain settings from the original project and require changes when cloning.
They use Amazon Corretto 21, matching the Gradle daemon criteria, and these Actions versions:
`actions/checkout@v7.0.1`, `actions/setup-java@v6.0.1`,
`actions/upload-artifact@v7.0.1`, `actions/download-artifact@v8.0.1`.

The project does not use Firebase: its SDK, configuration files, and build secrets are not required.
The JS check runs `:webApp:jsBrowserProductionWebpack` without preparing external configuration files.

| Workflow | Trigger | Defined behavior |
| --- | --- | --- |
| [pr-checks.yml](.github/workflows/pr-checks.yml) | PR targeting `main` or `master` | Android, iOS framework, JS, and JVM builds, plus ktlint and screenshot verification |
| [build-and-deploy.yml](.github/workflows/build-and-deploy.yml) | Push to `main` | Release APK and an artifact commit to `alad1nks/alad1nks.github.io` |
| [release.yml](.github/workflows/release.yml) | Push to `release/*` | Release AAB and an artifact commit to the same external repository |

Before enabling CI in your project:

1. Replace or remove `post-build`, which publishes files to `alad1nks/alad1nks.github.io`.
   Update the commit author name and email, `you2` paths, and artifact names.
2. When changing the project JVM, update Java in CI and the criteria in `gradle-daemon-jvm.properties` together.
3. Configure Android release signing and the actual output file paths. The workflows currently decode a keystore
   and pass `SIGNING_*` variables, but Gradle does not use them, so this does not produce the expected signed APK.
4. Use module-specific tasks: for the JVM application, use `:desktopApp:jar` or a packaging task;
   for shared JVM code, use `:shared:jvmJar`. The current `jvmJar` invocation does not check the Desktop entry point.
   Building `linkReleaseFrameworkIosArm64` checks the framework, not the complete SwiftUI application.

The existing workflows use the secrets `KEYSTORE_BASE64`, `KEY_ALIAS`, `KEY_PASSWORD`,
`KEYSTORE_PASSWORD`, and `ACCESS_TOKEN`.
These belong to those CI workflows; they are not needed for a local debug build of the template.
If you keep the release workflows, add the keystore and private configuration files to `.gitignore`
before creating them locally; there are currently no specific ignore rules for them.
