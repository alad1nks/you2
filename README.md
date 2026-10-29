# you2

you2 helps couples keep wishes, shared plans, and memories in one place. It is built with
Kotlin Multiplatform and Compose Multiplatform for Android, iOS, Desktop (JVM), and Web (Kotlin/JS).

## Current application

The app currently opens a paired demo for Alex and Mia with 12 active wishes and four memories.
It supports the following flows:

- **Wishes:** browse and filter six categories (adventures, presents, plans, movies, TV series,
  and other), view details, and create or edit wishes with descriptions, dates, covers, and attachments.
- **Dates:** leave a wish undated, set a deadline, or choose a date range. Badges show upcoming,
  current, and overdue dates; dates do not automatically complete wishes.
- **Memories:** complete a wish, browse completed wishes grouped by completion month, edit a memory,
  restore a wish to the active list, or delete it after confirmation.
- **Us:** see the couple and shared counters, edit your display name, switch between light and dark
  themes, and select English, Russian, Kazakh, or the system language.

Wish titles are required and limited to 80 characters; descriptions and memory text are limited to
500 characters. The image picker uses bundled category illustrations as demo images.

The paired space is held in memory. Wishes, memories, and profile edits reset when the app restarts
(or the browser page reloads). Theme and language preferences use platform storage. All date-sensitive
behavior uses the fixed demo date **October 28, 2026**, supplied by `DemoDateSource`.

Account registration, authentication, couple linking, synchronization between devices, durable wish
storage, and picking personal photos are not implemented. Sign out is disabled with an explanation
in the UI. Demo wish content stays in English when the interface language changes.

## Design and resources

The [you2 Figma design](https://www.figma.com/design/4NzqzLDkOMaaGQGKYhEeuT/you2-%E2%80%94-Our-wishes)
is the product design reference. The implemented app currently covers the paired experience.
Shared strings, Roboto fonts, and category illustrations live in [resources](resources).
See [the asset guide](resources/ASSETS.md) for their locations, provenance, and usage.

## Development setup

Clone the application and open the repository in your IDE:

```bash
git clone https://github.com/alad1nks/you2.git
cd you2
```

Install Git and a JDK to launch the Gradle wrapper. The Gradle daemon is configured to use
Amazon JDK 21 in [gradle/gradle-daemon-jvm.properties](gradle/gradle-daemon-jvm.properties).
Gradle can download the matching JVM; initial builds require network access for toolchains and dependencies.

Platform requirements:

- **Android:** Android Studio and Android SDK API 37. Configure the SDK through Android Studio or
  `sdk.dir` in an untracked `local.properties`. The minimum supported Android API is 28.
- **Desktop:** a desktop environment and the configured JDK.
- **Web:** a browser. The project targets Kotlin/JS.
- **iOS:** macOS and Xcode with support for the configured iOS 18.2 deployment target.
  Kotlin targets are `iosArm64` and `iosSimulatorArm64`.

Dependency versions are defined in [gradle/libs.versions.toml](gradle/libs.versions.toml),
with Gradle 9.8.0 pinned in [the wrapper configuration](gradle/wrapper/gradle-wrapper.properties).
The project currently pins Kotlin 2.4.20, Compose Multiplatform 1.12.1, Material 3 1.9.0,
and Android Gradle Plugin 9.4.1. These are repository settings, not a claim about the latest releases.

## Running and building

Run commands from the repository root. In PowerShell, replace `./gradlew` with `.\gradlew.bat`.
On Unix, run `chmod +x gradlew` if needed.

### Android

```bash
./gradlew :androidApp:assembleDebug
./gradlew :androidApp:installDebug
```

The APK is written to `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.
Installation requires a connected device or emulator. You can also run the `androidApp` configuration
from Android Studio. The application ID is `com.alad1nks.you2`.

### Desktop

```bash
./gradlew :desktopApp:run
```

Build an installer for the current OS with `:desktopApp:packageDistributionForCurrentOS`.
DMG, MSI, and DEB formats are configured; build each on its corresponding OS.
Outputs are under `desktopApp/build/compose/binaries`.

### Web

```bash
./gradlew :webApp:jsBrowserDevelopmentRun
```

Build static production files with `:webApp:jsBrowserDistribution`.
Outputs are in `webApp/build/dist/js/productionExecutable`.
The browser entry point, HTML, and CSS are under `webApp/src/webMain`.

### iOS

Open [iosApp/iosApp.xcodeproj](iosApp/iosApp.xcodeproj) in Xcode and select the `iosApp` scheme
and an ARM64 simulator or device. For a device build, configure the team and bundle identifier in
[iosApp/Configuration/Config.xcconfig](iosApp/Configuration/Config.xcconfig).

Xcode's `Compile Kotlin Framework` phase runs `:shared:embedAndSignAppleFrameworkForXcode`.
SwiftUI hosts the shared Compose UI through `MainViewController` in the static `Shared` framework.
To build just the framework on macOS:

```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
./gradlew :shared:linkReleaseFrameworkIosArm64
```

Build and archive the full iOS application in Xcode.

## Code organization

| Module / directory | Responsibility |
| --- | --- |
| [androidApp](androidApp) | Android activity, manifest, launcher resources, and application build |
| [iosApp](iosApp) | Xcode project and SwiftUI host |
| [desktopApp](desktopApp) | JVM entry point, window, and installer configuration |
| [webApp](webApp) | Browser entry point, HTML, and CSS |
| [shared](shared) | App composition, paired navigation, Wishes/Memories/Us screens, forms, `MainViewModel`, and platform adapters |
| [core/model](core/model) | Couple, user, wish, completion, category, date, image, and language models; draft validation and date status |
| [core/data](core/data) | `WishesRepository`, its in-memory implementation, demo data/date source, and `SettingsRepository` |
| [core/domain](core/domain) | Theme and language read/write use cases |
| [core/storage/common](core/storage/common) | `Storage`, `StorageImpl`, and `AppPreferences` contracts |
| [core/storage/datastore](core/storage/datastore) | DataStore Preferences for Android, iOS, and JVM |
| [core/storage/js](core/storage/js) | Browser `localStorage` preferences |
| [core/designsystem](core/designsystem) | Shared components, Material icons, color schemes, typography, spacing, and shape tokens |
| [resources](resources) | Shared localized strings, fonts, and category images |
| [design/icons](design/icons) | Original SVG design references |

`App()` creates Koin with `CommonModules + PlatformModules`. `MainViewModel` observes the singleton
`WishesRepository.space` StateFlow and sends wish, memory, and profile mutations directly to that
repository. Screens share the same `PairedSpace`, so lists and counters update together.
`PairedNavigation` holds tab, filter, and overlay state.

Settings follow a separate path: `MainViewModel` calls the theme/language use cases in `core/domain`,
which delegate to `SettingsRepository`, then `Storage` and the platform `AppPreferences` implementation.
`App` applies those flows through `AppTheme` and `LocalAppLocale`.

Shared Kotlin code lives in `src/commonMain`, with platform adapters in `androidMain`, `iosMain`,
`jvmMain`, and `jsMain`. Platform entry points use `webMain` in `webApp` and `main` in `desktopApp`.

### Settings persistence

Us exposes both the theme and language controls. An unset theme resolves to light after preferences
load; the system theme is only the initial subscription value. Missing or unknown language codes
resolve to `Language.SYSTEM`.

| Platform | Settings location |
| --- | --- |
| Android | `filesDir/you2.preferences_pb` |
| iOS | App Documents directory, `you2.preferences_pb` |
| Desktop | `java.io.tmpdir/you2.preferences_pb` |
| Web | Current origin's `localStorage`, keys `dark_theme` and `language` |

Desktop settings currently use a temporary directory and may be cleared by the OS.
A durable user data directory is still needed for release. Browser settings are scoped to the origin.

### Making changes

Keep product screens and their state in `shared`, reusable visuals in `core/designsystem`, models in
`core/model`, and data access in `core/data`. Register dependencies in the owning module's Koin module.
The existing `core/domain` use cases cover settings; wish operations currently go directly through
`WishesRepository`.

Add interface copy to `resources/src/commonMain/composeResources/values/strings.xml` and update
`values-ru` and `values-kk` alongside it. Use shared design tokens and `AppIcons` for new UI.
Changes to demo data or the fixed date can affect many screenshot references.

## Validation

```bash
./gradlew ktlintCheck
./gradlew :shared:jvmTest
```

`ktlintCheck` checks Kotlin formatting using [.editorconfig](.editorconfig).
`ktlintFormat` applies formatting changes. `:shared:jvmTest` runs the common tests and JVM tests:

- Common tests cover draft validation, date boundaries, repository transitions, navigation state,
  and grouping memories by completion date.
- JVM Compose tests cover forms, wish details, memories, profile/settings controls, and complete
  wish lifecycle journeys in English, Russian, and Kazakh, including narrow screens and enlarged text.
- `SettingsTest` checks repository/domain recreation against a shared in-memory storage fake;
  it does not validate native persistence on a device.

### Screenshot regression tests

Roborazzi captures the production shared UI on the JVM without an emulator. References are committed
under `shared/src/jvmTest/screenshots`. Generate and verify them on **Windows with JDK 21**, matching
the `windows-2025` CI runner; font rendering can differ on other operating systems.

```powershell
# Compare with committed references; missing or changed images fail verification.
.\gradlew.bat :shared:verifyRoborazziJvm

# For intentional visual changes, record and review the changed PNGs before committing.
.\gradlew.bat :shared:recordRoborazziJvm
.\gradlew.bat :shared:verifyRoborazziJvm
```

A plain `:shared:jvmTest` runs interactions without pixel verification. Screenshot scenarios cover
light/dark themes, category covers, empty and long content, dialogs, forms, details, and complete journeys.
Local reports are in `shared/build/reports/roborazzi` and `shared/build/reports/tests/jvmTest`;
comparison images are under `shared/build/outputs/roborazzi`.

These JVM checks do not establish native Android/iOS rendering or keyboard, inset, and back-gesture
behavior. Those need device/emulator validation, and iOS builds require macOS/Xcode.

## CI and distribution

| Workflow | Trigger | Configured behavior |
| --- | --- | --- |
| [pr-checks.yml](.github/workflows/pr-checks.yml) | PRs targeting `main` or `master` | Android debug build, iOS release framework, production JS webpack build, shared JVM jars, ktlint, and Windows screenshot verification |
| [build-and-deploy.yml](.github/workflows/build-and-deploy.yml) | Push to `main` | Tests and release APK build, followed by APK publication under `you2` in `alad1nks/alad1nks.github.io` |
| [release.yml](.github/workflows/release.yml) | Push to `release/*` | Tests and release AAB build, followed by AAB publication to the same repository |

CI uses Amazon Corretto 21. The screenshot job uploads `screenshot-test-reports`, including on failure.
The PR JVM command is `jvmJar`, which does not build the Desktop application entry point;
use `:desktopApp:jar` or run/package Desktop when changing that target. The iOS job builds the Kotlin
framework, not the complete SwiftUI app.

Release publication is configured but still has a signing gap: the workflows decode a keystore and
pass `SIGNING_*` variables, while `androidApp/build.gradle.kts` has no release signing configuration
and does not consume those variables. The APK workflow also expects `androidApp-release.apk`.
Signing and the resulting artifact filename must be aligned before relying on release publication.

The publishing workflows reference `KEYSTORE_BASE64`, `KEY_ALIAS`, `KEY_PASSWORD`, `KEYSTORE_PASSWORD`,
and `ACCESS_TOKEN`. These are not required for local debug builds or the PR checks.
