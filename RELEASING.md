# Release Process

This document describes how to create a new release of the Requirements Management Tools.

## Prerequisites

- Write access to the repository
- Clean working directory (all changes committed)
- All tests passing locally

## Release Steps

### 1. Update Version Documentation

If needed, update version references in:
- `README.md` - CI/CD examples
- `CLAUDE.md` - Installation examples

### 2. Create and Push Tag

```bash
# Ensure you're on main and up to date
git checkout main
git pull

# Create annotated tag (triggers release workflow)
git tag -a v1.0.0 -m "Release v1.0.0: Brief description of changes"

# Push tag to trigger GitHub Actions release workflow
git push origin v1.0.0
```

### 3. Monitor Release Workflow

1. Go to: https://github.com/rriehle/.req/actions
2. Watch the "Release" workflow run
3. Verify it completes successfully

### 4. Verify Release

1. Go to: https://github.com/rriehle/.req/releases
2. Verify the new release appears
3. Check that release includes:
   - `req-tools-vX.Y.Z.tar.gz` - Distribution tarball
   - `req-tools-vX.Y.Z.tar.gz.sha256` - Checksum file
   - `install.sh` - Installation script
   - Auto-generated release notes

### 5. Test Installation

Test the installation from the new release:

```bash
# In a temporary directory
cd $(mktemp -d)

# Test install script
curl -sL https://github.com/rriehle/.req/releases/download/v1.0.0/install.sh | bash

# Verify installation
~/.req/bin/req-validate --help

# Test functionality
cd /path/to/test/project
req-validate
req-search list
req-trace summary
```

### 6. Announce Release (Optional)

If appropriate, announce the release:
- Update project documentation
- Notify users/teams
- Post to relevant channels

## Version Numbering

Follow [Semantic Versioning](https://semver.org/):

- **MAJOR** (vX.0.0): Breaking changes, incompatible API changes
- **MINOR** (v1.X.0): New features, backwards-compatible
- **PATCH** (v1.0.X): Bug fixes, backwards-compatible

Examples:
- `v1.0.0` - Initial stable release
- `v1.1.0` - Added new search capabilities
- `v1.1.1` - Fixed validation bug
- `v2.0.0` - Changed metadata format (breaking)

## Troubleshooting

### Release Workflow Failed

1. Check workflow logs: https://github.com/rriehle/.req/actions
2. Common issues:
   - Missing files in tarball creation
   - Permission issues
   - Invalid release notes format

### Tag Already Exists

If you need to recreate a tag:

```bash
# Delete local tag
git tag -d v1.0.0

# Delete remote tag (careful!)
git push origin :refs/tags/v1.0.0

# Recreate and push
git tag -a v1.0.0 -m "Release v1.0.0: Description"
git push origin v1.0.0
```

**Note:** Only do this for unreleased tags. Never delete published release tags.

### Release Appears But Files Missing

1. Check workflow completed successfully
2. Verify all steps in workflow ran
3. Check `files:` section in `.github/workflows/release.yml`

## Pre-release / Beta Releases

For testing before stable release:

```bash
# Create pre-release tag
git tag -a v1.0.0-beta.1 -m "Beta release for testing"
git push origin v1.0.0-beta.1
```

Mark as pre-release in GitHub:
1. Go to release page
2. Edit release
3. Check "This is a pre-release"
4. Save

## Hotfix Releases

For urgent fixes:

```bash
# Create from main or release branch
git checkout main  # or release/v1.0
git pull

# Apply fix
git commit -m "Fix critical issue"

# Tag patch version
git tag -a v1.0.1 -m "Hotfix: Brief description"
git push origin main v1.0.1
```

## Release Checklist

Before tagging:
- [ ] All tests passing
- [ ] Documentation updated
- [ ] CHANGELOG updated (if maintained)
- [ ] Version numbers updated in examples
- [ ] Clean working directory

After release:
- [ ] Release workflow completed successfully
- [ ] All artifacts present in release
- [ ] Installation tested from release
- [ ] Tools function correctly
- [ ] Users notified (if applicable)
