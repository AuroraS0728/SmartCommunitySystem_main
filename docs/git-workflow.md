# Git Branch Workflow

## Branch Roles

- `main`
  - Stable production branch.
  - Only accepts code that has been confirmed and is ready for release.
  - Server deployment must come from code already merged into `main`.

- `develop`
  - Integration branch for the development environment.
  - Keeps the latest completed features and bug fixes.
  - Used for front-end/back-end joint testing.

- `dev`
  - Daily development branch.
  - Used to aggregate day-to-day development work before promoting to `develop`.

- `feature/<module-name>`
  - New feature branch.
  - Must be created from `develop`.
  - Examples: `feature/recommend-activity`, `feature/security-miniapp-cn`

- `release/<version-or-date>`
  - Pre-release branch.
  - Created from `develop` only after explicit confirmation.
  - No direct code modification is allowed on the release branch.
  - Used for release verification, packaging, and deployment preparation only.

- `hotfix/<issue-name>`
  - Emergency bug-fix branch.
  - Created from `main` when production has a defect.
  - After verification, merge back into `main` and `develop`.

## Standard Flow

### 1. Daily Development

1. Create a feature branch from `develop`.
2. Complete development and self-test in `feature/<module-name>`.
3. Merge the feature branch into `dev`.
4. After related fixes are completed, merge `dev` into `develop`.

### 2. Release Flow

1. When `develop` is ready, ask for confirmation first.
2. After confirmation, create `release/<version-or-date>` from `develop`.
3. Do not change code directly on `release/<version-or-date>`.
4. Use the release branch for final verification only.
5. After verification passes, merge into `main` and deploy to server.

### 3. Production Hotfix

1. Create `hotfix/<issue-name>` from `main`.
2. Fix the problem and complete verification.
3. Merge back into `main`.
4. Merge the same hotfix into `develop` to avoid branch drift.

## Merge Rules

- `main` only receives validated release code or verified production hotfixes.
- `develop` must always stay usable for integration testing.
- `feature/*` must not be created from `main`.
- `release/*` must not become a normal development branch.
- Server deployment requires explicit confirmation before entering the release flow.

## Recommended Commands

```bash
# create a feature branch
git checkout develop
git pull
git checkout -b feature/<module-name>

# merge feature into dev
git checkout dev
git merge --no-ff feature/<module-name>

# promote dev into develop
git checkout develop
git merge --no-ff dev

# create a release branch after confirmation
git checkout develop
git pull
git checkout -b release/<version-or-date>

# hotfix from production
git checkout main
git pull
git checkout -b hotfix/<issue-name>
```
