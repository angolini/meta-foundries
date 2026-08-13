# Running the `ci/` Check Scripts

Layer checks that run inside `kas-container`, matching how CI validates the
layer. Requires `kas-container` on your `PATH` (or set `KAS_CONTAINER`). Run
all checks from the repository root.

## Checks at a Glance

Run through these before opening a pull request:

- [ ] `ci/scripts/yocto-check-layer.sh` — runs `yocto-check-layer` against the layer
      (structure, signatures, and README compliance).
- [ ] `ci/scripts/yocto-patchreview.sh` — runs OpenEmbedded-Core patchreview over the
      layer's patches against the Yocto Project patch guidelines.
- [ ] `ci/scripts/commit-msg-check.sh` — checks each commit message against the CI
      Commit Message Check rules. A message needs a non-empty subject and
      body, blank-line separators, and 72-character lines. Runs on the host,
      not in `kas-container`.
- [ ] `vale --config=docs/.vale.ini docs/` — lints the `docs/` prose with Vale
      (runs on the host, not in `kas-container`).

`ci/scripts/kas-container-shell-helper.sh` is a helper, not a check: it runs a given
script inside the CI container and passes the repo and work directories to it.

## Running Each Check

### Layer Check and Patch Review

Wrap these with the helper so they run in the CI container:

```sh
ci/scripts/kas-container-shell-helper.sh ci/scripts/yocto-check-layer.sh
ci/scripts/kas-container-shell-helper.sh ci/scripts/yocto-patchreview.sh
```

The helper passes the repo and work directories to the script for you.

### Commit Message Check

`ci/scripts/commit-msg-check.sh` mirrors the CI Commit Message Check locally. It runs on
the host, needing only git and the working tree, not `kas-container`. It checks
every non-merge commit against the same rules. A commit needs a non-empty
subject and body, blank lines separating the subject, body, and trailers, and
72-character subject and body lines.

By default it checks the commits on the current branch that are not yet on
`main`. Pass a revision range to override:

```sh
ci/scripts/commit-msg-check.sh                  # main..HEAD (default)
ci/scripts/commit-msg-check.sh origin/main..HEAD
ci/scripts/commit-msg-check.sh HEAD~3..HEAD
```

It exits non-zero and lists the offending commit and line when a message
violates the rules, matching the CI failure.

### Linting Docs With Vale

Prose under `docs/` is linted with [Vale](https://vale.sh). CI runs it on every
pull request via `.github/workflows/lint-docs.yml`, reporting through reviewdog.
The `fail_on_error: false` setting keeps annotations non-fatal, yet reviewdog
still exits non-zero once it finds a result in the diff, so the check turns red.
Unlike the checks above, Vale runs on the host, not in `kas-container`.

Install it with the [Vale install guide](https://vale.sh/docs/install). The
config at `docs/.vale.ini` pulls the shared `Fio-docs` style; sync it once (and
again when the style updates):

```sh
vale --config=docs/.vale.ini sync
```

This downloads styles into `docs/.styles`. Then lint from the repository root:

```sh
vale --config=docs/.vale.ini docs/               # whole folder, like CI
vale --config=docs/.vale.ini docs/ci-scripts.md  # a single file
```

`MinAlertLevel` is `suggestion`, so expect suggestions, warnings, and errors.
CI reports any of them on the lines your pull request adds, so clear them
before pushing.
