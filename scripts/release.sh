#!/bin/bash
set -euo pipefail

BUMP_TYPE="${1:-}"
if [[ "$BUMP_TYPE" != "patch" && "$BUMP_TYPE" != "minor" && "$BUMP_TYPE" != "major" ]]; then
  echo "Usage: $0 <patch|minor|major>"
  exit 1
fi

# Require clean working tree
if [[ -n "$(git status --porcelain)" ]]; then
  echo "Error: working tree is not clean. Commit or stash changes first."
  exit 1
fi

# Require npm auth
if ! npm whoami &>/dev/null; then
  if [[ -z "${NPM_TOKEN:-}" ]]; then
    echo "Error: not logged in to npm. Run 'npm login' or set NPM_TOKEN."
    exit 1
  fi
fi

echo "Running lint..."
npm run lint

# Bump version in package.json only (no git commit/tag)
npm version "$BUMP_TYPE" --no-git-tag-version

NEW_VERSION=$(node -p "require('./package.json').version")
DATE=$(date +%Y-%m-%d)

echo "Bumped to v${NEW_VERSION}"

# Build changelog entries from commits since last tag
LAST_TAG=$(git describe --tags --abbrev=0 2>/dev/null || echo "")
if [[ -n "$LAST_TAG" ]]; then
  COMMITS=$(git log "${LAST_TAG}..HEAD" --oneline --no-merges)
else
  COMMITS=$(git log --oneline --no-merges)
fi

ENTRY="## [${NEW_VERSION}] - ${DATE}

${COMMITS}
"

if [[ -f CHANGELOG.md ]]; then
  EXISTING=$(cat CHANGELOG.md)
  printf '%s\n\n%s\n' "$ENTRY" "$EXISTING" > CHANGELOG.md
else
  printf '# Changelog\n\n%s\n' "$ENTRY" > CHANGELOG.md
fi

git add package.json CHANGELOG.md
git commit -m "chore: release v${NEW_VERSION}"
git tag "v${NEW_VERSION}"

echo "Pushing commit and tag..."
git push origin HEAD
git push origin "v${NEW_VERSION}"

echo "Publishing to npm..."
npm publish

echo "Released v${NEW_VERSION}"
