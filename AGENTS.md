# Instructions for AI agents

Follow [.agents/skills/pr-workflow/SKILL.md](.agents/skills/pr-workflow/SKILL.md)
when starting a task or preparing a pull request.

## Visual changes and screenshot references

If a change affects visuals (including layout, text, fonts, assets, themes, or seeded data),
you must update screenshot references through CI **before creating a PR**:

1. Commit and push the changes to the task branch.
2. Run the **Generate screenshots** workflow (`generate-screenshots.yml`) for that branch:
   `gh workflow run generate-screenshots.yml --ref <branch>`.
3. Find the corresponding run with `gh run list --workflow generate-screenshots.yml --branch <branch>`
   and wait for it to succeed: `gh run watch <run-id> --exit-status`.
4. Pull the generated commit: `git pull --ff-only origin <branch>`.
5. Review the generated PNG differences, then create the PR. If the visual changes are wrong,
   fix them and repeat the generation and pull steps.

Generation and verification use Ubuntu 24.04, Amazon Corretto JDK 21, and Xvfb.
Do not commit references generated on macOS or Windows. The workflow rejects `main` and tags,
commits only `shared/src/jvmTest/screenshots/`, and does not force-push. If another commit lands
during generation, pull and rerun. An unchanged reference set produces no commit, but the run
must still succeed. Do not continue to PR creation after a failed generation.

The workflow must first exist on the default branch for GitHub to accept manual dispatches.
The initial CI migration therefore needs a subsequent feature-branch run to replace the old
Windows references. See [README.md](README.md#screenshot-regression-tests) for details.
