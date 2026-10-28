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

Roboto Regular, Medium and Bold are bundled from
[googlefonts/roboto-2](https://github.com/googlefonts/roboto-2/tree/main/src/hinted).
Their Apache 2.0 license is included in `Roboto-LICENSE.txt`.

English is the first UI reference. Interface copy is in `values/strings.xml`;
Russian and Kazakh translations and the language picker are part of the Us stage.
Demo wish content represents user-authored data and intentionally remains English.
