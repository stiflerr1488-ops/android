#!/usr/bin/env python3
"""Guardrail against encoding regressions (UTF-8/BOM/mojibake)."""

from __future__ import annotations

import argparse
import pathlib
import re
import subprocess
import sys
from dataclasses import dataclass


TEXT_EXTENSIONS = {
    ".kt",
    ".kts",
    ".java",
    ".xml",
    ".json",
    ".md",
    ".txt",
    ".yml",
    ".yaml",
    ".properties",
    ".gradle",
    ".sh",
    ".bat",
    ".cmd",
    ".ps1",
}
TEXT_BASENAMES = {
    ".editorconfig",
    ".gitattributes",
    ".gitignore",
}

TOKEN_RE = re.compile(r"[A-Za-z\u00C0-\u00FF\u0400-\u04FF]{6,}")
SUSPICIOUS_CORE = {"\u0420", "\u0421", "\u00D0", "\u00D1"}  # Р, С, Ð, Ñ
SUSPICIOUS_SUBSTRINGS = (
    "\u0432\u0402",  # вЂ
    "\uFFFD",  # replacement character
)


@dataclass
class FileIssue:
    path: str
    line: int
    severity: str
    reason: str
    sample: str


def run_git(args: list[str]) -> str:
    proc = subprocess.run(
        ["git", *args],
        check=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
    )
    return proc.stdout


def staged_paths() -> list[str]:
    out = run_git(["diff", "--cached", "--name-only", "--diff-filter=ACMR"])
    return [p for p in out.splitlines() if p.strip()]


def tracked_paths() -> list[str]:
    out = run_git(["ls-files"])
    return [p for p in out.splitlines() if p.strip()]


def is_text_candidate(path: str) -> bool:
    p = pathlib.Path(path)
    ext = p.suffix.lower()
    return ext in TEXT_EXTENSIONS or p.name in TEXT_BASENAMES


def read_staged_blob(path: str) -> bytes:
    proc = subprocess.run(
        ["git", "show", f":{path}"],
        check=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )
    return proc.stdout


def read_worktree_file(path: str) -> bytes:
    return pathlib.Path(path).read_bytes()


def strip_bom_in_worktree(path: str) -> bool:
    file_path = pathlib.Path(path)
    data = file_path.read_bytes()
    if not data.startswith(b"\xEF\xBB\xBF"):
        return False
    file_path.write_bytes(data[3:])
    return True


def is_likely_binary(data: bytes) -> bool:
    return b"\x00" in data


def line_has_mojibake(line: str) -> bool:
    if any(marker in line for marker in SUSPICIOUS_SUBSTRINGS):
        return True

    for token in TOKEN_RE.findall(line):
        suspicious = sum(1 for ch in token if ch in SUSPICIOUS_CORE)
        if suspicious >= 3 and suspicious * 2 >= len(token):
            return True

    return False


def collect_issues(path: str, data: bytes) -> list[FileIssue]:
    issues: list[FileIssue] = []

    if data.startswith(b"\xEF\xBB\xBF"):
        issues.append(
            FileIssue(
                path=path,
                line=1,
                severity="warning",
                reason="UTF-8 BOM detected",
                sample="<BOM>",
            )
        )

    try:
        text = data.decode("utf-8")
    except UnicodeDecodeError as err:
        issues.append(
            FileIssue(
                path=path,
                line=1,
                severity="error",
                reason=f"File is not valid UTF-8 ({err})",
                sample="<binary/non-utf8>",
            )
        )
        return issues

    for idx, line in enumerate(text.splitlines(), start=1):
        if line_has_mojibake(line):
            issues.append(
                FileIssue(
                    path=path,
                    line=idx,
                    severity="error",
                    reason="Suspicious mojibake pattern",
                    sample=line.strip()[:160],
                )
            )

    return issues


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--staged",
        action="store_true",
        help="Check staged files only.",
    )
    parser.add_argument(
        "--all",
        action="store_true",
        help="Check all tracked text files in repository.",
    )
    parser.add_argument(
        "--fix-bom",
        action="store_true",
        help="Strip UTF-8 BOM from worktree files before checking.",
    )
    parser.add_argument(
        "--strict-bom",
        action="store_true",
        help="Treat UTF-8 BOM as an error.",
    )
    args = parser.parse_args()

    if args.staged and args.all:
        print("Use only one of --staged or --all", file=sys.stderr)
        return 2

    if args.all:
        paths = tracked_paths()
        reader = read_worktree_file
    else:
        # Default to staged mode to match pre-commit workflow.
        paths = staged_paths()
        reader = read_staged_blob

    if args.fix_bom and reader is not read_worktree_file:
        print("--fix-bom requires --all (worktree mode).", file=sys.stderr)
        return 2

    targets = [p for p in paths if is_text_candidate(p)]
    if not targets:
        return 0

    if args.fix_bom:
        fixed = [path for path in targets if strip_bom_in_worktree(path)]
        if fixed:
            print(f"Stripped UTF-8 BOM from {len(fixed)} file(s).")
            for path in fixed:
                print(f"- {path}")

    all_issues: list[FileIssue] = []
    for path in targets:
        try:
            data = reader(path)
        except FileNotFoundError:
            continue
        except subprocess.CalledProcessError:
            continue
        if is_likely_binary(data):
            continue
        all_issues.extend(collect_issues(path, data))

    if args.strict_bom:
        for issue in all_issues:
            if issue.reason == "UTF-8 BOM detected":
                issue.severity = "error"

    if not all_issues:
        return 0

    errors = [issue for issue in all_issues if issue.severity == "error"]
    warnings = [issue for issue in all_issues if issue.severity != "error"]

    if errors:
        print("Encoding guard failed. Detected potential mojibake/encoding issues:")
    elif warnings:
        print("Encoding guard warnings:")

    for issue in all_issues[:100]:
        prefix = "ERROR" if issue.severity == "error" else "WARN "
        print(f"- [{prefix}] {issue.path}:{issue.line}: {issue.reason}")
        print(f"  {issue.sample}")

    if errors:
        print(
            "\nFix encoding and re-run:\n"
            "  python tools/check_mojibake.py --staged",
            file=sys.stderr,
        )
        return 1

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
