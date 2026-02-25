#!/usr/bin/env python3
import pathlib
import re
import sys


ROOT = pathlib.Path(__file__).resolve().parents[2]
MAIN_KOTLIN = ROOT / "app" / "src" / "main" / "kotlin"
DEBUG_MANIFEST = ROOT / "app" / "src" / "debug" / "AndroidManifest.xml"


FORBIDDEN_VIEWMODEL_PATTERNS = [
    re.compile(r"application\s+as\?\s+android\.app\.Activity"),
    re.compile(r"application\s+as\s+android\.app\.Activity"),
    re.compile(r"getApplication\s*<\s*android\.app\.Activity\s*>"),
]


def check_viewmodels(errors: list[str]) -> None:
    for vm in MAIN_KOTLIN.rglob("*ViewModel.kt"):
        text = vm.read_text(encoding="utf-8", errors="replace")
        for pattern in FORBIDDEN_VIEWMODEL_PATTERNS:
            m = pattern.search(text)
            if not m:
                continue
            line = text.count("\n", 0, m.start()) + 1
            errors.append(
                f"::error file={vm.as_posix()},line={line}::Forbidden Application->Activity cast in ViewModel",
            )


def check_debug_manifest(errors: list[str]) -> None:
    if not DEBUG_MANIFEST.exists():
        errors.append(
            f"::error file={DEBUG_MANIFEST.as_posix()}::Missing debug AndroidManifest.xml",
        )
        return

    text = DEBUG_MANIFEST.read_text(encoding="utf-8", errors="replace")
    if 'android:usesCleartextTraffic="true"' in text:
        errors.append(
            f"::error file={DEBUG_MANIFEST.as_posix()}::Debug manifest must not enable global cleartext traffic",
        )
    if 'android:networkSecurityConfig="@xml/network_security_config"' not in text:
        errors.append(
            f"::error file={DEBUG_MANIFEST.as_posix()}::Debug manifest must declare networkSecurityConfig",
        )


def main() -> int:
    errors: list[str] = []
    check_viewmodels(errors)
    check_debug_manifest(errors)

    if errors:
        for err in errors:
            print(err)
        print("Android guard checks failed.")
        return 1

    print("Android guard checks passed.")
    return 0


if __name__ == "__main__":
    sys.exit(main())

