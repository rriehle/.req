# Requirements Management Tools

A general-purpose toolkit for managing software requirements across multiple projects, with built-in traceability to ADRs, RunNotes, code, and tests.

## Features

- **Config-driven**: Global defaults with project-specific overrides
- **Structured metadata**: EDN-based metadata with ISO 25010 taxonomy
- **Bidirectional traceability**: Link requirements to ADRs, RunNotes, code, and tests
- **Validation**: Automatic validation of requirement format and completeness
- **Search**: Tag-based, content, priority, status, and traceability search
- **Traceability analysis**: Gap analysis and coverage reporting
- **RFC 2119 semantics**: Clear MUST/SHALL/SHOULD/MAY prioritization

## Installation

### Quick Install (Recommended)

Using the installation script from the latest release:

```bash
curl -sL https://github.com/rriehle/.req/releases/latest/download/install.sh | bash
```

Then add to your PATH:
```bash
# For bash
echo 'export PATH="$HOME/.req/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc

# For zsh
echo 'export PATH="$HOME/.req/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

Verify installation:
```bash
req-validate --help
```

### Manual Install from Release

Download and extract a specific version:

```bash
VERSION=v1.0.0  # Replace with desired version
curl -sL https://github.com/rriehle/.req/releases/download/${VERSION}/req-tools-${VERSION}.tar.gz | tar xz
mv req-tools-${VERSION} ~/.req
```

Then add to PATH as shown above.

### Development Install

Clone the repository for development:

```bash
git clone https://github.com/rriehle/.req.git ~/.req
```

Then add to PATH as shown above.

## Configuration

### Global Config: `~/.req/config.edn`

Default configuration for all projects:

```edn
{:req
 {:path "doc/req"                           ; Default requirements location
  :template-dir "~/.req/template"           ; Template location
  :template-file "REQ-00000-template.md"    ; Template filename
  :excluded-files #{"README.md"}            ; Files to skip

  :metadata-schema
  {:required #{:req-id :type :category :priority :status :tag}
   :optional #{:updated :trace :nfr-taxonomy :acceptance-criteria}}

  :adr-integration
  {:enabled true
   :adr-dir "doc/adr"                       ; Where to search for ADRs
   :require-adr-refs false}                 ; ADR refs optional

  :runnote-integration
  {:enabled true
   :runnote-dir "runnote"}                  ; Where to search for RunNotes

  :priority-levels #{:must :shall :should :may}
  :req-types #{:functional :non-functional :constraint}}}
```

### Project Config: `<project>/.req.edn`

Override defaults per project:

```edn
{:req
 {:path "docs/requirements"                ; Custom requirements path
  :adr-integration
  {:require-adr-refs true}                 ; Require ADR references

  ;; Project-specific categories
  :categories #{:authentication :authorization :data-management
                :ui :api :integration :reporting}}}
```

## Usage

All commands automatically discover your project root (via git) and load the appropriate configuration.

### Validate Requirements

Check all requirements for format compliance and traceability:

```bash
# In project directory
cd ~/projects/my-app
req-validate

# CI mode (minimal output)
req-validate --ci

# Pre-commit hook (check staged files only)
req-validate --check-new

# GitHub Actions (check PR files)
req-validate --check-pr origin/main
```

**Output:**
```
📋 Requirements Validation Report
=================================

✅ No duplicate requirement IDs found

📊 Summary: 25 requirements checked, 0 errors
```

### Search Requirements

Search by various criteria:

```bash
# List all requirements
req-search list

# Search by tag
req-search tag :authentication
req-search tag :security

# Search by content
req-search content "multi-factor"
req-search content "performance"

# Search by status
req-search status accepted
req-search status proposed

# Search by category
req-search category :security

# Search by priority
req-search priority :must

# Search by type
req-search type functional
req-search type non-functional

# Find requirements linked to an ADR
req-search adr ADR-00042

# Find requirements linked to RunNotes
req-search runnote AuthRefactor

# List all tags
req-search list-tags

# List all categories
req-search list-categories

# Show tag usage statistics
req-search tag-summary

# Show summary statistics
req-search summary
```

**Example output:**
```
REQ-AUTH-001 - Multi-Factor Authentication
  File: REQ-AUTH-001-multi-factor-authentication.md
  Type: functional
  Category: security
  Priority: must
  Status: accepted
  Tags: :authentication :security :compliance
  ADRs: ADR-00042
  Code: src/auth/mfa.clj:45-67
  Tests: test/auth/mfa_test.clj:12
```

### Traceability Analysis

Analyze and report on traceability coverage:

```bash
# Show traceability coverage summary
req-trace summary

# Show traceability matrix for all requirements
req-trace matrix

# Show requirements missing specific traceability
req-trace gaps adr      # Missing ADR links
req-trace gaps code     # Missing code links
req-trace gaps tests    # Missing test links
req-trace gaps runnote  # Missing RunNotes links
req-trace gaps          # Show all gaps

# Show detailed traceability for a specific requirement
req-trace detail REQ-AUTH-001
```

**Example summary output:**
```
📊 Traceability Coverage Summary
=================================

Total Requirements: 25

ADR Traceability:
  With ADR links:      20/25 (80%)
  Missing ADR links:   5

Code Traceability:
  With code links:     18/25 (72%)
  Missing code links:  7

Test Traceability:
  With test links:     15/25 (60%)
  Missing test links:  10
```

### Fix Metadata Issues

Automatically fix common metadata problems across all requirements:

```bash
# Fix all requirements
req-fix-metadata

# Preview changes without applying (dry-run)
req-fix-metadata --dry-run

# Show help
req-fix-metadata --help
```

**Issues Fixed Automatically:**

1. **Missing :metadata marker** - Adds `:metadata` to EDN blocks
   ```edn
   # Before
   ```edn
   {:req-id "REQ-AUTH-001" ...}
   ```

   # After
   ```edn :metadata
   {:req-id "REQ-AUTH-001" ...}
   ```
   ```

2. **Trace strings instead of sets** - Converts trace fields to proper set format
   ```edn
   # Before
   :trace {:adr "ADR-001" :code "auth.py"}

   # After
   :trace {:adr #{"ADR-001"} :code #{"auth.py"}}
   ```

3. **Missing Context section** - Adds placeholder Context section
   ```markdown
   # Before: Missing ## Context section

   # After: Adds
   ## Context

   [Context to be added]
   ```

**Example Output:**
```
🔧 Requirement File Fix Report
==============================

✅ Fixed 27 file(s):

  REQ-AUTH-001-mfa.md
    • Added :metadata marker
    • Converted trace strings to sets

  REQ-DATA-003-encryption.md
    • Added Context section

✓ 53 file(s) already valid

📊 Summary: 80 files checked, 27 files fixed
```

## Requirements Format

Requirements use markdown with embedded EDN metadata:

```markdown
# REQ-AUTH-001 - Multi-Factor Authentication

\`\`\`edn :metadata
{:req-id "REQ-AUTH-001"
 :type :functional
 :category :security
 :priority :must
 :status :accepted
 :tag #{:authentication :security :compliance}
 :trace {:adr #{"ADR-00042"}
         :runnote #{"RunNotes-2025-10-01-AuthRefactor-planning"}
         :code #{"src/auth/mfa.clj:45-67"}
         :tests #{"test/auth/mfa_test.clj:12"}}
 :nfr-taxonomy {:iso-25010 #{:security/authenticity :security/accountability}
                :furps+ #{:reliability}}}
\`\`\`

## Requirement Statement

**MUST** support time-based one-time passwords (TOTP) as a second authentication factor.

## Context

We need stronger authentication to meet compliance requirements...

## Acceptance Criteria

- [ ] **AC-1**: TOTP generation follows RFC 6238
- [ ] **AC-2**: QR code provisioning supported
- [ ] **AC-3**: Backup codes generated and stored securely
```

### Metadata Fields

**Required:**
- `:req-id` - Unique requirement ID (format: REQ-[CATEGORY]-[NUMBER])
  - Supports hyphenated categories: `REQ-SECURITY-CRYPTO-001`, `REQ-INTEGRATION-VERSION-CONTROL-001`
- `:type` - `:functional`, `:non-functional`, `:constraint`, or `:integration`
- `:category` - Project-specific category (keyword or vector for multi-dimensional)
  - Single: `:authentication`
  - Multi-dimensional: `[:pppost :integration :events]`
- `:priority` - `:must`, `:shall`, `:should`, or `:may` (RFC 2119)
- `:status` - `:proposed`, `:accepted`, `:deprecated`, `:deferred`, or `:implemented`
- `:tag` - Set of keyword tags

**Optional:**
- `:updated` - Last update date (YYYY-MM-DD)
- `:trace` - Traceability links:
  - `:adr` - Set of ADR references
  - `:runnote` - Set of RunNotes references
  - `:code` - Set of code file references
  - `:tests` - Set of test file references
- `:nfr-taxonomy` - Quality attribute taxonomy:
  - `:iso-25010` - ISO 25010 quality characteristics
  - `:furps+` - FURPS+ categories
- `:acceptance-criteria` - Structured acceptance criteria

## File Naming Convention

Requirements files follow this pattern:

```
REQ-[CATEGORY]-[NUMBER]-title.md

Examples:
REQ-AUTH-001-multi-factor-authentication.md
REQ-PERF-042-response-time-sla.md
REQ-UI-015-accessibility-compliance.md

Hyphenated categories (multi-word):
REQ-SECURITY-CRYPTO-001-cryptographic-agility.md
REQ-INTEGRATION-VERSION-CONTROL-001-policy-versioning.md
```

- **CATEGORY**: Uppercase abbreviation, may include hyphens for multi-word categories
- **NUMBER**: 3-5 digit sequence (e.g., 001, 042, 00123)
- **title**: Lowercase with hyphens

## Directory Structure

```
~/.req/                          # Installation directory
├── bin/                        # Executable scripts
│   ├── req-validate            # Validate requirements format
│   ├── req-search              # Search and filter requirements
│   ├── req-trace               # Traceability analysis
│   └── req-fix-metadata        # Automated metadata fixer
├── req-core.bb                 # Requirements utilities (domain-specific)
├── req-metadata-extractor.bb   # Metadata extraction and validation
├── template/                   # Requirement templates
│   ├── default.md
│   ├── functional.md
│   └── non-functional.md
├── test/                       # Test suite
│   ├── validator-error-reporting-test.bb  # Validator tests (10 tests)
│   └── duplicate-function-test.bb         # DRY refactoring tests
├── config.edn                  # Global configuration
└── README.md                   # This file

Dependencies from ~/.lib:
├── config-core.bb              # Shared config management
├── metadata-parser.bb          # EDN metadata parsing
└── test/test-framework.bb      # Test utilities
```

## Common Workflows

### Starting a New Project

1. Initialize requirements directory:
   ```bash
   mkdir -p doc/req
   cp ~/.req/template/default.md doc/req/REQ-00000-template.md
   ```

2. Create first requirement:
   ```bash
   cp doc/req/REQ-00000-template.md doc/req/REQ-ARCH-001-system-architecture.md
   # Edit the file to fill in details
   ```

3. Validate:
   ```bash
   req-validate
   ```

### Project with Custom Path

1. Create `.req.edn` in project root:
   ```edn
   {:req
    {:path "docs/requirements"}}
   ```

2. Tools automatically use custom path:
   ```bash
   req-validate  # Uses docs/requirements
   req-search list
   ```

### Pre-commit Hook

Prevent commits with duplicate requirement IDs:

```bash
# .git/hooks/pre-commit
#!/bin/bash
req-validate --check-new
```

### CI/CD Integration

#### GitHub Actions

**Full workflow example** - validate requirements on pull requests:

```yaml
# .github/workflows/validate-requirements.yml
name: Validate Requirements

on:
  pull_request:
    paths:
      - 'doc/req/**'
      - '.req.edn'
  push:
    branches:
      - main
    paths:
      - 'doc/req/**'
      - '.req.edn'

jobs:
  validate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0  # Needed for PR validation

      - name: Install Babashka
        run: |
          curl -sL https://raw.githubusercontent.com/babashka/babashka/master/install | sudo bash

      - name: Install req-tools
        run: |
          VERSION=v1.0.0  # Pin to specific version for reproducibility
          curl -sL https://github.com/rriehle/.req/releases/download/${VERSION}/req-tools-${VERSION}.tar.gz | tar xz
          mv req-tools-${VERSION} ~/.req
          echo "$HOME/.req/bin" >> $GITHUB_PATH

      - name: Validate requirements (PR)
        if: github.event_name == 'pull_request'
        run: req-validate --check-pr origin/${{ github.base_ref }}

      - name: Validate requirements (Push)
        if: github.event_name == 'push'
        run: req-validate --ci

      - name: Check traceability coverage
        run: req-trace summary
```

**Minimal example** - validate on any change:

```yaml
- name: Install Babashka
  run: curl -sL https://raw.githubusercontent.com/babashka/babashka/master/install | sudo bash

- name: Install req-tools
  run: |
    curl -sL https://github.com/rriehle/.req/releases/latest/download/install.sh | bash
    echo "$HOME/.req/bin" >> $GITHUB_PATH

- name: Validate Requirements
  run: req-validate --ci
```

**Reusable workflow** - create a reusable workflow in your organization:

```yaml
# .github/workflows/req-validate-reusable.yml
name: Validate Requirements (Reusable)

on:
  workflow_call:
    inputs:
      req-tools-version:
        description: 'Version of req-tools to use'
        required: false
        default: 'latest'
        type: string

jobs:
  validate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Install Babashka
        run: curl -sL https://raw.githubusercontent.com/babashka/babashka/master/install | sudo bash

      - name: Install req-tools
        run: |
          VERSION=${{ inputs.req-tools-version }}
          if [ "$VERSION" = "latest" ]; then
            URL="https://github.com/rriehle/.req/releases/latest/download/install.sh"
          else
            URL="https://github.com/rriehle/.req/releases/download/${VERSION}/install.sh"
          fi
          curl -sL "$URL" | bash
          echo "$HOME/.req/bin" >> $GITHUB_PATH

      - name: Validate
        run: req-validate --ci

      - name: Traceability Report
        run: req-trace summary
```

Then use it in your projects:

```yaml
# .github/workflows/pr-checks.yml
name: PR Checks

on: [pull_request]

jobs:
  requirements:
    uses: ./.github/workflows/req-validate-reusable.yml
    with:
      req-tools-version: v1.0.0
```

#### GitLab CI

```yaml
# .gitlab-ci.yml
validate-requirements:
  stage: test
  image: ubuntu:latest
  before_script:
    # Install Babashka
    - apt-get update && apt-get install -y curl
    - curl -sL https://raw.githubusercontent.com/babashka/babashka/master/install | bash
    - export PATH="/usr/local/bin:$PATH"

    # Install req-tools
    - curl -sL https://github.com/rriehle/.req/releases/latest/download/install.sh | bash
    - export PATH="$HOME/.req/bin:$PATH"

  script:
    - req-validate --ci
    - req-trace summary

  only:
    changes:
      - doc/req/**
      - .req.edn
```

#### Docker/Container Approach

For reproducible builds, create a Docker image with req-tools pre-installed:

```dockerfile
# Dockerfile.req-tools
FROM babashka/babashka:latest

ARG REQ_TOOLS_VERSION=latest

RUN apt-get update && apt-get install -y curl tar && \
    curl -sL "https://github.com/rriehle/.req/releases/${REQ_TOOLS_VERSION}/download/install.sh" | bash && \
    rm -rf /var/lib/apt/lists/*

ENV PATH="/root/.req/bin:${PATH}"

WORKDIR /workspace

ENTRYPOINT ["req-validate"]
CMD ["--help"]
```

Then use in CI:

```yaml
- name: Validate Requirements
  uses: docker://your-registry/req-tools:v1.0.0
  with:
    args: --ci
```

## Integration with ADR and RunNotes

Requirements integrate seamlessly with ADR and RunNotes systems:

### Linking to ADRs

```edn
{:trace {:adr #{"ADR-00042" "ADR-00043"}}}
```

Search requirements by ADR:
```bash
req-search adr ADR-00042
```

Find requirements missing ADR links:
```bash
req-trace gaps adr
```

### Linking to RunNotes

```edn
{:trace {:runnote #{"RunNotes-2025-10-01-AuthRefactor-planning"}}}
```

Search requirements by RunNotes:
```bash
req-search runnote AuthRefactor
```

### Traceability Workflow

1. **Planning Phase (RunNotes)**:
   - Document business need in RunNotes planning session
   - Elicit requirements from business process

2. **Requirements Phase**:
   - Create requirement with RunNotes traceability
   - Link to relevant ADRs for architectural constraints

3. **Implementation Phase**:
   - Implement feature
   - Update requirement with code traceability

4. **Testing Phase**:
   - Write tests
   - Update requirement with test traceability

5. **Verification**:
   ```bash
   req-trace detail REQ-AUTH-001  # Verify complete traceability
   ```

## ISO 25010 Quality Model

Requirements support ISO 25010 quality characteristics:

```edn
{:nfr-taxonomy
 {:iso-25010 #{:functional-suitability/completeness
               :performance-efficiency/time-behavior
               :security/authenticity
               :usability/accessibility
               :reliability/fault-tolerance
               :maintainability/testability
               :portability/adaptability
               :compatibility/interoperability}}}
```

**Available characteristics:**
- **Functional Suitability**: Completeness, Correctness, Appropriateness
- **Performance Efficiency**: Time Behavior, Resource Utilization, Capacity
- **Compatibility**: Co-existence, Interoperability
- **Usability**: Learnability, Operability, User Error Protection, Accessibility, UI Aesthetics
- **Reliability**: Maturity, Availability, Fault Tolerance, Recoverability
- **Security**: Confidentiality, Integrity, Non-repudiation, Accountability, Authenticity
- **Maintainability**: Modularity, Reusability, Analyzability, Modifiability, Testability
- **Portability**: Adaptability, Installability, Replaceability

## Troubleshooting

### "Could not load config" errors

**Problem:** Project config file has invalid EDN.

**Solution:**
```bash
# Validate EDN syntax
bb -e "(clojure.edn/read-string (slurp \".req.edn\"))"
```

### Requirements not found

**Problem:** Requirements directory doesn't exist or path is wrong.

**Solution:**
1. Check config:
   ```bash
   cat .req.edn
   ```

2. Verify directory exists:
   ```bash
   ls -la doc/req  # or custom path
   ```

### "Invalid metadata" errors

**Problem:** Requirement metadata is incomplete or malformed.

**Solution:** Ensure all required fields are present:
```edn
{:req-id "REQ-CAT-001"
 :type :functional
 :category :your-category
 :priority :must
 :status :proposed
 :tag #{:your-tag}}
```

## Best Practices

### Requirement Writing

- **Use RFC 2119 keywords**: Be precise with MUST/SHALL/SHOULD/MAY
- **Make requirements testable**: Each requirement should have verifiable acceptance criteria
- **One requirement per file**: Keep requirements atomic and focused
- **Link early**: Add traceability links as soon as possible

### Traceability

- **Bidirectional links**: When linking a requirement to code, add a comment in code linking back
- **Update on change**: When code changes, update requirement traceability
- **Regular audits**: Use `req-trace summary` to track coverage trends

### Categorization

- **Consistent categories**: Define project categories in `.req.edn`
- **Meaningful tags**: Use tags for cross-cutting concerns
- **ISO 25010 for NFRs**: Use standard taxonomy for non-functional requirements

## Documentation

For AI agents and advanced users:

- **[CLAUDE.md](CLAUDE.md)** - Quick reference guide for AI agents
- **[doc/README-WORKFLOWS.md](doc/README-WORKFLOWS.md)** - Detailed elicitation, inference, and validation workflows
- **[doc/README-FILE-FORMAT.md](doc/README-FILE-FORMAT.md)** - Complete format specifications, metadata fields, EDN syntax
- **[doc/README-INTEGRATION.md](doc/README-INTEGRATION.md)** - Integration patterns with ADR, RunNotes, code, tests
- **[doc/README-QUALITY.md](doc/README-QUALITY.md)** - Quality enforcement guidelines, RFC 2119, testability, common pitfalls

## See Also

- [ADR Tools](https://github.com/rriehle/.adr)
- [RunNotes](https://github.com:rriehle/.runnote)
- [ISO/IEC 25010 Software Quality Model](https://iso25000.com/index.php/en/iso-25000-standards/iso-25010)
- [RFC 2119: Key words for use in RFCs](https://www.rfc-editor.org/rfc/rfc2119)

## License

MIT

