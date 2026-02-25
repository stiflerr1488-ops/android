#!/usr/bin/env python3
import pathlib
import re
import sys


ROOT = pathlib.Path(__file__).resolve().parents[2]
SRC = ROOT / "app" / "src" / "main" / "kotlin"

# Matches PendingIntent.getActivity/getService/getBroadcast/getForegroundService(...)
CALL_RE = re.compile(
    r"PendingIntent\.(getActivity|getService|getBroadcast|getForegroundService)\s*\((.*?)\)",
    re.DOTALL,
)


def main() -> int:
    failures = []
    for kt in SRC.rglob("*.kt"):
        text = kt.read_text(encoding="utf-8", errors="replace")
        for match in CALL_RE.finditer(text):
            args = match.group(2)
            if "FLAG_IMMUTABLE" in args or "FLAG_MUTABLE" in args:
                continue

            line = text.count("\n", 0, match.start()) + 1
            failures.append((kt, line))

    if failures:
        for path, line in failures:
            print(
                f"::error file={path.as_posix()},line={line}::PendingIntent missing FLAG_IMMUTABLE/FLAG_MUTABLE",
            )
        print("PendingIntent flag checks failed.")
        return 1

    print("PendingIntent flag checks passed.")
    return 0


if __name__ == "__main__":
    sys.exit(main())

