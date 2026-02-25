#!/usr/bin/env python3
import pathlib
import re
import subprocess
import sys


ROOT = pathlib.Path(__file__).resolve().parents[2]

# High-confidence secret patterns only.
PATTERNS = [
    ("Google API key", re.compile(r"AIza[0-9A-Za-z_-]{35}")),
    (
        "Private key block",
        re.compile(r"-----BEGIN (?:RSA |EC |DSA )?PRIVATE KEY-----"),
    ),
    ("Slack bot token", re.compile(r"xoxb-[0-9A-Za-z-]{20,}")),
]

ALLOWLIST_FILES = {
    "app/google-services.json.example",
}


def tracked_files() -> list[pathlib.Path]:
    out = subprocess.check_output(
        ["git", "ls-files"],
        cwd=ROOT,
        text=True,
        encoding="utf-8",
    )
    files = []
    for rel in out.splitlines():
        rel = rel.strip()
        if not rel:
            continue
        files.append(ROOT / rel)
    return files


def is_probably_binary(path: pathlib.Path) -> bool:
    return path.suffix.lower() in {
        ".png",
        ".jpg",
        ".jpeg",
        ".gif",
        ".webp",
        ".mp4",
        ".pdf",
        ".jar",
        ".keystore",
        ".jks",
        ".aab",
        ".apk",
    }


def main() -> int:
    errors: list[str] = []
    for path in tracked_files():
        rel = path.relative_to(ROOT).as_posix()
        if rel in ALLOWLIST_FILES:
            continue
        if is_probably_binary(path):
            continue
        if not path.exists():
            continue

        text = path.read_text(encoding="utf-8", errors="replace")
        lines = text.splitlines()
        for i, line in enumerate(lines, start=1):
            if "YOUR_WEB_API_KEY" in line:
                continue
            for name, pattern in PATTERNS:
                if pattern.search(line):
                    errors.append(
                        f"::error file={rel},line={i}::{name} candidate detected",
                    )

    if errors:
        for err in errors:
            print(err)
        print("Secret scan failed.")
        return 1

    print("Secret scan passed.")
    return 0


if __name__ == "__main__":
    sys.exit(main())

