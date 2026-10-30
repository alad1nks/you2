"""Generate platform icons from the unmodified Figma PNG export (requires Pillow)."""

from pathlib import Path
import shutil

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[2]
SOURCE = Path(__file__).with_name("app-icon.png")
master = Image.open(SOURCE).convert("RGB")
assert master.size == (1024, 1024)
background = master.getpixel((0, 0))


def save(image, relative, **options):
    path = ROOT / relative
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path, **options)


for density, size in [("mdpi", 48), ("hdpi", 72), ("xhdpi", 96), ("xxhdpi", 144), ("xxxhdpi", 192)]:
    icon = master.resize((size, size), Image.Resampling.LANCZOS)
    directory = f"androidApp/src/main/res/mipmap-{density}"
    save(icon, f"{directory}/ic_launcher.png")
    mask = Image.new("L", master.size)
    ImageDraw.Draw(mask).ellipse((0, 0, 1023, 1023), fill=255)
    rounded = master.convert("RGBA")
    rounded.putalpha(mask)
    save(rounded.resize((size, size), Image.Resampling.LANCZOS), f"{directory}/ic_launcher_round.png")

# Fit the master inside the adaptive icon's central 72/108 viewport.
foreground = Image.new("RGB", (432, 432), background)
foreground.paste(master.resize((288, 288), Image.Resampling.LANCZOS), (72, 72))
save(foreground, "androidApp/src/main/res/drawable-xxxhdpi/ic_launcher_foreground.png")
save(master, "iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/app-icon-1024.png")
save(master.resize((256, 256), Image.Resampling.LANCZOS), "desktopApp/src/main/resources/app-icon.png")
save(master, "desktopApp/icons/app-icon.png")
save(master, "desktopApp/icons/app-icon.ico", sizes=[(s, s) for s in (16, 24, 32, 48, 64, 128, 256)])
save(master, "desktopApp/icons/app-icon.icns")
save(master, "webApp/src/webMain/resources/favicon.ico", sizes=[(s, s) for s in (16, 32, 48)])
save(master.resize((180, 180), Image.Resampling.LANCZOS), "webApp/src/webMain/resources/apple-touch-icon.png")
shutil.copyfile(SOURCE.with_suffix(".svg"), ROOT / "webApp/src/webMain/resources/favicon.svg")
