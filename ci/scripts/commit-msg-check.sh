#!/bin/bash
# Copyright (c) 2026 Qualcomm Innovation Center, Inc. All rights reserved.
# SPDX-License-Identifier: MIT
#
# Mirror the CI Commit Message Check locally so violations surface before
# pushing. Runs on the host; git is the only dependency. Rules and messages
# track qualcomm/commit-msg-check-action (the QC Preflight commit-msg job),
# check_commits.py at 79e81656d5a4.
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

# Trailers recognised anywhere in the message, not only in the trailing block.
has_trailer_prefix() {
    case "$(printf '%s' "$1" | tr '[:upper:]' '[:lower:]')" in
        signed-off-by:*|co-authored-by:*|co-developed-by:*|\
        reviewed-by:*|acked-by:*|tested-by:*|assisted-by:*) return 0 ;;
    esac
    return 1
}

# A "Token: value" line, as git-interpret-trailers(1) defines a trailer.
is_trailer() {
    [[ "$1" =~ ^[A-Za-z][A-Za-z0-9-]*:([[:space:]]|$) ]] && return 0
    has_trailer_prefix "$1"
}

# Index of the first line of the trailing trailer block, or n if there is
# none. The block is contiguous and ends the message: a message that ends
# in "(cherry picked from commit ...)" has no block, while a Signed-off-by
# below that line opens one with no blank line above it.
trailer_block_start() {
    local end=$n start
    while [ "$end" -gt 0 ] && [ -z "$(strip "${lines[end - 1]}")" ]; do
        end=$((end - 1))
    done
    start=$end
    while [ "$start" -gt 0 ]; do
        local line="${lines[start - 1]}"
        if is_trailer "$line" ||
           { [ "$start" -lt "$end" ] && [ -n "$(strip "$line")" ] &&
             [[ "${line:0:1}" =~ [[:space:]] ]]; }; then
            start=$((start - 1))
        else
            break
        fi
    done
    # A folded continuation line cannot open the block.
    while [ "$start" -lt "$end" ] && ! is_trailer "${lines[start]}"; do
        start=$((start + 1))
    done
    if [ "$start" -lt "$end" ]; then echo "$start"; else echo "$n"; fi
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

    # Trailers are not body text: they are exempt from the line length limit.
    tstart=$(trailer_block_start)
    body=()
    for (( i = 2; i < tstart; i++ )); do
        s="$(strip "${lines[i]}")"
        if [ -n "$s" ] && ! has_trailer_prefix "${lines[i]}"; then
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

    if [ "${#body[@]}" -gt 0 ] && [ "$tstart" -gt 0 ] && [ "$tstart" -lt "$n" ]; then
        [ -n "$(strip "${lines[tstart - 1]}")" ] &&
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
