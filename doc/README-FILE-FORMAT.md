# Requirements File Format Reference

Complete specification for requirements file format, metadata fields, and EDN syntax. For a quick reference, see [CLAUDE.md](../CLAUDE.md).

## Table of Contents

- [File Structure](#file-structure)
- [Metadata Fields](#metadata-fields)
- [File Naming Convention](#file-naming-convention)
- [Traceability Structure](#traceability-structure)
- [ISO 25010 Taxonomy Structure](#iso-25010-taxonomy-structure)
- [EDN Syntax Reference](#edn-syntax-reference)

---

## File Structure

Requirements use markdown with embedded EDN metadata:

```markdown
# REQ-[CATEGORY]-[NUMBER] - [Title]

\`\`\`edn :metadata
{:req-id "REQ-[CATEGORY]-[NUMBER]"
 :type :functional
 :category :your-category
 :priority :must
 :status :proposed
 :tag #{:tag1 :tag2}
 :trace {:adr #{...} :runnote #{...} :code #{...} :tests #{...}}}
\`\`\`

## Requirement Statement

**[MUST|SHALL|SHOULD|MAY]** [Clear, testable statement]

## Context

[Business need, rationale, related ADRs/RunNotes, constraints]

## Acceptance Criteria

- [ ] **AC-1**: [Testable, measurable criterion]
- [ ] **AC-2**: [Another criterion]
- [ ] **AC-3**: [Another criterion]
```

### Complete Example

```markdown
# REQ-AUTH-001 - Multi-Factor Authentication

\`\`\`edn :metadata
{:req-id "REQ-AUTH-001"
 :type :functional
 :category :security
 :priority :must
 :status :accepted
 :tag #{:authentication :security :compliance}
 :updated "2025-10-14"
 :trace {:adr #{"ADR-00042"}
         :runnote #{"RunNotes-2025-10-01-AuthRefactor-planning"}
         :code #{"src/auth/mfa.clj:45-67"}
         :tests #{"test/auth/mfa_test.clj:12"}}
 :nfr-taxonomy {:iso-25010 #{:security/authenticity :security/accountability}
                :furps+ #{:reliability}}}
\`\`\`

## Requirement Statement

**MUST** support time-based one-time passwords (TOTP) as a second authentication factor per RFC 6238.

## Context

Per RunNotes-2025-10-01-AuthRefactor-planning, we need stronger authentication to meet compliance requirements (SOC 2, GDPR). Current password-only authentication presents security risk for high-value accounts.

Per ADR-00042, we chose TOTP over SMS-based 2FA due to SIM-swapping attack vectors and better offline support.

Target rollout: All admin users (MUST), optional for regular users (MAY).

## Acceptance Criteria

- [ ] **AC-1**: TOTP generation follows RFC 6238 specification
- [ ] **AC-2**: QR code provisioning supported for mobile authenticator apps
- [ ] **AC-3**: Backup codes generated (10 single-use codes) and stored securely
- [ ] **AC-4**: Time window tolerance of ±1 period (30 seconds) for clock skew
- [ ] **AC-5**: Rate limiting applied (max 5 attempts per 5 minutes)
```

---

## Metadata Fields

### Required Fields

All requirements MUST include these fields:

#### :req-id

**Format:** `"REQ-[CATEGORY]-[NUMBER]"`

**Examples:**
```edn
:req-id "REQ-AUTH-001"
:req-id "REQ-PERF-042"
:req-id "REQ-SECURITY-CRYPTO-001"  ; Hyphenated category
```

**Rules:**
- Must be unique across all requirements
- Must match filename (REQ-AUTH-001 → REQ-AUTH-001-*.md)
- Category may be multi-word with hyphens
- Number: 3-5 digits (001, 042, 00123)

#### :type

**Format:** Keyword

**Valid values:**
- `:functional` - What the system does
- `:non-functional` - How well the system performs
- `:constraint` - Fixed constraints (legal, regulatory, environmental)
- `:integration` - Integration with external systems

**Examples:**
```edn
:type :functional
:type :non-functional
:type :constraint
:type :integration
```

#### :category

**Format:** Keyword or vector (for multi-dimensional categorization)

**Single category:**
```edn
:category :authentication
:category :performance
:category :data-management
```

**Multi-dimensional category:**
```edn
:category [:pppost :integration :events]
:category [:security :crypto]
```

**Rules:**
- Use project-specific taxonomy
- Define common categories in `.req.edn`
- Be consistent across requirements

#### :priority

**Format:** Keyword (RFC 2119 levels)

**Valid values:**
- `:must` / `:shall` - Absolute requirement
- `:should` - Strong recommendation
- `:may` - Optional

**Examples:**
```edn
:priority :must
:priority :should
:priority :may
```

**Decision guide:**
- **:must** - System unusable without it, legal/regulatory requirement, critical business function
- **:should** - Best practice, important but not critical, deviations need justification
- **:may** - Nice to have, future consideration, discretionary

#### :status

**Format:** Keyword

**Valid values:**
- `:proposed` - Initial draft, under review
- `:accepted` - Approved for implementation
- `:implemented` - Fully implemented and verified
- `:deprecated` - No longer valid, superseded
- `:deferred` - Postponed to future release

**Examples:**
```edn
:status :proposed
:status :accepted
:status :implemented
:status :deprecated
:status :deferred
```

**Status transitions:**
```
:proposed → :accepted → :implemented
           ↓
        :deferred
           ↓
        :deprecated
```

#### :tag

**Format:** Set of keywords

**Examples:**
```edn
:tag #{:authentication}
:tag #{:security :compliance :audit}
:tag #{:performance :scalability :caching}
```

**Rules:**
- Must be a set (use `#{}`)
- At least one tag required
- Use for cross-cutting concerns
- Be consistent with project taxonomy

**Best practices:**
- Check existing tags: `req-search list-tags`
- Reuse tags for consistency
- Tags for: domain, technical area, stakeholder, release

### Optional Fields

#### :updated

**Format:** String (YYYY-MM-DD)

**Example:**
```edn
:updated "2025-10-14"
```

**Use for:** Tracking last modification date

#### :trace

**Format:** Map of sets

Traceability links to related artifacts.

**Structure:**
```edn
:trace {:adr #{"ADR-00042" "ADR-00043"}
        :runnote #{"RunNotes-2025-10-01-Feature-planning"}
        :code #{"src/auth/mfa.py:45-67" "src/auth/api.py:123"}
        :tests #{"test/auth/test_mfa.py:12-45"}}
```

**Fields:**
- `:adr` - Architecture Decision Records
- `:runnote` - RunNotes planning/implementation documents
- `:code` - Source code files (with line numbers/ranges)
- `:tests` - Test files (with line numbers/ranges)

**Format for code/tests:**
- With line range: `"path/to/file.ext:45-67"`
- Single line: `"path/to/file.ext:45"`
- Whole file: `"path/to/file.ext"`

**All values must be sets, not strings:**
```edn
# Wrong
:trace {:adr "ADR-001"}

# Right
:trace {:adr #{"ADR-001"}}
```

#### :nfr-taxonomy

**Format:** Map with taxonomy sets

For non-functional requirements, maps to quality characteristic taxonomies.

**Structure:**
```edn
:nfr-taxonomy {:iso-25010 #{:performance-efficiency/time-behavior
                             :reliability/fault-tolerance}
               :furps+ #{:performance :reliability}}
```

**Fields:**
- `:iso-25010` - ISO 25010 quality characteristics (see below)
- `:furps+` - FURPS+ categories (optional supplement)

#### :acceptance-criteria

**Format:** Project-defined structure

Some projects use structured acceptance criteria in metadata:

```edn
:acceptance-criteria [{:id "AC-1"
                       :description "TOTP follows RFC 6238"
                       :verified false}
                      {:id "AC-2"
                       :description "QR code provisioning"
                       :verified true}]
```

Most projects use markdown section instead (see File Structure).

---

## File Naming Convention

### Format

```
REQ-[CATEGORY]-[NUMBER]-title.md
```

### Components

**CATEGORY:**
- Uppercase
- May include hyphens for multi-word categories
- Examples: `AUTH`, `PERF`, `SECURITY-CRYPTO`, `INTEGRATION-VERSION-CONTROL`

**NUMBER:**
- 3-5 digit sequence
- Zero-padded
- Examples: `001`, `042`, `00123`

**title:**
- Lowercase
- Words separated by hyphens
- Brief, descriptive
- Examples: `multi-factor-authentication`, `response-time-sla`, `cryptographic-agility`

### Examples

**Single-word categories:**
```
REQ-AUTH-001-multi-factor-authentication.md
REQ-PERF-042-response-time-sla.md
REQ-UI-015-accessibility-compliance.md
REQ-DATA-007-encryption-at-rest.md
```

**Multi-word categories (hyphenated):**
```
REQ-SECURITY-CRYPTO-001-cryptographic-agility.md
REQ-INTEGRATION-VERSION-CONTROL-001-policy-versioning.md
REQ-USER-MANAGEMENT-012-role-based-access.md
```

**Non-functional requirements:**
```
REQ-AUTH-NFR-001-authentication-performance.md
REQ-DATA-NFR-005-backup-recovery.md
REQ-API-NFR-003-rate-limiting.md
```

### Rules

1. REQ-ID in metadata MUST match filename pattern:
   - File: `REQ-AUTH-001-multi-factor.md`
   - Metadata: `:req-id "REQ-AUTH-001"`

2. Category in filename SHOULD match `:category` in metadata:
   - File: `REQ-AUTH-001-*.md`
   - Metadata: `:category :authentication` or `:category [:security :auth]`

3. Use consistent casing:
   - Filename category: UPPERCASE
   - Metadata category: lowercase keyword

---

## Traceability Structure

### Complete Example

```edn
{:trace
 {:adr #{"ADR-00042" "ADR-00043"}
  :runnote #{"RunNotes-2025-10-01-Feature-planning"
             "RunNotes-2025-10-01-Feature-research"}
  :code #{"src/auth/mfa.py:45-67"
          "src/auth/api.py:123-145"
          "src/auth/totp.py"}
  :tests #{"test/auth/test_mfa.py:12-45"
           "test/auth/test_api.py:67"
           "test/integration/test_auth_flow.py"}}}
```

### Field Descriptions

#### :adr - Architecture Decision Records

References to ADR files documenting architectural decisions that constrain or inform this requirement.

**Format:** Set of ADR identifiers (strings)

**Examples:**
```edn
:adr #{"ADR-00042"}
:adr #{"ADR-00042" "ADR-00043" "ADR-00055"}
```

**When to link:**
- Requirement affected by architectural decision
- Requirement drives need for architectural decision
- Technical constraint documented in ADR

#### :runnote - RunNotes Documents

References to RunNotes documenting planning, research, or implementation journey.

**Format:** Set of RunNotes identifiers (strings)

**Examples:**
```edn
:runnote #{"RunNotes-2025-10-01-AuthRefactor-planning"}
:runnote #{"RunNotes-2025-10-01-Feature-planning"
           "RunNotes-2025-10-05-Feature-implementation"}
```

**When to link:**
- Business need documented in RunNotes
- Planning session that identified requirement
- Implementation notes with discoveries

#### :code - Source Code Files

References to source code files implementing this requirement.

**Format:** Set of file paths with optional line numbers (strings)

**Line number formats:**
- Range: `"path/to/file.ext:45-67"`
- Single line: `"path/to/file.ext:45"`
- Whole file: `"path/to/file.ext"`

**Examples:**
```edn
:code #{"src/auth/mfa.py:45-67"}
:code #{"src/auth/mfa.py:45-67"
        "src/auth/api.py:123-145"
        "src/auth/totp.py"}
```

**Best practices:**
- Use project-relative paths
- Include line numbers for precise references
- Update when code moves
- Add bidirectional comment in code

#### :tests - Test Files

References to test files verifying this requirement.

**Format:** Set of test file paths with optional line numbers (strings)

**Examples:**
```edn
:tests #{"test/auth/test_mfa.py:12-45"}
:tests #{"test/auth/test_mfa.py:12-45"
         "test/auth/test_api.py:67"
         "test/integration/test_auth_flow.py"}
```

**Best practices:**
- Link tests that verify acceptance criteria
- Include line numbers for specific test cases
- Update when tests move
- Reference REQ-ID in test docstrings

### Expected Traceability by Status

| Status | Required Links | Optional Links |
|--------|----------------|----------------|
| :proposed | :runnote (business need) | :adr (constraints) |
| :accepted | :runnote, :adr | :code (early impl) |
| :implemented | :runnote, :adr, :code, :tests | - |
| :deprecated | (preserve original) | :superseded-by |

---

## ISO 25010 Taxonomy Structure

For non-functional requirements, use ISO 25010 quality model.

### Complete Example

```edn
{:nfr-taxonomy
 {:iso-25010 #{:performance-efficiency/time-behavior
               :performance-efficiency/resource-utilization
               :reliability/fault-tolerance
               :security/authenticity
               :usability/accessibility}
  :furps+ #{:performance :reliability :usability}}}
```

### ISO 25010 Characteristics

Format: `:category/subcategory`

#### Functional Suitability

```edn
:functional-suitability/completeness
:functional-suitability/correctness
:functional-suitability/appropriateness
```

#### Performance Efficiency

```edn
:performance-efficiency/time-behavior
:performance-efficiency/resource-utilization
:performance-efficiency/capacity
```

#### Compatibility

```edn
:compatibility/co-existence
:compatibility/interoperability
```

#### Usability

```edn
:usability/learnability
:usability/operability
:usability/user-error-protection
:usability/accessibility
:usability/ui-aesthetics
```

#### Reliability

```edn
:reliability/maturity
:reliability/availability
:reliability/fault-tolerance
:reliability/recoverability
```

#### Security

```edn
:security/confidentiality
:security/integrity
:security/non-repudiation
:security/accountability
:security/authenticity
```

#### Maintainability

```edn
:maintainability/modularity
:maintainability/reusability
:maintainability/analyzability
:maintainability/modifiability
:maintainability/testability
```

#### Portability

```edn
:portability/adaptability
:portability/installability
:portability/replaceability
```

### Usage Examples

**Performance requirement:**
```edn
{:nfr-taxonomy
 {:iso-25010 #{:performance-efficiency/time-behavior}}}
```

**Security requirement:**
```edn
{:nfr-taxonomy
 {:iso-25010 #{:security/authenticity
               :security/accountability
               :security/confidentiality}}}
```

**Reliability requirement:**
```edn
{:nfr-taxonomy
 {:iso-25010 #{:reliability/fault-tolerance
               :reliability/recoverability}}}
```

---

## EDN Syntax Reference

### Maps

Key-value pairs enclosed in `{}`:

```edn
{:key1 "value1"
 :key2 42
 :key3 #{:set :of :keywords}}
```

### Keywords

Identifiers starting with `:`:

```edn
:functional
:must
:proposed
:authentication
:performance-efficiency/time-behavior  ; Qualified keyword
```

### Strings

Text enclosed in double quotes:

```edn
"2025-10-14"
"REQ-AUTH-001"
"src/module/file.py:45-67"
```

### Sets

Unique collection of values with `#{}`:

```edn
#{:tag1 :tag2 :tag3}
#{"ADR-00042" "ADR-00043"}
#{:security/authenticity :security/accountability}
```

**Sets are unordered and contain unique elements.**

### Vectors

Ordered collection with `[]` (less common in requirements):

```edn
[:category1 :category2]
```

### Numbers

```edn
42
3.14
```

### Booleans

```edn
true
false
```

### Nil

```edn
nil
```

### Common Mistakes

#### Wrong: String instead of keyword

```edn
# Wrong
{:priority "must"}
{:status "proposed"}

# Right
{:priority :must}
{:status :proposed}
```

#### Wrong: Vector instead of set for tags

```edn
# Wrong
{:tag [:authentication :security]}

# Right
{:tag #{:authentication :security}}
```

#### Wrong: String instead of set for trace

```edn
# Wrong
{:trace {:adr "ADR-001"}}

# Right
{:trace {:adr #{"ADR-001"}}}
```

#### Wrong: Unquoted date

```edn
# Wrong
{:date 2025-10-14}

# Right
{:date "2025-10-14"}
```

#### Wrong: Missing :metadata marker

```edn
# Wrong
```edn
{:req-id "REQ-AUTH-001" ...}
```

# Right
```edn :metadata
{:req-id "REQ-AUTH-001" ...}
```
```

### Validation

**Check EDN syntax:**
```bash
bb -e "(clojure.edn/read-string (slurp \"path/to/requirement.md\"))"
```

**Validate with req-validate:**
```bash
req-validate
```

---

## See Also

- [CLAUDE.md](../CLAUDE.md) - Quick reference for AI agents
- [README.md](../README.md) - Human-focused usage guide
- [README-WORKFLOWS.md](README-WORKFLOWS.md) - Detailed workflow examples
- [README-INTEGRATION.md](README-INTEGRATION.md) - Integration patterns
- [README-QUALITY.md](README-QUALITY.md) - Quality enforcement guidelines
- [ISO/IEC 25010](https://iso25000.com/index.php/en/iso-25000-standards/iso-25010) - Quality model standard
- [RFC 2119](https://www.rfc-editor.org/rfc/rfc2119) - RFC keywords
