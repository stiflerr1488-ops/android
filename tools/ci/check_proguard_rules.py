#!/usr/bin/env python3
import pathlib
import re
import sys


ROOT = pathlib.Path(__file__).resolve().parents[2]
RULES = ROOT / "app" / "proguard-rules.pro"


FORBIDDEN = [
    re.compile(r"-keep\s+class\s+com\.google\.firebase\.\*\*"),
    re.compile(r"-keep\s+class\s+com\.google\.android\.gms\.\*\*"),
]

REQUIRED = [
    re.compile(r"-keep\s+class\s+kotlin\.Metadata"),
    re.compile(r"-keepclassmembers\s+enum\s+\*"),
]


def main() -> int:
    text = RULES.read_text(encoding="utf-8", errors="replace")
    errors: list[str] = []

    for pattern in FORBIDDEN:
        m = pattern.search(text)
        if m:
            line = text.count("\n", 0, m.start()) + 1
            errors.append(
                f"::error file={RULES.as_posix()},line={line}::Forbidden broad keep rule detected: {pattern.pattern}",
            )

    for pattern in REQUIRED:
        if not pattern.search(text):
            errors.append(
                f"::error file={RULES.as_posix()}::Missing expected rule: {pattern.pattern}",
            )

    if errors:
        for err in errors:
            print(err)
        print("Proguard rule checks failed.")
        return 1

    print("Proguard rule checks passed.")
    return 0


if __name__ == "__main__":
    sys.exit(main())

