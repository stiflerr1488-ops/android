# Icon Generation Tools

This project uses vector graphics (`VectorDrawable`) for modern Android 8.0+ (API 26) Adaptive Icons.
These are generated and kept in `app/src/main/res/drawable/` and linked via `app/src/main/res/mipmap-anydpi-v26/`.

## Generating Legacy PNGs
For older Android versions (API < 26), standard PNG files are required in the various `mipmap-*` folders.

If you need to update the basic shape of the icon:
1. Edit `app/src/main/assets/icon_source.svg`.
2. Edit `app/src/main/assets/playstore-icon.svg`.
3. Update the `VectorDrawable` XML files manually if using standard tools, OR use Android Studio's Vector Asset Studio.
4. Run `tools/export_icons.ps1` to automatically export all PNG densities.

### Requirements
- **Inkscape** is required for `export_icons.ps1` to work. Download it from [inkscape.org](https://inkscape.org/) and ensure `inkscape` is available in your system `PATH`.
