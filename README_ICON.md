# Icon Workflow

This project keeps launcher icon sources in `assets/` and exports Android PNG mipmaps with a local script.

## Source files

- `assets/icon_source.svg` - full-color master icon
- `assets/icon_monochrome.svg` - monochrome reference for themed icons

## Android resources used at runtime

- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`
- `app/src/main/res/drawable/ic_launcher_foreground.xml`
- `app/src/main/res/drawable/ic_launcher_background.xml`
- `app/src/main/res/drawable/ic_launcher_monochrome.xml`

Legacy launcher PNGs:

- `app/src/main/res/mipmap-mdpi/ic_launcher*.png` (48x48)
- `app/src/main/res/mipmap-hdpi/ic_launcher*.png` (72x72)
- `app/src/main/res/mipmap-xhdpi/ic_launcher*.png` (96x96)
- `app/src/main/res/mipmap-xxhdpi/ic_launcher*.png` (144x144)
- `app/src/main/res/mipmap-xxxhdpi/ic_launcher*.png` (192x192)

Play Store icon:

- `assets/playstore-icon.png` (512x512)

## Export command

Run from repo root:

```powershell
.\tools\export_icons.ps1
```

The script behavior:

- Uses Inkscape CLI if available (`inkscape` in PATH or common Windows install paths).
- Falls back automatically to local `.NET System.Drawing` rendering when Inkscape is not found.

Force fallback renderer:

```powershell
.\tools\export_icons.ps1 -ForceDotNet
```

## Optional Inkscape install (Windows)

```powershell
winget install Inkscape.Inkscape
```

After install, run export again for SVG-based PNG output.
