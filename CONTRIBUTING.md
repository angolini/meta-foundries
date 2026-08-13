# Contributing to meta-foundries

Hi there!
We’re thrilled that you’d like to contribute to this project.
Your help is essential for keeping this project great and for making it better.

## Branching Strategy

In general, contributors should develop on branches based off of `main` and pull requests should be made against `main`.

## Commit messages

Keep each commit atomic — one logical change per commit, and keep the tree
buildable after each one.

Use a `component: summary of the change` subject, where `component` names the
recipe, directory, or file being touched and the summary concisely captures the
intent (not a file-by-file dump). For example:

- `ci: add yocto-check-layer wrapper`
- `README: list the layer maintainers`
- `recipe-name: upgrade vX.Y.Z -> vA.B.C`

Write a plain-English body that first explains the problem or reason for the
change, then uses the imperative mood ("add", "drop", "enable") to describe the
actions taken. Do not merely restate what the diff changes line by line.

**Line length: keep the subject and every body line to 72 characters or
fewer, and leave a blank line between the subject and the body.** CI enforces
this in the Commit Message Check (part of QC Preflight Checks), which fails the
whole PR on any over-length line. Check your commits locally before pushing
with `ci/scripts/commit-msg-check.sh` (see the checklist below).

## Submitting a pull request

1. Please read our [code of conduct](CODE-OF-CONDUCT.md) and [license](COPYING.MIT).
1. [Fork](https://github.com/foundriesio/meta-foundries/fork) and clone the repository.

    ```bash
    git clone https://github.com/<username>/meta-foundries.git
    ```

1. Create a new branch based on `main`:

    ```bash
    git checkout -b <my-branch-name> main
    ```

1. Create an upstream `remote` to make it easier to keep your branches up-to-date:

    ```bash
    git remote add upstream https://github.com/foundriesio/meta-foundries.git
    ```

1. Make your changes, add tests, and make sure the tests still pass.
1. Commit your changes using the [DCO](https://developercertificate.org/). You can attest to the DCO by commiting with the **-s** or **--signoff** options or manually adding the "Signed-off-by":

    ```bash
    git commit -s -m "Really useful commit message"`
    ```

1. After committing your changes on the topic branch, sync it with the upstream branch:

    ```bash
    git pull --rebase upstream main
    ```

1. Push to your fork.

    ```bash
    git push -u origin <my-branch-name>
    ```

    The `-u` is shorthand for `--set-upstream`. This will set up the tracking reference so subsequent runs of `git push` or `git pull` can omit the remote and branch.

1. [Submit a pull request](https://github.com/foundriesio/meta-foundries/pulls) from your branch to `main`.
1. Pat yourself on the back and wait for your pull request to be reviewed.

## Checks to run before submitting a pull request

Run the same checks CI runs from the repository root and make sure they pass:

- [ ] `ci/scripts/kas-container-shell-helper.sh ci/scripts/yocto-patchreview.sh`
- [ ] `ci/scripts/kas-container-shell-helper.sh ci/scripts/yocto-check-layer.sh`
- [ ] `ci/scripts/commit-msg-check.sh`
- [ ] `vale --config=docs/.vale.ini docs/`

See [Running the `ci/` Check Scripts](docs/ci-scripts.md) for what each check
does and how to run it.

## Security Analysis of Pull Requests

To maintain the security and integrity of this project, all pull requests from external contributors are automatically scanned using [Semgrep](https://github.com/semgrep/semgrep) to detect insecure coding patterns and potential security flaws.

**Static Analysis with Semgrep:**  We use Semgrep to perform lightweight, fast static analysis on every PR. This helps identify risky code patterns and logic flaws early in the development process.

**Contributor Responsibility:** If any issues are flagged, contributors are expected to resolve them before the PR can be merged.

**Continuous Improvement:** Our Semgrep ruleset evolves over time to reflect best practices and emerging security concerns.

By submitting a PR, you agree to participate in this process and help us keep the project secure for everyone.


Here are a few things you can do that will increase the likelihood of your pull request to be accepted:

- Keep your change as focused as possible.
  If you want to make multiple independent changes, please consider submitting them as separate pull requests.
- Write a [good commit message](https://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html).
- It's a good idea to arrange a discussion with other developers to ensure there is consensus on large features, architecture changes, and other core code changes. PR reviews will go much faster when there are no surprises.

## Documentation

To lint contributions under docs locally, [install vale](https://vale.sh/docs/vale-cli/installation/).
After vale is installed, navigate to `docs/` and run `vale sync`. 
To lint a file, run `vale <file-name>`.

For the full local workflow (style sync and linting the way CI does), see the
Vale section of [Running the `ci/` Check Scripts](docs/ci-scripts.md).
