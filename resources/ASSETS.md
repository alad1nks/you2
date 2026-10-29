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

The 24 × 24 icons come from the original vector nodes: heart `43:727`,
Memories `43:782`, couple `43:787`, more `23:131`. Unmodified SVG sources are in
`design/icons`. These SVGs are rendered to PNG with resvg (intrinsic size, transparent
background), because direct Figma PNG exports include the page background.
PNG is used for shared Android, iOS, JVM and JS resource support.
`Icon` applies the current theme's tint.

The calendar icon is the original 14 × 14 SVG from date layer `I43:897;42:526`
in the Wishes screen (`1:2`), exported on 2026-10-28. Its unmodified source is
`design/icons/calendar.svg`; `icon_calendar.png` is rendered with resvg at its
intrinsic size and displayed at 14 dp, tinted for each date status.

Roboto Regular, Medium and Bold are bundled from
[googlefonts/roboto-2](https://github.com/googlefonts/roboto-2/tree/main/src/hinted).
Their Apache 2.0 license is included in `Roboto-LICENSE.txt`.

Wish details adds the original 20 × 20 back (`18:111`), edit (`21:121`) and
delete (`21:125`) vectors from Figma, exported on 2026-10-29. Their unmodified
sources are `design/icons/back.svg`, `edit.svg` and `delete.svg`. The corresponding
`icon_back.png`, `icon_edit.png` and `icon_delete.png` resources are rendered with
resvg at intrinsic size and displayed at 20 dp with theme-aware tints. Details
reuse the existing category covers, heart, more and calendar resources.

English is the first UI reference. Interface copy is in `values/strings.xml`;
Russian and Kazakh translations and the language picker are part of the Us stage.
Demo wish content represents user-authored data and intentionally remains English.

Add/Edit wish reuses the local category illustrations as the demo image collection.
The original 24 x 24 add-image vectors from `37:406` and `34:698` were downloaded
on 2026-10-29. Unmodified SVG sources are `design/icons/add_image.svg` and
`design/icons/add.svg`; resvg renders their intrinsic size to `icon_add_image.png`
and `icon_add.png`. The form uses these in the cover and attachment actions,
and reuses back (20 dp), calendar (14 dp), heart (24 dp), and delete (20 dp).

Memories uses the original completion check from `46:989` (24 x 24) and restore
arrow from `47:959` (20 x 20), downloaded on 2026-10-29. Unmodified sources are
`design/icons/completed.svg` and `restore.svg`; resvg renders their intrinsic
sizes to `icon_completed.png` and `icon_restore.png`. Checks appear in timeline
cards and completion summaries at 24 dp; restore appears in the menu at 20 dp.
Completed details reuse category covers in the reference's 144 dp high slot,
plus the existing back, more, edit and delete icons. Cover radius follows the
shared 24 dp plan token. System status and gesture bars remain platform-owned.
