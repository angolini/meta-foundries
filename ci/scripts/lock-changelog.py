#!/usr/bin/env python3
# Copyright (c) 2026 Qualcomm Innovation Center, Inc. All rights reserved.
# SPDX-License-Identifier: MIT
#
# Write, in Markdown, what changed in each pinned repo between two sets
# of kas dumps: the one taken with the committed lockfiles and the one
# taken after relocking. Each dump is the output of
# "kas dump --format json <target>.yml", one file per target.
#
# Usage: lock-changelog.py OLD_DIR NEW_DIR

import json
import re
import subprocess
import sys
import tempfile
from pathlib import Path

# A pull request body holds at most 65536 characters. Six repos at this
# many lines each stay well below it.
MAX_COMMITS = 100
MAX_SUBJECT = 72
MAX_BODY = 60000
# A server that stops answering must not hold the job for hours.
CLONE_TIMEOUT = 600


def load(dump_dir):
    """Map each repo name to (url, branch, commit) across all dumps."""
    repos = {}
    for dump in sorted(Path(dump_dir).glob("*.json")):
        config = json.loads(dump.read_text())
        pins = (config.get("overrides") or {}).get("repos") or {}
        for name, repo in (config.get("repos") or {}).items():
            repo = repo or {}
            if not repo.get("url"):
                continue
            commit = (pins.get(name) or {}).get("commit") or repo.get("commit")
            repos[name] = (repo["url"], repo.get("branch"), commit)
    return repos


def git(gitdir, *args):
    return subprocess.run(["git", "--git-dir", gitdir, *args],
                          capture_output=True, text=True)


def commits(work, name, url, branch, old, new):
    """Return (log lines, None) or (None, reason it cannot be listed)."""
    gitdir = str(Path(work) / f"{name}.git")
    # Only commits are needed, so a treeless clone keeps it small.
    args = ["clone", "--quiet", "--bare", "--filter=tree:0"]
    if branch:
        args += ["--single-branch", "--branch", branch]
    try:
        clone = subprocess.run(["git", *args, url, gitdir],
                               capture_output=True, text=True,
                               timeout=CLONE_TIMEOUT)
    except subprocess.TimeoutExpired:
        return None, f"timed out cloning {url}"
    if clone.returncode:
        return None, f"could not clone {url}"
    for sha in (old, new):
        if git(gitdir, "cat-file", "-e", f"{sha}^{{commit}}").returncode:
            return None, f"{sha[:12]} is not on {branch or 'the default branch'}"
    if git(gitdir, "merge-base", "--is-ancestor", old, new).returncode:
        return None, "the new pin does not descend from the old one"
    log = git(gitdir, "log", "--no-merges", "--format=%h %s", f"{old}..{new}")
    return log.stdout.splitlines(), None


def section(name, old, new, lines, problem, with_log):
    head = f"`{name}` `{old[:12]}..{new[:12]}`"
    if problem:
        return f"- {head}: **{problem}**, check by hand\n"
    out = f"- {head}: {len(lines)} commit{'' if len(lines) == 1 else 's'}\n"
    if not with_log or not lines:
        return out
    shown = [l if len(l) <= MAX_SUBJECT else l[:MAX_SUBJECT - 3] + "..."
             for l in lines[:MAX_COMMITS]]
    if len(lines) > MAX_COMMITS:
        shown.append(f"... and {len(lines) - MAX_COMMITS} more")
    # A code block keeps @names and #numbers in the subjects from
    # turning into mentions and links. Its fence is longer than any run
    # of backticks in them, so no subject can close it.
    runs = [len(r) for l in shown for r in re.findall("`+", l)]
    fence = "`" * max(3, max(runs, default=0) + 1)
    return (out + f"  <details><summary>log</summary>\n\n  {fence}text\n"
            + "".join(f"  {l}\n" for l in shown)
            + f"  {fence}\n\n  </details>\n")


def main():
    if len(sys.argv) != 3:
        sys.exit(f"usage: {sys.argv[0]} OLD_DIR NEW_DIR")
    old, new = load(sys.argv[1]), load(sys.argv[2])

    # The clones take a few hundred MB, and a self-hosted runner keeps
    # whatever is left in /tmp.
    changed = []
    with tempfile.TemporaryDirectory() as work:
        for name, (url, branch, sha) in new.items():
            before = old.get(name, (None, None, None))[2]
            if before and sha and before != sha:
                changed.append((name, before, sha,
                                *commits(work, name, url, branch, before, sha)))

    if not changed:
        print("No pinned repo moved.")
        return
    for with_log in (True, False):
        body = "".join(section(*c, with_log) for c in changed)
        if len(body) <= MAX_BODY:
            break
    print("## What the new pins bring\n")
    print("Non-merge commits between the old and the new pin of each repo.\n")
    print(body, end="")


if __name__ == "__main__":
    main()
