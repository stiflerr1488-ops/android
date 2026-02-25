#!/usr/bin/env python3
import pathlib
import re
import sys


ROOT = pathlib.Path(__file__).resolve().parents[2]
APP_GRADLE = ROOT / "app" / "build.gradle.kts"
MAIN_MANIFEST = ROOT / "app" / "src" / "main" / "AndroidManifest.xml"


def main() -> int:
    errors: list[str] = []

    gradle_text = APP_GRADLE.read_text(encoding="utf-8", errors="replace")
    if not re.search(r"buildTypes\s*\{", gradle_text):
        errors.append(f"::error file={APP_GRADLE.as_posix()}::Missing buildTypes block")
    if not re.search(r"release\s*\{[\s\S]*isMinifyEnabled\s*=\s*true", gradle_text):
        errors.append(
            f"::error file={APP_GRADLE.as_posix()}::release must set isMinifyEnabled = true",
        )
    if not re.search(r"release\s*\{[\s\S]*proguardFiles\s*\(", gradle_text):
        errors.append(
            f"::error file={APP_GRADLE.as_posix()}::release must configure proguardFiles(...)",
        )

    manifest_text = MAIN_MANIFEST.read_text(encoding="utf-8", errors="replace")
    if not re.search(
        r'<service[\s\S]*android:name="\.TrackingService"[\s\S]*android:exported="false"',
        manifest_text,
    ):
        errors.append(
            f"::error file={MAIN_MANIFEST.as_posix()}::TrackingService must stay android:exported=\"false\"",
        )

    if errors:
        for e in errors:
            print(e)
        print("Release hardening checks failed.")
        return 1

    print("Release hardening checks passed.")
    return 0


if __name__ == "__main__":
    sys.exit(main())

