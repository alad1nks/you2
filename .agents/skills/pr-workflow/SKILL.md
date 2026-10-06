---
name: pr-workflow
description: Start a new task in you2 from the latest main branch and prepare a pull request with the completed changes. Use when starting a new task that modifies files or when asked to create a PR; continue an existing task on its working branch.
---

# Working with branches and pull requests

## Starting a new task

Before modifying files, check `git status --short --branch` and the current branch.
This repository uses `main` as its main branch and `origin` as its remote.

If the working directory is clean, switch to the main branch and update it:

```bash
git switch main
git pull --ff-only origin main
```

Create a separate working branch named for the task only after the update succeeds.
If switching branches or fast-forwarding fails, investigate the cause. Do not discard local commits
or replace `--ff-only` with a forced update or an automatic merge.

If there are uncommitted changes, first determine whether they belong to the current task.
Preserve them: do not automatically run `reset --hard`, `clean`, or `stash`, or discard files.
If it is unclear which task the changes belong to, ask the user how to proceed.

## Continuing a task and creating a PR

Continue an existing task on its working branch. A request to create a PR after changes have been made
is not a new task: do not switch to `main` over the prepared work.
If changes have already been made on `main`, move them to a new working branch with `git switch -c`.

Before committing, review the diff and run checks appropriate to the changes.
Include only files belonging to the task in the commit. Before publishing, get the latest base with
`git fetch origin main` and review the diff against `origin/main`.

Create a PR only when the user requests one or has already authorized it for this task.
Push the working branch to `origin` and open a PR targeting `main`. Describe the resulting changes,
checks performed, and limitations. When using `gh` with a multiline description,
use `--body-file`. Verify that the PR uses the correct source branch and contains only changes for this task;
return its link to the user. Do not merge the PR without a separate instruction.
