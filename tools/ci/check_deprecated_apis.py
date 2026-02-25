#!/usr/bin/env python3
import pathlib
import re
import sys


ROOT = pathlib.Path(__file__).resolve().parents[2]
SRC = ROOT / "app" / "src" / "main" / "kotlin"

FORBIDDEN_PATTERNS = [
    (
        "BluetoothAdapter.getDefaultAdapter() is deprecated; use BluetoothManager.adapter",
        re.compile(r"BluetoothAdapter\s*\.\s*getDefaultAdapter\s*\("),
    ),
    (
        "WindowManager.defaultDisplay is deprecated; use DisplayManager/current Window context",
        re.compile(r"\.defaultDisplay\b"),
    ),
]


def main() -> int:
    errors: list[str] = []

    for kt in SRC.rglob("*.kt"):
        text = kt.read_text(encoding="utf-8", errors="replace")
        for message, pattern in FORBIDDEN_PATTERNS:
            for match in pattern.finditer(text):
                line = text.count("\n", 0, match.start()) + 1
                errors.append(f"::error file={kt.as_posix()},line={line}::{message}")

    if errors:
        for err in errors:
            print(err)
        print("Deprecated API checks failed.")
        return 1

    print("Deprecated API checks passed.")
    return 0


if __name__ == "__main__":
    sys.exit(main())

