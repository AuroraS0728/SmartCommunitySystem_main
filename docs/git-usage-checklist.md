# Git Usage Checklist

## Branch Naming

- Daily development branch: `dev`
- Integration branch: `develop`
- Stable release branch: `main`

- Feature branches:
  - `feature/activity-recommend`
  - `feature/security-miniapp-cn`
  - `feature/repair-priority-rule`

- Release branches:
  - `release/2026-05-15`
  - `release/v1.3.0`

- Hotfix branches:
  - `hotfix/smart-work-order-npe`
  - `hotfix/login-token-expire`

## Daily Feature Development

```bash
git checkout develop
git pull origin develop
git checkout -b feature/<module-name>
```

Development is done in `feature/<module-name>`.

After local verification:

```bash
git add .
git commit -m "feat(<module>): <summary>"
git push -u origin feature/<module-name>
```

Merge into `dev` for daily aggregation:

```bash
git checkout dev
git pull origin dev
git merge --no-ff feature/<module-name>
git push origin dev
```

After front-end/back-end joint testing and bug fixing are complete, promote to `develop`:

```bash
git checkout develop
git pull origin develop
git merge --no-ff dev
git push origin develop
```

## Release Preparation

Release preparation requires explicit confirmation first.

```bash
git checkout develop
git pull origin develop
git checkout -b release/<version-or-date>
git push -u origin release/<version-or-date>
```

Rules:

- Do not write new code on `release/*`.
- Only perform verification, packaging, and deployment preparation.
- If a release issue is found, fix it in `dev` or a dedicated `feature/*`, then merge back through `develop`.

After release verification passes:

```bash
git checkout main
git pull origin main
git merge --no-ff release/<version-or-date>
git push origin main
```

Only after this step should the code be deployed to server.

## Hotfix Process

When production has an urgent issue:

```bash
git checkout main
git pull origin main
git checkout -b hotfix/<issue-name>
```

After the fix:

```bash
git add .
git commit -m "fix(<module>): <summary>"
git push -u origin hotfix/<issue-name>
```

Merge back to `main`:

```bash
git checkout main
git pull origin main
git merge --no-ff hotfix/<issue-name>
git push origin main
```

Then back-merge into `develop`:

```bash
git checkout develop
git pull origin develop
git merge --no-ff hotfix/<issue-name>
git push origin develop
```

If `dev` is still active for current iteration, merge there too:

```bash
git checkout dev
git pull origin dev
git merge --no-ff hotfix/<issue-name>
git push origin dev
```

## Commit Message Conventions

- Feature: `feat(<module>): <summary>`
- Fix: `fix(<module>): <summary>`
- Refactor: `refactor(<module>): <summary>`
- Docs: `docs(<module>): <summary>`
- Chore: `chore(<module>): <summary>`
- Test: `test(<module>): <summary>`

Examples:

- `feat(activity): boost recommended events for owner portraits`
- `fix(repair): handle null assignee in smart work order page`
- `docs(git): add branch workflow checklist`

## Pre-Push Checklist

Before pushing a `feature/*` or `dev` change:

1. Confirm current branch is correct.
2. Run minimal compile/test for changed modules.
3. Check `git diff --stat` and `git status`.
4. Avoid committing local passwords, dumps, IDE files, or generated packages.
5. Confirm `main` is untouched unless this is a release or hotfix.

## Server Deployment Rule

- No direct deployment from `dev`, `develop`, or `feature/*`.
- `release/*` is created only after confirmation.
- Deployment is performed only after the target code is merged into `main`.
- If deployment has not been explicitly confirmed, stop at `develop` or `release/*`.
