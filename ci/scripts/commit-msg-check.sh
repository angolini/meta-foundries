#!/bin/bash
# Copyright (c) 2026 Qualcomm Innovation Center, Inc. All rights reserved.
# SPDX-License-Identifier: MIT
#
# Mirror the CI Commit Message Check locally so violations surface before
# pushing. Runs on the host; git is the only dependency. Rules and messages
# track qualcomm/commit-msg-check-action (the QC Preflight commit-msg job).
#
# Usage: ci/scripts/commit-msg-check.sh [<range>]   # default: main..HEAD

set -u

LIMIT=72
RANGE="${1:-main..HEAD}"

strip() {
    local s="$1"
    s="${s#"${s%%[![:space:]]*}"}"
    s="${s%"${s##*[![:space:]]}"}"
    printf '%s' "$s"
}

is_trailer() {
    case "$(printf '%s' "$1" | tr '[:upper:]' '[:lower:]')" in
        signed-off-by:*|co-authored-by:*|co-developed-by:*|\
        reviewed-by:*|acked-by:*|tested-by:*) return 0 ;;
    esac
    return 1
}

if ! commits="$(git rev-list --no-merges --reverse "$RANGE" 2>&1)"; then
    echo "Error: could not resolve range '$RANGE':" >&2
    echo "$commits" >&2
    exit 1
fi
if [ -z "$commits" ]; then
    echo "No commits in range '$RANGE'; nothing to check."
    exit 0
fi

fail=0
for sha in $commits; do
    short="$(git rev-parse --short "$sha")"
    mapfile -t lines < <(git log -1 --format=%B "$sha")
    n=${#lines[@]}
    errors=()

    subject="${lines[0]:-}"
    [ -z "$(strip "$subject")" ] && errors+=("Commit message is missing subject!")
    [ "${#subject}" -gt "$LIMIT" ] && errors+=("Subject exceeds $LIMIT characters!")

    if [ "$n" -gt 1 ] && [ -n "$(strip "${lines[1]}")" ]; then
        errors+=("Subject and body must be separated by a blank line")
    fi

    body=()
    for (( i = 1; i < n; i++ )); do
        s="$(strip "${lines[i]}")"
        if [ -n "$s" ] && ! is_trailer "${lines[i]}"; then
            body+=("$s")
        fi
    done
    if [ "${#body[@]}" -eq 0 ]; then
        errors+=("Commit message is missing a body!")
    else
        for l in "${body[@]}"; do
            [ "${#l}" -gt "$LIMIT" ] && errors+=("Line exceeds $LIMIT characters: $l")
        done
    fi

    first_trailer=-1
    for (( i = 0; i < n; i++ )); do
        if is_trailer "${lines[i]}"; then first_trailer=$i; break; fi
    done
    if [ "${#body[@]}" -gt 0 ] && [ "$first_trailer" -gt 0 ]; then
        [ -n "$(strip "${lines[first_trailer - 1]}")" ] &&
            errors+=("Body and trailers must be separated by a blank line")
    fi

    if [ "${#errors[@]}" -eq 0 ]; then
        echo "$short: ok"
    else
        fail=1
        for e in "${errors[@]}"; do
            echo "$short: $e"
        done
    fi
done

exit $fail
