# you2 assets

Runtime resources live in `src/commonMain/composeResources` within this module and use the generated
package `com.alad1nks.you2.resources.generated`. Shared UI components and resource mappings live in
`core/designsystem` and `shared`.

## Password visibility

The password visibility control reuses the installed Material outlined `Visibility` and
`VisibilityOff` vectors through `AppIcons`, matching Figma components `132:2993` and `132:2997`.
The icons render at 24 dp inside a 44 dp button with localized Show/Hide password action labels.

## Google sign-in

`drawable/google.png` is the unmodified 20 × 20 PNG export of Figma layer `100:1096`,
downloaded October 31, 2026. Its SVG source is preserved in `design/icons/google.svg`.
The authentication button displays it at 20 × 20 dp without tint, on login and registration.
It retains Figma's white provider-button background in both app themes.

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

## Guest illustrations

The three original PNG image fills were downloaded from the mobile Guest experience section of the
same Figma file on October 31, 2026. They are bundled locally with no runtime Figma requests.

| Resource | Figma image layer | Display slot |
| --- | --- | --- |
| `guest_wishes.png` | `58:983` | Wishes preview, 364 × 160 dp at the reference width, 20 dp corners |
| `guest_memories.png` | `58:998` | Memories preview, 364 × 160 dp at the reference width, 20 dp corners |
| `guest_us.png` | `54:1028` | Us welcome, 364 × 160 dp at the reference width, 24 dp corners |

`GuestIllustration` uses a responsive width and crops the image to a 160 dp height. Guest UI icons reuse
the shared Material vectors, including `KeyboardArrowDown`, `PersonAdd`, and `FormatListBulleted`.

## App icon

The app icon comes from [you2 / App icon, node 110:1990](https://www.figma.com/design/4NzqzLDkOMaaGQGKYhEeuT/you2?node-id=110-1990),
exported on October 31, 2026. The unmodified SVG and opaque 1024 × 1024 PNG master are in
`design/brand`. Run `python design/brand/generate_icons.py` with Pillow installed to regenerate
the platform assets. The script preserves the artwork and aspect ratio, resizes with Lanczos,
and applies only platform delivery masks and padding.

Android uses density-specific legacy icons and an adaptive icon with the master centered in
the 72 dp viewport of a 108 dp layer, with a matching green background extending into the bleed.
iOS uses the opaque 1024 px image in the existing AppIcon asset catalog. Desktop uses a PNG
window icon plus ICO, ICNS, and PNG native package icons. Web uses SVG/ICO favicons and a
180 px Apple touch icon. All assets are local; no Figma URLs are requested at runtime.

## Interface icons

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

## Image loading indicators

`image_loading_dots.png` and `image_loading_spinner.png` are transparent 4x rasterizations
of the unmodified SVG exports in `design/icons/`. They come from the you2 Figma
file `4NzqzLDkOMaaGQGKYhEeuT`, nodes `172:3149` and `171:3101`, respectively.
The app displays them at 23 x 5 dp and 32 x 32 dp. Dots follow the secondary
theme color; the viewer rotates the original light spinner on its dark stage.

To regenerate with `resvg-py` installed:

```python
from pathlib import Path
import resvg_py

for name in ["image_loading_dots", "image_loading_spinner"]:
    Path(f"resources/src/commonMain/composeResources/drawable/{name}.png").write_bytes(
        resvg_py.svg_to_bytes(svg_path=f"design/icons/{name}.svg", zoom=4)
    )
```
