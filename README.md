# you2

you2 is an application for couples to keep wishes, shared plans, and memories in one place.
From little surprises to big adventures, it gives partners a shared space for the things they want
to do, give, watch, and enjoy together.

The application is under active development, built with Kotlin Multiplatform and Compose Multiplatform
for Android, iOS, Desktop (JVM), and Web (Kotlin/JS).

## Product experience

The product journey starts with exploring you2, creating an account or logging in, and connecting
with a partner through an invitation. Each person has their own account; together they build a wish
list and turn completed wishes into memories. The Figma design defines guest, unpaired, and paired
states around three main areas:

- **Wishes:** browse and filter six categories (adventures, presents, plans, movies, TV series,
  and other), view details, and create or edit wishes with descriptions, dates, covers, and attachments.
  Leave a wish undated, set a deadline, or choose a date range. Badges show upcoming,
  current, and overdue dates; dates do not automatically complete wishes.
- **Memories:** complete a wish, browse completed wishes grouped by completion month, edit a memory,
  restore a wish to the active list, or delete it after confirmation.
- **Us:** see the couple and shared counters, edit your display name, switch between light and dark
  themes, and select English, Russian, Kazakh, or the system language.

## Development status

The shared Wishes, Memories, and Us screens are implemented, including wish creation/editing,
date validation, completion and restoration, memory editing, profile-name editing, and theme/language
controls. Wish titles are required and limited to 80 characters; descriptions and memory text are
limited to 500 characters.

Android email/password registration, Google sign-in, password reset, session restoration, and sign-out use Firebase Authentication.
Android and iOS use Cloud Firestore for profiles, partner invitations, shared wishes, and memories. After sign-in,
users return to the tab they were viewing before authentication. For unpaired users, Us shows
**Your space**, with profile, appearance, and language settings.
All three tabs remain available; Wishes and Memories explain how to connect. **Invite your partner**
opens a copyable-code dialog, and **I have an invite code** opens the join dialog. After pairing,
**View our wishes** opens the shared list.
iOS supports Firebase email/password registration, sign-in, password reset, session restoration, and sign-out.
Its profiles, partner invitations, wishes, and memories sync through Firestore using the same schema as Android.
iOS social sign-in remains unavailable. Desktop still uses the demo flow with sign-out disabled.
Web supports Firebase email/password and Google authentication, session restoration, password reset,
and sign-out. Profiles, partner invitations, wishes, and memories sync through the same Firestore
database and schema as Android and iOS.
Android and iOS support private photo uploads for wish covers and attachments.

Tap a cover or attachment in wish or memory details to open the full-screen image viewer.
Images fit without cropping. Galleries support swiping, selectable thumbnails, and left/right arrow keys;
Close, Escape, or system Back returns to the same details position. Single images hide the gallery
counter, caption, and thumbnails, following the Figma viewer designs.
Pinch to zoom up to 5×, or double-tap/double-click to animate between fitted size and 3×.
Drag to pan while zoomed; return to fitted size to swipe between images. A single tap/click toggles
the close button and gallery controls. Switching images resets the zoom.
The close button has a transparent background, with a top shadow behind visible controls.
On Android, hiding the controls also hides the status and navigation bars; showing the controls
restores them. An edge swipe can temporarily reveal the system bars. On iOS, hiding controls hides
the status bar and requests automatic hiding of the home indicator; closing the viewer restores both.

For Desktop development and UI validation, the demo screens use seeded data for Alex and Mia:
12 active wishes and four memories. `InMemoryWishesRepository` holds this data, so wish, memory, and
profile changes reset when the demo restarts. Android, iOS, and Web supply the current local date; `DemoDateSource` supplies the fixed date
**October 28, 2026**. Desktop image pickers use bundled illustrations; Web supports photo selection. Theme and language
preferences already use platform storage; seeded wish content stays English when the UI language changes.

## Design and resources

The [you2 Figma design](https://www.figma.com/design/4NzqzLDkOMaaGQGKYhEeuT/you2-%E2%80%94-Our-wishes)
is the product design reference. It covers guest previews, Us and pairing, wish lists and timing,
wish forms and images, wish details and actions, and completed wishes and memories. Its design system
defines the shared colors, typography, forms, components, spacing, and category illustrations.
Shared strings, Roboto fonts, and category illustrations live in [resources](resources).
See [the asset guide](resources/ASSETS.md) for their locations, provenance, and usage.

Tablet windows follow the portrait (834 ? 1194) and landscape (1194 ? 834) Figma layouts.
At 600 dp, pages gain tablet spacing; wishes use two columns from 760 dp and three from 1000 dp.
At 1000 dp, wish forms, details, and Us use two columns, memory months appear side by side,
and guest previews sit beside authentication actions. Bottom navigation stays centered in both
orientations. Narrow split-screen windows use the compact layout. Authentication forms retain
their existing 440 dp content width, and image viewing remains full screen.

At 1200 dp and above, Desktop and Web use a persistent 104 dp navigation rail. Wishes
keep three columns and place Add a wish beside the page heading. Forms and details keep
the rail visible, with their primary action aligned to the bottom right. Guest previews,
Memories, and Us use the wide two-column workspace. Resizing below 1200 dp restores
bottom navigation; authentication and image viewing continue to use the full window.

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
- **iOS:** macOS and Xcode 26.2 or newer (Swift 6.2.3 for the Firebase package), with support for the configured iOS 18.2 deployment target.
  Kotlin targets are `iosArm64` and `iosSimulatorArm64`.

Dependency versions are defined in [gradle/libs.versions.toml](gradle/libs.versions.toml),
with Gradle 9.8.2 pinned in [the wrapper configuration](gradle/wrapper/gradle-wrapper.properties).
The project currently pins Kotlin 2.4.21, Compose Multiplatform 1.12.2, Material 3 1.9.0,
and Android Gradle Plugin 9.5.1. These are repository settings, not a claim about the latest releases.

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

#### Release signing

Place your release/upload keystore at `release-key.jks` in the repository root.
Set these environment variables before running Gradle:

```properties
SIGNING_STORE_PASSWORD=your-store-password
SIGNING_KEY_ALIAS=your-key-alias
SIGNING_KEY_PASSWORD=your-key-password
```

Keep credentials in your local environment or CI secrets, never in tracked project files.
Release builds require signing credentials; debug builds do not.

```bash
./gradlew :androidApp:assembleRelease :androidApp:bundleRelease
```

Signed outputs are under `androidApp/build/outputs/apk/release` and
`androidApp/build/outputs/bundle/release`. Release builds shrink code and resources;
archive `androidApp/build/outputs/mapping/release/mapping.txt` with each published version
to retrace crash reports.

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

#### Web authentication and Firestore on localhost

The web app uses the Firebase JavaScript SDK (managed by Gradle) and the registered web app in
`you2-ae09a`. Its public client configuration lives in
`shared/src/jsMain/kotlin/com/alad1nks/you2/auth/WebAuthClient.kt`; Analytics is not initialized.
Do not add service-account credentials to the browser app.

In Firebase Console, enable **Email/Password** and **Google** under **Authentication > Sign-in method**.
Under **Authentication > Settings > Authorized domains**, add `localhost` (a hostname without a scheme
or port). Start `./gradlew :webApp:jsBrowserDevelopmentRun` and open the localhost URL printed by Gradle.
This connects to the real Firebase project, not the Auth emulator; accounts created here are real accounts.
Google sign-in opens a popup from the button click, so allow popups for localhost if the browser blocks it.

Firebase manages browser session persistence. The app waits for the initial auth-state event before
showing guest or signed-in content, and waits for sign-out to complete before clearing auth state.
Signed-in screens use Firestore rather than Alex/Mia demo data. New accounts get an empty profile
and can create or accept a partner invitation. Paired accounts receive realtime profiles, wishes,
and memories, including changes from Android/iOS. Publish `firestore.rules` to the project's default
Firestore database before using these flows. No additional web-specific rules or indexes are required.

Web writes use transactions and require connectivity; failed operations expose the existing retry
action. The SDK uses its default in-memory cache (no persistent browser database). Sign-out and
account switching remove listeners and clear displayed private data. Existing cached data can be
displayed during a temporary disconnect, but an offline cache miss never creates a new profile.
Web supports photo selection, private Firebase Storage uploads/downloads, and cleanup when images
are replaced or wishes are deleted. See [Web image storage](#web-image-storage) for bucket CORS setup.

Verify registration, existing-account login, wrong-password errors, password reset, Google sign-in and
popup cancellation. Reload to check session restoration, then sign out and reload to check the guest
screen returns. Test account switching and a second browser tab as well. When deploying later, authorize
the deployed hostname and verify the Google popup on that host.

Web integration tests run the actual Kotlin/JS adapter in headless Chrome against the
local Firestore and Storage emulators with mock authenticated users. Install Chrome, Node 24.12+, and Java 21,
then run from the repository root (use `gradlew.bat` instead of `./gradlew` on Windows):

```shell
npm --prefix firebase-tests ci
npm --prefix firebase-tests exec -- firebase emulators:exec --config firebase.json --project demo-you2 --only firestore,storage "./gradlew :shared:jsBrowserTest"
```

The tests cover two-account pairing, revoked invitations, realtime edits, the wish/memory lifecycle,
idempotent creation/completion, outsider read/write denial, reconnecting, sign-out, and offline profile
cache misses. Photo tests cover resizing, private uploads, partner access, replacement/deletion cleanup,
and create retries after partner edits. They use only the `demo-you2` emulator project, never production credentials.

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

#### iOS Firebase authentication

Register an Apple app in the existing Firebase project `you2-ae09a` with bundle ID
`com.alad1nks.you2`. Download its configuration to
`iosApp/iosApp/GoogleService-Info.plist` (beside `iOSApp.swift`, not beside the Xcode project).
The app's synchronized Xcode source folder automatically includes this plist as a bundled resource.
The plist is ignored by Git, like Android's `google-services.json`; copy it separately to your Mac.
Do not replace the app's existing `Info.plist`.

The Xcode project references FirebaseCore, FirebaseAuth, and FirebaseFirestore through Swift Package Manager,
pinned to Firebase Apple SDK 13.1.1. Let Xcode resolve packages on the first build.
Enable Email/Password in Firebase Console > Authentication > Sign-in method if it is not already enabled.
`iOSApp` configures Firebase before creating the native auth provider and the shared Compose UI.
Firebase owns credential storage and session restoration; passwords and tokens are not stored in app preferences.
The Swift provider implements `NativeAuthProvider`, and `NativeAuthClient` maps it into the shared auth flow.
Google sign-in uses GoogleSignIn-iOS 10.1.0 and the same shared button and auth flow as Android.
Enable Google in Firebase Console > Authentication > Sign-in method. The Apple plist must contain
`CLIENT_ID`; `GOOGLE_REVERSED_CLIENT_ID` in `iosApp/Configuration/Config.xcconfig` must match its
`REVERSED_CLIENT_ID` (update it when switching Firebase projects). SwiftUI forwards callback URLs
to the Google SDK. Google credentials are exchanged for a Firebase session; signing out clears both
sessions. Cancelling the account chooser returns to the form without an error.
See [Firebase's Google sign-in setup](https://firebase.google.com/docs/auth/ios/google-signin).
Apple sign-in is not implemented.

On a Mac, run the `iosApp` scheme on an ARM64 simulator or signed device and verify:

- Sign in with Google using an existing Android account; verify the same profile and couple are restored.
- Cancel Google sign-in, retry, then sign out and choose a different Google account.
- Register with a new email; sign out and sign back in, including with an existing Android email/password account.
- Terminate and relaunch: the session should remain signed in. Sign out, relaunch, and confirm the guest screen returns.
- Request password reset, follow the email, and sign in using the new password.
- Try a wrong password, an already registered email, and a disconnected network; verify errors and retry behavior.
- Check keyboard scrolling and password visibility on a small iPhone, in English, Russian, and Kazakh.

iOS uses `FirebaseCloudProvider` and the shared `NativeCloudRepository` for profiles, invitations,
wishes, and memories. Configure the default Firestore database and deploy the existing rules described
below. The Firebase project must match Android to share a couple across platforms. Signed-in iOS never
falls back to seeded data; its date source uses the current device timezone instead of the fixed demo date.
Creation and completion use transactions, require connectivity, and preserve the wish ID/first completion
on retries. Edits, restoration, memory changes, renaming, and deletion can queue offline; the UI shows
cached/pending-write state. Sign-out detaches listeners and clears shared UI state; it does not erase
Firebase's disk cache or cancel writes already submitted to the SDK.

Also verify on two iOS/Android accounts: create and accept an invitation, regenerate/expire a code,
rename both profiles, create/edit/delete a wish, complete it, edit its memory, restore it, and relaunch.
Verify realtime updates, cached reads, offline edits, retry after reconnect, and account switching with
an outstanding write. Invalid/used/self invitations must fail without changing membership.
Also verify photo selection (single cover and multiple attachments), cancellation, replacement/deletion,
retry after interrupted uploads, and cross-platform viewing of private Storage images. On iOS, toggle
viewer controls and close the viewer to check status bar/home indicator restoration. Both mobile clients
use bundled category artwork when a photo is unavailable and persist pending Storage cleanup.

Account deletion is still pending before App Store release. PR CI builds both the Kotlin device framework
and the unsigned simulator app (including Swift/Firebase); runtime testing requires the local plist and
a simulator/device. Shared JVM tests exercise snapshot decoding, metadata, account isolation, retries,
calendar-date writes, and listener teardown.

## Code organization

| Module / directory | Responsibility |
| --- | --- |
| [androidApp](androidApp) | Android activity, manifest, launcher resources, and application build |
| [iosApp](iosApp) | Xcode project and SwiftUI host |
| [desktopApp](desktopApp) | JVM entry point, window, and installer configuration |
| [webApp](webApp) | Browser entry point, HTML, and CSS |
| [shared](shared) | App composition, paired navigation, Wishes/Memories/Us screens, forms, `MainViewModel`, and platform adapters |
| [core/model](core/model) | Couple, user, wish, completion, category, date, image, and language models; draft validation and date status |
| [core/data](core/data) | `WishesRepository`, its current in-memory implementation, seeded development data/date source, and `SettingsRepository` |
| [core/domain](core/domain) | Theme and language read/write use cases |
| [core/storage/common](core/storage/common) | `Storage`, `StorageImpl`, and `AppPreferences` contracts |
| [core/storage/datastore](core/storage/datastore) | DataStore Preferences for Android, iOS, and JVM |
| [core/storage/js](core/storage/js) | Browser `localStorage` preferences |
| [core/designsystem](core/designsystem) | Shared components, Material icons, color schemes, typography, spacing, and shape tokens |
| [resources](resources) | Shared localized strings, fonts, and category images |
| [design/icons](design/icons) | Original SVG design references |

`App()` creates Koin with `CommonModules + PlatformModules`. Android overrides the demo repository with
`FirestoreWishesRepository` from `core/data/androidMain` and a real clock. iOS injects
`NativeCloudRepository` with a Swift Firebase provider and a real clock. Web uses the same repository
with `WebCloudProvider`, the Firebase JavaScript SDK, and the browser's current local date.
`MainViewModel` observes the singleton
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
Changes to seeded development data or the fixed date can affect many screenshot references.

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
under `shared/src/jvmTest/screenshots`. CI generates and verifies them on **Ubuntu 24.04 with
Amazon Corretto JDK 21**, using Xvfb; font rendering can differ on other operating systems.

```bash
# Compare with committed references on Ubuntu with JDK 21 and Xvfb installed.
xvfb-run --auto-servernum ./gradlew :shared:verifyRoborazziJvm
```

For intentional visual changes, commit and push your feature branch, then run
[Generate screenshots](.github/workflows/generate-screenshots.yml) in GitHub Actions, selecting
that branch in **Use workflow from** (or `gh workflow run generate-screenshots.yml --ref <branch>`).
Wait for the run to succeed, then run `git pull --ff-only origin <branch>` and visually review
the changed PNGs **before creating the PR**. The workflow records and verifies references,
commits only the screenshot directory, and pushes to the selected branch. It creates no commit
when references are unchanged. Runs from `main` or tags are rejected. If the branch advances
during generation, the push fails instead of overwriting changes; pull and rerun the workflow.

GitHub requires a manually dispatched workflow to exist on the default branch first. After
installing this workflow, regenerate the existing Windows references on a feature branch to
complete the Ubuntu baseline migration. Do not replace CI references with locally generated
macOS or Windows images.

A plain `:shared:jvmTest` runs interactions without pixel verification. Screenshot scenarios cover
light/dark themes, category covers, empty and long content, dialogs, forms, details, and complete journeys.
Local reports are in `shared/build/reports/roborazzi` and `shared/build/reports/tests/jvmTest`;
comparison images are under `shared/build/outputs/roborazzi`.

These JVM checks do not establish native Android/iOS rendering or keyboard, inset, and back-gesture
behavior. Those need device/emulator validation, and iOS builds require macOS/Xcode.

## CI and distribution

| Workflow | Trigger | Configured behavior |
| --- | --- | --- |
| [pr-checks.yml](.github/workflows/pr-checks.yml) | PRs targeting `main` or `master` | Android debug build, iOS release framework, production JS webpack build, shared JVM jars, ktlint, and Ubuntu screenshot verification |
| [generate-screenshots.yml](.github/workflows/generate-screenshots.yml) | Manual, feature branches only | Record and verify Ubuntu screenshots, commit and push references to the selected branch |
| [build-and-deploy.yml](.github/workflows/build-and-deploy.yml) | Push to `main` | Tests and release APK build, followed by APK publication under `you2` in `alad1nks/alad1nks.github.io` |
| [release.yml](.github/workflows/release.yml) | Push to `release/*` | Tests and release AAB build, followed by AAB publication to the same repository |

CI uses Amazon Corretto 21. The screenshot job uploads `screenshot-test-reports`, including on failure.
The PR JVM command is `jvmJar`, which does not build the Desktop application entry point;
use `:desktopApp:jar` or run/package Desktop when changing that target. The iOS job builds the Kotlin
framework, not the complete SwiftUI app.

Release publishing workflows decode the keystore to `release-key.jks` and pass the
`SIGNING_*` environment variables consumed by the Android release signing configuration.
The APK workflow publishes the signed `androidApp-release.apk`.

The publishing workflows reference `KEYSTORE_BASE64`, `KEY_ALIAS`, `KEY_PASSWORD`, `KEYSTORE_PASSWORD`,
and `ACCESS_TOKEN`. These are not required for local debug builds or the PR checks.

## Android Firebase authentication

The Android app uses Firebase project `you2-ae09a` and package `com.alad1nks.you2`.
Keep its ignored client configuration at `androidApp/google-services.json` and enable Email/Password in
Firebase Console → Authentication → Sign-in method. Firebase Auth SDK versions are managed by the
Firebase BoM in `gradle/libs.versions.toml`. No admin credentials are needed in the app.

For Google sign-in, enable the Google provider in the same Firebase project, set its support email,
and register the SHA-1 and SHA-256 fingerprints of each Android signing certificate (debug, release,
and Play App Signing where applicable). Download the updated `google-services.json` into `androidApp`;
it must include the Web OAuth client so the Google Services plugin generates `default_web_client_id`.
The app reads that generated resource rather than hardcoding an OAuth client ID.
See [Firebase's Google sign-in setup](https://firebase.google.com/docs/auth/android/google-signin).

The login and registration screens offer Continue with Google above the email fields, matching
Figma node `100:1094`. Android Credential Manager opens the Google account picker and Firebase
exchanges its ID token for the same session used by email sign-in. Cancelling returns to the form
without an error; failures allow retry or email sign-in. Sign-out clears Firebase and requests
credential-provider session cleanup. Google tokens are never saved in application preferences or logs.
Google sign-in requires a device or emulator with Google Play services and a configured Google account;
the JVM tests cover UI and state transitions, not a live OAuth exchange.

Signed-out Android users start with the mobile guest previews for Wishes, Memories, and Us. Each tab
has its own illustration and introductory content, a persistent language selector, and Create account
and Log in actions. Returning from authentication restores the selected guest tab; signing out returns
to the Wishes preview. Preview cards are examples and do not modify the shared wish repository.
The guest content scrolls on small screens and with enlarged text while the bottom tabs remain accessible.

Android supports account creation with password confirmation and password-reset
emails, restores the Firebase session on restart, and provides sign-out from the Us tab.
Passwords stay in memory only and are cleared when changing forms or leaving the authentication screen.
Authentication screens and state live in shared/commonMain and use the common English, Russian, and Kazakh resources.
All platforms enter through the single shared App, which owns Koin, the theme, locale, and screen routing.
Android DI supplies FirebaseAuthClient from shared/androidMain; iOS injects NativeAuthClient backed by FirebaseAuthProvider.
Desktop and Web retain the demo flow.

Android and iOS main tabs display the signed-in user's shared Firestore data. Loading, unpaired, and failed
states never fall back to Alex/Mia sample content. Web/Desktop cloud data and email verification UI
are not implemented.

Validation:

```powershell
.\gradlew.bat :shared:jvmTest :androidApp:assembleDebug :shared:ktlintCheck :androidApp:ktlintCheck
```

On a device, register with an email address you control, restart to check session restoration,
sign out and sign back in, then request a password-reset email. Also check offline and invalid-password
errors. Unit tests use a fake auth client and do not create production Firebase users or send emails.

Android CI restores this file from the repository Actions secret `GOOGLE_SERVICES_JSON`. Set its value
to the complete downloaded JSON. This secret is required for Android CI (and is unavailable to fork PRs).

## Android Firestore setup

Use the **default `(default)` Cloud Firestore database**, Standard edition, in the same Firebase project
as `androidApp/google-services.json`. The client discovers its location automatically; no region string
or administrator credential belongs in the app. A database created in Production mode rejects requests
until the application rules are published.

Publish the complete contents of [firestore.rules](firestore.rules) in Firebase Console → Firestore
Database → Rules → Publish. Alternatively, with Firebase CLI access to the project, run from the repo root:

```powershell
firebase deploy --only firestore:rules --project you2-ae09a
```

This is a separate deployment step: a code merge does not deploy the rules. No collections need to be
created manually. Cloud Functions are not required. Photo uploads require a Firebase Storage bucket in the same project and the Storage rules below.

Data layout:

- `users/{uid}`: display name and `coupleId`; only the user can change their profile, and their partner can read it.
- `couples/{coupleId}`: immutable membership after two users join, current invitation ID, creation timestamp.
- `couples/{coupleId}/wishes/{wishId}`: one wish per document, including optional completion/memory fields.
- `invites/{code}`: random UUID v4 code (without hyphens), owner, pair, expiry, and one-time consumer.
  Invitation collections cannot be listed. An authenticated user who possesses the code can read it only
  while it is current, unused, unexpired, and the pair has one member. The owner can read their historical codes.

One partner creates an invitation from any unpaired tab; the second registers/signs in and pastes
the code in **I have an invite code**. Names can be edited in **Us > Your profile**; unnamed accounts
use the localized **You** label until edited. Reopening the invitation preserves the current code and
offers **Create a new invitation** when a replacement is needed. Invitations expire after 24 hours;
generating a new one invalidates the old one. Creation
and acceptance use Firestore transactions, with Security Rules validating all related documents through
`getAfter`. Two concurrent join attempts cannot add a third member. No client can directly assign a partner.
Initial profile creation, pairing, and wish completion require internet access. Invitation creation
requires a reasonably accurate device clock. Completion uses a transaction so concurrent completion
cannot replace an existing memory or its author.

The repository listens for profile, pair, and wish changes. Firestore caches data locally; previously
loaded data can be shown offline, and queued writes display a waiting-for-server dialog. The UI allows
one pending operation at a time. Signing out clears displayed state, cancels listeners, and ignores late
callbacks, but does not cancel an SDK write already queued or erase Firestore's disk cache. Failed writes
can be explicitly retried with the same document ID and payload. Cancelling the error discards that retry.
Edits update only the relevant fields; content edits use last-write-wins behavior. A stale memory edit
cannot recreate a restored memory. Server timestamps track writes; wish deadlines remain local calendar dates.

Current limits: each wish allows up to 12 attachments; Android photos use private Storage objects,
wish lists are loaded in full (pagination
is needed for larger histories), and pair unlinking/account deletion are not implemented. An account that
has created its own invitation must receive its partner into that space; switching to another space is
not implemented. Only one of the two partners should create an invitation.

### Firestore validation

Calendar dates in wishes must be real dates in years 0001 through 9999, including Gregorian leap-year
checks. Invalid month/day combinations are rejected in creation dates, deadlines, ranges, and completions.

### Storage security and deployment

Storage remains available to couple members: upload and read images for existing wishes, and delete
images even after a wish is removed. The rules preserve the existing paths, image formats, and 5 MiB
per-file limit. They reject application custom metadata, compressed content encodings, and arbitrary
content-disposition overrides. Listing, overwrites, and metadata updates remain forbidden.

These are access and metadata checks, not image-byte validation or aggregate quotas. Storage Rules
cannot count all objects or atomically reserve total storage across uploads. A trusted backend must
enforce account storage/count quotas and validate or re-encode actual image contents if those guarantees
are required. Client-side checks and App Check do not replace those controls. This PR does not implement
that backend or claim that unlimited aggregate uploads and spoofed image bytes are prevented.

Deploy both rule files to apply this hardening (merging this PR does not deploy rules):

```powershell
firebase deploy --only "firestore:rules,storage" --project you2-ae09a
```

If the previous Storage rules were ever deployed, review the bucket's existing objects. Previously
issued Firebase download-token URLs remain usable even after rules deny reads or a wish is deleted.
Revoke those tokens in Firebase Console or through a trusted administrative process; delete unwanted
objects after review. Rules cannot revoke existing tokens. Also verify that bucket IAM does not grant
public access. This repository change does not modify production objects, tokens, or IAM.

Before deploying, audit existing wish dates and repair invalid historical values through a trusted
administrative process. New validation prevents further malformed writes but does not rewrite old data.

### Rules tests

Rules tests use a **demo project and local emulator only**, without production credentials. Install Node 24.12 or newer
and Java 21, then run:

```powershell
cd firebase-tests
npm ci
npm test
```

The configured Firestore emulator port is `8185`; Storage uses `9199`. The suite covers access isolation,
atomic pairing, concurrent acceptance, invitation read revocation, calendar validation, the complete
wish/memory lifecycle, member Storage access, outsider denial, and upload metadata validation.
The same suite runs in PR CI. Shared tests cover document mapping and hiding data from a previous account.

Before release, on two Android devices/accounts verify invitation creation/join, realtime edits,
completion/restoration, profile renaming, restart, offline writes, sign-out and account switching.
Verify that the third account cannot read or write the pair's data. Do not loosen rules to public access
to work around a denied request.

## Storage rules for wish images

Android and iOS use the system photo picker for a cover and up to 12 attachments, with no broad media permission.
Selections are decoded, resized to at most 2048 pixels on the longest edge, and re-encoded as JPEG
without original metadata. Each encoded image must fit the 5 MiB limit. Previews are staged in app cache;
cancelling the form does not upload anything. The system can evict staged files; select the photo again
if a staged file is no longer available.

Save shows the existing waiting/error/retry dialog while the upload completes. New wishes are first
saved without local photos because Storage requires an existing wish. If an upload fails or the app
closes, that wish can remain without its photos; retry in the current session or edit it to select again.
Retries reuse completed uploads and the same wish ID. Replacement uploads use fresh object IDs, then
update Firestore and clean up superseded files. Downloads use the authenticated Storage SDK and never
request persistent download-token URLs. Unavailable images fall back to category artwork.

A per-account persistent cleanup queue tracks uploads and removed references. Cleanup checks current
server references before deleting, and retries after subsequent saves or sign-in. It also handles wish
deletion. Uninstalling/clearing app data loses this client queue; guaranteed orphan collection requires
a backend. Uploads and image edits/deletion require connectivity. Desktop retains demo images.
Web supports browser photo selection and private Firebase Storage images.

Create the default Firebase Storage bucket for the project in `androidApp/google-services.json` and
ensure that configuration includes `storage_bucket`. Deploy both `firestore.rules` and `storage.rules`
before using uploads. No production deployment is performed by the application build.

Allowed paths are `couples/{coupleId}/wishes/{wishId}/{imageId}.jpg` (or `.png` / `.webp`). Use a new
random ID per upload, containing 1–128 letters, digits, underscores, or hyphens before the extension.
Both members listed in the Firestore couple document can read, upload, and delete images. Reads and
uploads require an existing wish document. Deletes remain allowed after wish deletion so orphaned
files can be cleaned up. Listing, overwrites, metadata updates, and all other paths are denied.

Uploads must be nonempty, at most **5 MiB (5,242,880 bytes)**, and have a matching `image/jpeg`,
`image/png`, or `image/webp` content type. Rules validate metadata, not the actual image bytes. The
upload implementation must decode/re-encode images. These rules do not enforce a per-wish file count;
the existing 12-attachment limit applies to the wish model, not the number of uploaded Storage objects.

The Android uploader sends JPEG content type, identity encoding, and inline disposition without custom
metadata. Rules do not whitelist optional metadata: Firebase normalizes it during real uploads, and
the Storage emulator does not reproduce every production shape. Authorization depends exclusively on
Firebase Authentication and Firestore membership, never on file metadata ownership or validation claims.
The app decodes image bytes for display and does not interpret custom metadata. Firebase can issue
download tokens; these rules must not be treated as token revocation.

Save the wish document before uploading. Image replacement uses a new object path, followed by a
Firestore reference update and deletion of the old object. Use authenticated SDK downloads for private
images; do not distribute persistent download-token URLs. The client persists references and retries cleanup. Firestore rules accept bundled keys or private
image paths belonging to the same couple and wish; local file paths and URLs are rejected.

To publish, paste the complete `storage.rules` file into Firebase Console → Storage → Rules → Publish,
or run from the repository root with Firebase CLI access:

```powershell
firebase deploy --only storage --project you2-ae09a
```

When prompted, enable the Storage Rules permission to read Firestore; the membership checks use the
`(default)` database in the same project. No bucket name or credentials are embedded in the rules.
Preparing or committing these files does not deploy them.
Rule releases can take several minutes to propagate. After publishing, allow that delay before
retrying a denied upload; a successful Rules Playground simulation does not prove the Android upload
has reached a server using the new release.

`npm test` in `firebase-tests` runs both Firestore and Storage rules suites sequentially against local
emulators (Firestore `8185`, Storage `9199`) using the `demo-you2` project. Storage tests cover both
partners, outsiders, removed membership, missing/deleted wishes, format/size boundaries, and denied
listing/overwrite operations. No production Firebase data is used.

### Web image storage

The browser picker re-encodes photos as JPEG (maximum 2048 pixels on the longest side and 5 MiB).
Selections stay in memory until saved. Only private Storage paths are written to Firestore; downloads
use authenticated `getBytes`, with category artwork as the fallback on failure. Cleanup uses a
per-account localStorage queue, so browser storage must be available to save photos. Clearing site
data removes the queue. Web Locks serialize writes and cleanup across tabs; use a modern browser
on HTTPS (or localhost).

Authenticated browser downloads require bucket CORS configuration, as described in the
[Firebase documentation](https://firebase.google.com/docs/storage/web/download-files#cors_configuration).
Apply the provided configuration with deployment credentials (this is not run by the build):

```powershell
gcloud storage buckets update gs://you2-ae09a.firebasestorage.app --cors-file=storage.cors.json
```

Add any custom hosting origins to `storage.cors.json` before deployment. Storage rules still enforce
Firebase authentication and couple membership; CORS does not grant access.
