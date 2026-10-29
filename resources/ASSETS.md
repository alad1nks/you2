# Paired UI assets

Category illustrations are original image fills from the you2 Figma file
`4NzqzLDkOMaaGQGKYhEeuT`, section `23:118`, downloaded on 2026-10-28.
The original 1891 ? 832 fills are resized with Lanczos to 1092 ? 480 pixels,
matching the 364 ? 160 dp slot at 3? density, then encoded as lossless WebP.
The six illustrations total 1,989,900 bytes. A decoded RGBA image needs about 2 MiB
instead of 6 MiB for the original resolution. Displays above 3? may upscale the image.
They are bundled locally; the application never requests temporary Figma URLs.

| Resource | Figma image layer |
| --- | --- |
| `category_adventures.webp` | `23:142` |
| `category_presents.webp` | `23:199` |
| `category_plans.webp` | `23:256` |
| `category_movies.webp` | `23:314` |
| `category_tv_series.webp` | `23:371` |
| `category_other.webp` | `23:428` |

`WishCover` resolves these by category and uses a full-width, 160 dp high crop.
The image corner radius follows the implementation plan's 24 dp token.
The reference category panels use 364 × 160 slots with a 20 px radius.

## UI icons

UI icons use `org.jetbrains.compose.material:material-icons-extended:1.7.3`
through `AppIcons` in `core/designsystem`. This dependency is versioned separately
from Compose Multiplatform and Material 3. Icons use the Outlined family;
back, restore and sign-out use AutoMirrored variants. Existing dp sizes,
theme tints and accessibility descriptions are preserved.

| Previous PNG | Material icon |
| --- | --- |
| `icon_heart` | `FavoriteBorder` |
| `icon_memories` | `PhotoLibrary` |
| `icon_couple` | `People` |
| `icon_more` | `MoreHoriz` |
| `icon_calendar` | `CalendarToday` |
| `icon_back` | `ArrowBack` (AutoMirrored) |
| `icon_edit` | `Edit` |
| `icon_delete` | `DeleteOutline` |
| `icon_add_image` | `AddPhotoAlternate` |
| `icon_add` | `Add` |
| `icon_completed` | `CheckCircle` |
| `icon_restore` | `Undo` (AutoMirrored) |
| `icon_chevron` | `ChevronRight` |
| `icon_connected` | `Link` |
| `icon_sign_out` | `Logout` (AutoMirrored) |

The old `icon_*.png` resources have been removed. Original Figma SVG exports in
`design/icons` remain as design references, not runtime assets. Category images,
fonts and application launcher icons are unchanged.

## Fonts and copy

Roboto Regular, Medium and Bold are bundled from
[googlefonts/roboto-2](https://github.com/googlefonts/roboto-2/tree/main/src/hinted).
Their Apache 2.0 license is included in `Roboto-LICENSE.txt`.

English is the first UI reference. Interface copy is in `values/strings.xml`;
Russian and Kazakh translations are in `values-ru` and `values-kk` and are available
through the Us language picker (including the system locale).
Demo wish content represents user-authored data and intentionally remains English.

Add/Edit wish reuses the local category illustrations as the demo image collection.
Completed details use category covers in a 144 dp high slot with the shared
24 dp corner radius. System status and gesture bars remain platform-owned.
The disabled sign-out action includes a visible demo explanation.
