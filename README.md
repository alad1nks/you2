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
Android uses Cloud Firestore for profiles, partner invitations, shared wishes, and memories. After sign-in,
users return to the tab they were viewing before authentication. For unpaired users, Us shows
**Your space**, with profile, appearance, and language settings.
All three tabs remain available; Wishes and Memories explain how to connect. **Invite your partner**
opens a copyable-code dialog, and **I have an invite code** opens the join dialog. After pairing,
**View our wishes** opens the shared list.
Other platforms still use the demo flow with sign-out disabled. Android supports private photo uploads for wish covers and attachments.

For non-Android development and UI validation, the demo screens use seeded data for Alex and Mia:
12 active wishes and four memories. `InMemoryWishesRepository` holds this data, so wish, memory, and
profile changes reset on restart or browser reload. Android supplies the current local date; `DemoDateSource` supplies the fixed date
**October 28, 2026**. Non-Android image pickers use bundled illustrations. Theme and language
preferences already use platform storage; seeded wish content stays English when the UI language changes.

## Design and resources

The [you2 Figma design](https://www.figma.com/design/4NzqzLDkOMaaGQGKYhEeuT/you2-%E2%80%94-Our-wishes)
is the product design reference. It covers guest previews, Us and pairing, wish lists and timing,
wish forms and images, wish details and actions, and completed wishes and memories. Its design system
defines the shared colors, typography, forms, components, spacing, and category illustrations.
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
| [core/data](core/data) | `WishesRepository`, its current in-memory implementation, seeded development data/date source, and `SettingsRepository` |
| [core/domain](core/domain) | Theme and language read/write use cases |
| [core/storage/common](core/storage/common) | `Storage`, `StorageImpl`, and `AppPreferences` contracts |
| [core/storage/datastore](core/storage/datastore) | DataStore Preferences for Android, iOS, and JVM |
| [core/storage/js](core/storage/js) | Browser `localStorage` preferences |
| [core/designsystem](core/designsystem) | Shared components, Material icons, color schemes, typography, spacing, and shape tokens |
| [resources](resources) | Shared localized strings, fonts, and category images |
| [design/icons](design/icons) | Original SVG design references |

`App()` creates Koin with `CommonModules + PlatformModules`. Android overrides the demo repository with
`FirestoreWishesRepository` from `core/data/androidMain` and a real clock. `MainViewModel` observes the singleton
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
Android DI supplies FirebaseAuthClient from shared/androidMain; other platforms keep the demo flow until an auth client is registered.

Android main tabs display the signed-in user's shared Firestore data. Loading, unpaired, and failed states
never fall back to Alex/Mia sample content. iOS, Web, and Desktop authentication and cloud data,
and email verification UI, are not implemented.

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

Rules tests use a **demo project and local emulator only**, without production credentials. Install Node 22
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

Android uses the system photo picker for a cover and up to 12 attachments, with no broad media permission.
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
a backend. Uploads and image edits/deletion require connectivity. Non-Android clients retain demo images.

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
