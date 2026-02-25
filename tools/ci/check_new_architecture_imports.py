#!/usr/bin/env python3
from __future__ import annotations

import re
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]

FIREBASE_IMPORT = re.compile(r"^\s*import\s+com\.google\.firebase\.")
LEGACY_IMPORT = re.compile(r"^\s*import\s+com\.example\.teamcompass\.")


def iter_files(base: Path):
    if not base.exists():
        return []
    return [p for p in base.rglob("*") if p.suffix in {".kt", ".java"} and p.is_file()]


def collect_core_feature_files() -> list[Path]:
    files: list[Path] = []

    core_root = ROOT / "core"
    if core_root.exists():
        for child in core_root.iterdir():
            if not child.is_dir():
                continue
            if child.name in {"src", "build"}:
                # Legacy :core module lives under core/src and is excluded.
                continue
            files.extend(iter_files(child / "src"))

    feature_root = ROOT / "feature"
    if feature_root.exists():
        for child in feature_root.iterdir():
            if not child.is_dir():
                continue
            files.extend(iter_files(child / "src"))
            for grandchild in child.iterdir():
                if grandchild.is_dir():
                    files.extend(iter_files(grandchild / "src"))

    return sorted(set(files))


def collect_infra_files() -> list[Path]:
    infra_root = ROOT / "infra"
    files: list[Path] = []
    if infra_root.exists():
        for child in infra_root.iterdir():
            if child.is_dir():
                files.extend(iter_files(child / "src"))
    return sorted(set(files))


def scan() -> int:
    core_feature_files = collect_core_feature_files()
    infra_files = collect_infra_files()

    violations: list[str] = []

    for path in core_feature_files:
        text = path.read_text(encoding="utf-8", errors="ignore")
        for i, line in enumerate(text.splitlines(), start=1):
            if FIREBASE_IMPORT.search(line):
                violations.append(
                    f"{path.relative_to(ROOT)}:{i}: Firebase import is forbidden in core/feature modules",
                )
            if LEGACY_IMPORT.search(line):
                violations.append(
                    f"{path.relative_to(ROOT)}:{i}: Legacy TeamCompass import is forbidden in new core/feature modules",
                )

    for path in infra_files:
        text = path.read_text(encoding="utf-8", errors="ignore")
        for i, line in enumerate(text.splitlines(), start=1):
            if LEGACY_IMPORT.search(line):
                violations.append(
                    f"{path.relative_to(ROOT)}:{i}: Legacy TeamCompass import is forbidden in infra adapters",
                )

    if violations:
        print("New architecture import guard failed:")
        for item in violations:
            print(f" - {item}")
        return 1

    print("New architecture import guard passed.")
    return 0


if __name__ == "__main__":
    raise SystemExit(scan())

