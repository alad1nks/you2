# you2 assets

Runtime resources live in `src/commonMain/composeResources` within this module and use the generated
package `com.alad1nks.you2.resources.generated`. Shared UI components and resource mappings live in
`core/designsystem` and `shared`.

## Category illustrations

The six bundled lossless WebP images are in `src/commonMain/composeResources/drawable`.
They are image fills exported from the you2 Figma file `4NzqzLDkOMaaGQGKYhEeuT`, section `23:118`,
on October 28, 2026. The original 1891 x 832 fills were resized with Lanczos to 1092 x 480 pixels,
matching a 364 x 160 dp slot at 3x density. Higher densities may upscale them.
The application loads these local resources without requesting Figma image URLs.

| Resource | Figma image layer |
| --- | --- |
| `category_adventures.webp` | `23:142` |
| `category_presents.webp` | `23:199` |
| `category_plans.webp` | `23:256` |
| `category_movies.webp` | `23:314` |
| `category_tv_series.webp` | `23:371` |
| `category_other.webp` | `23:428` |

`WishCover` crops its supplied image to the available width and defaults to 160 dp height.
Completed wish details use a 144 dp cover. Both use the shared `AppShapes.image` 24 dp corner radius.
The current Add/Edit wish image picker reuses these illustrations for covers and attachments.
Personal photo selection remains to be implemented. `WishImage` currently stores a category key
rather than a file or remote URL.

## UI icons

Use `AppIcons` in `core/designsystem` for interface icons. It wraps Material Outlined vectors from
`org.jetbrains.compose.material:material-icons-extended`; the version is pinned independently of
Compose Multiplatform and Material 3 in `gradle/libs.versions.toml`.

| Use | AppIcons property |
| --- | --- |
| Wishes | `FavoriteBorder` |
| Memories | `PhotoLibrary` |
| Couple / Us | `People` |
| More actions | `MoreHoriz` |
| Dates | `CalendarToday` |
| Back | `ArrowBack` (AutoMirrored) |
| Edit | `Edit` |
| Delete | `DeleteOutline` |
| Add image | `AddPhotoAlternate` |
| Add wish | `Add` |
| Completed wish | `CheckCircle` |
| Restore wish | `Undo` (AutoMirrored) |
| Row navigation | `ChevronRight` |
| Connected status | `Link` |
| Sign out | `Logout` (AutoMirrored) |

Original SVG exports under `design/icons` are design references. Runtime UI icons are Material vectors;
launcher icons are maintained separately in the platform applications.

## Fonts and copy

Roboto Regular, Medium, and Bold are bundled in `src/commonMain/composeResources/font`.
They were sourced from `googlefonts/roboto-2` (`src/hinted`); their Apache 2.0 license is included in
[Roboto-LICENSE.txt](Roboto-LICENSE.txt). `AppTheme` applies them through the typography in
`core/designsystem`.

English interface strings live in `src/commonMain/composeResources/values/strings.xml`.
Russian and Kazakh translations live in `values-ru` and `values-kk`. The Us language picker also offers
the system locale. Keep all three string sets updated together. Seeded wish titles, descriptions, and
memories represent user-authored content and remain English when the UI language changes.

After changing visible assets, fonts, or copy, run the shared screenshot verification described in
[the README](../README.md#screenshot-regression-tests). Record new references on Windows with JDK 21
only for intentional changes, and visually review the PNG differences before committing.
