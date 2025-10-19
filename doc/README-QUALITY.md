# Requirements Quality Enforcement

Detailed guidelines for enforcing quality standards in requirements. For a quick reference, see [CLAUDE.md](../CLAUDE.md).

## Table of Contents

- [RFC 2119 Keyword Precision](#rfc-2119-keyword-precision)
- [Atomic Requirements Enforcement](#atomic-requirements-enforcement)
- [Testable Acceptance Criteria](#testable-acceptance-criteria)
- [Complete Traceability](#complete-traceability)
- [Common Pitfalls](#common-pitfalls)
- [Decision Framework](#decision-framework)

---

## RFC 2119 Keyword Precision

Requirements use RFC 2119 keywords with precise meaning to eliminate ambiguity.

### Keyword Definitions

| Keyword | Meaning | When to Use |
|---------|---------|-------------|
| **MUST** / **SHALL** | Absolute requirement | Legal requirement, system unusable without it, critical business function |
| **MUST NOT** / **SHALL NOT** | Absolute prohibition | Security violation, legal prohibition, data corruption risk |
| **SHOULD** | Strong recommendation | Best practice, important but not critical, deviations need justification |
| **SHOULD NOT** | Strong discouragement | Problematic but not prohibited, alternatives preferred |
| **MAY** | Optional | Nice to have, future consideration, discretionary |

### Decision Guide

Ask these questions to determine the correct keyword:

**For MUST/SHALL:**
- Can we ship without this? **If NO → MUST**
- Is this legally/regulatorily required? **If YES → MUST**
- Is the system unusable without it? **If YES → MUST**
- Is this a critical business function? **If YES → MUST**

**For SHOULD:**
- Is this best practice but negotiable? **If YES → SHOULD**
- Are there valid reasons to deviate? **If YES → SHOULD**
- Is this important but not critical? **If YES → SHOULD**

**For MAY:**
- Is this optional enhancement? **If YES → MAY**
- Is this a nice-to-have feature? **If YES → MAY**
- Is this discretionary? **If YES → MAY**

### Examples: Correct Usage

#### MUST Examples

✅ **Correct:**
```markdown
System **MUST** authenticate users before allowing access to protected resources.
```
**Why:** Security critical - system unusable without authentication

✅ **Correct:**
```markdown
System **MUST** encrypt personally identifiable information (PII) at rest per GDPR requirements.
```
**Why:** Legal requirement (GDPR compliance)

✅ **Correct:**
```markdown
System **MUST** process payment transactions with ACID guarantees.
```
**Why:** Critical business function - data corruption unacceptable

#### SHOULD Examples

✅ **Correct:**
```markdown
System **SHOULD** respond to search queries within 500ms at 95th percentile.
```
**Why:** Performance target, important but not absolute - degradation acceptable under load

✅ **Correct:**
```markdown
API responses **SHOULD** include ETag headers for caching optimization.
```
**Why:** Best practice, improves performance, but system works without it

✅ **Correct:**
```markdown
Error messages **SHOULD** provide actionable guidance to users.
```
**Why:** Important for UX, but exact wording flexible

#### MAY Examples

✅ **Correct:**
```markdown
System **MAY** provide dark mode theme for user interface.
```
**Why:** Nice-to-have feature, purely optional

✅ **Correct:**
```markdown
Application **MAY** support exporting data to PDF format.
```
**Why:** Discretionary feature, not core functionality

### Common Mistakes

#### Mistake 1: Using SHOULD for Critical Requirements

❌ **Wrong:**
```markdown
System should authenticate users.
```

✅ **Right:**
```markdown
System **MUST** authenticate users before allowing access.
```

**Why wrong:** Authentication is not optional - it's a security requirement.

#### Mistake 2: Using MUST for Optional Features

❌ **Wrong:**
```markdown
System must have dark mode.
```

✅ **Right:**
```markdown
System **MAY** provide dark mode theme.
```

**Why wrong:** Dark mode is a nice-to-have, not critical.

#### Mistake 3: Using SHOULD for SLA Commitments

❌ **Wrong:**
```markdown
API should return within 200ms.
```

✅ **Right:**
```markdown
API **MUST** return within 200ms at 95th percentile per SLA.
```

**Why wrong:** If this is a contractual SLA, it's MUST, not SHOULD.

#### Mistake 4: Vague MUST Without Measurable Criteria

❌ **Wrong:**
```markdown
System must be fast.
```

✅ **Right:**
```markdown
System **MUST** respond to user actions within 100ms at 95th percentile.
```

**Why wrong:** "Fast" is subjective - need measurable criteria.

### Enforcement Checklist

Before finalizing a requirement:

- [ ] Contains one RFC 2119 keyword (MUST, SHOULD, or MAY)
- [ ] Keyword is **bold** for visibility
- [ ] Keyword matches actual priority (:must, :should, :may in metadata)
- [ ] If MUST: requirement is truly absolute and testable
- [ ] If SHOULD: deviations can be justified
- [ ] If MAY: clearly optional, not disguised requirement

---

## Atomic Requirements Enforcement

Each requirement file MUST contain exactly one requirement. Compound requirements reduce clarity and traceability.

### Violation Patterns

#### Pattern 1: Multiple SHALL/MUST Statements

❌ **Wrong:**
```markdown
# REQ-AUTH-001 - Authentication and Authorization

System SHALL authenticate users using OAuth2 AND
system SHALL authorize access based on roles AND
system SHALL log all security events.
```

**Why wrong:** Three separate concerns combined into one requirement.

✅ **Right - Decompose:**
```markdown
# REQ-AUTH-001 - OAuth2 Authentication
System **SHALL** authenticate users using OAuth2 protocol.

# REQ-AUTH-002 - Role-Based Authorization
System **SHALL** authorize access based on user roles.

# REQ-AUTH-003 - Security Event Logging
System **SHALL** log all authentication and authorization events.
```

#### Pattern 2: "AND" Connecting Different Concerns

❌ **Wrong:**
```markdown
System MUST validate input AND sanitize output AND log errors.
```

✅ **Right:**
```markdown
# REQ-VAL-001 - Input Validation
System **MUST** validate all user input against defined schemas.

# REQ-SEC-002 - Output Sanitization
System **MUST** sanitize all output to prevent XSS attacks.

# REQ-LOG-003 - Error Logging
System **MUST** log all errors with timestamp and context.
```

#### Pattern 3: Multiple Unrelated Acceptance Criteria

❌ **Wrong:**
```markdown
## Acceptance Criteria

- [ ] **AC-1**: User can log in with username/password
- [ ] **AC-2**: User roles determine accessible features
- [ ] **AC-3**: All login attempts are logged
```

**Why wrong:** Three different requirements (authentication, authorization, audit logging).

✅ **Right:** Split into three requirements, each with focused AC.

### Decomposition Strategy

**Step 1: Identify concerns**
- Read requirement statement
- Count "AND" clauses
- Group related acceptance criteria

**Step 2: Create separate requirements**
- One file per concern
- Independent REQ-IDs
- Focused acceptance criteria

**Step 3: Link related requirements**
- Use Context section to reference related reqs
- Maintain traceability to same ADR/RunNotes

**Example:**

**Original compound requirement:**
```markdown
System SHALL authenticate users AND authorize access AND log audit trail.
```

**Decomposed (3 requirements):**

```markdown
# REQ-AUTH-001 - User Authentication
System **SHALL** authenticate users using multi-factor authentication.

## Context
Part of security framework. See also REQ-AUTH-002 (authorization), REQ-AUTH-003 (audit logging).
```

```markdown
# REQ-AUTH-002 - Access Authorization
System **SHALL** authorize access based on user roles and permissions.

## Context
Part of security framework. See also REQ-AUTH-001 (authentication), REQ-AUTH-003 (audit logging).
```

```markdown
# REQ-AUTH-003 - Security Audit Logging
System **SHALL** log all authentication and authorization events.

## Context
Part of security framework. See also REQ-AUTH-001 (authentication), REQ-AUTH-002 (authorization).
```

### Benefits of Atomic Requirements

- **Independent verification** - Each can be tested separately
- **Clear ownership** - Different teams can own different requirements
- **Precise traceability** - Code maps to specific requirement
- **Granular status tracking** - Some may be implemented, others deferred

### Test for Atomicity

Ask: "Does this requirement address multiple distinct concerns?"

**If YES:**
- Multiple SHALL/MUST statements
- Multiple unrelated acceptance criteria
- "AND" connecting different topics

**Then:** Decompose into atomic requirements.

---

## Testable Acceptance Criteria

Every acceptance criterion MUST be verifiable through objective criteria.

### Characteristics of Testable Criteria

**Measurable:**
- Can be verified objectively
- Quantifiable where possible
- Specific thresholds defined

**Unambiguous:**
- Single interpretation
- No subjective terms
- Clear pass/fail definition

**Observable:**
- Can be tested (automated or manual)
- Results can be recorded
- Repeatable verification

**Binary:**
- Pass or fail, no gray area
- No partial compliance
- Clear boundary conditions

### Non-Testable vs Testable

#### Example 1: User Interface

❌ **Non-testable:**
```markdown
- [ ] **AC-1**: User interface is intuitive
```
**Why:** "Intuitive" is subjective.

✅ **Testable:**
```markdown
- [ ] **AC-1**: New users complete first task within 5 minutes without consulting help documentation (measured via usability testing with n=10 users)
```

#### Example 2: Performance

❌ **Non-testable:**
```markdown
- [ ] **AC-1**: System performs well under load
```
**Why:** "Performs well" is vague.

✅ **Testable:**
```markdown
- [ ] **AC-1**: System processes 1000 requests/second at 95th percentile latency < 200ms under sustained load
```

#### Example 3: Security

❌ **Non-testable:**
```markdown
- [ ] **AC-1**: System is secure
```
**Why:** "Secure" is too broad.

✅ **Testable:**
```markdown
- [ ] **AC-1**: All user passwords MUST be hashed using bcrypt with cost factor ≥ 12
- [ ] **AC-2**: All API endpoints MUST require authentication token
- [ ] **AC-3**: All database queries MUST use parameterized statements
```

#### Example 4: Code Quality

❌ **Non-testable:**
```markdown
- [ ] **AC-1**: Code is maintainable
```
**Why:** "Maintainable" is subjective.

✅ **Testable:**
```markdown
- [ ] **AC-1**: Cyclomatic complexity ≤ 10 for all functions
- [ ] **AC-2**: Code coverage ≥ 80% for all modules
- [ ] **AC-3**: All public APIs documented with examples
```

### Red Flags for Non-Testable Criteria

Watch for these subjective terms:

- **Usability:** "intuitive", "user-friendly", "easy to use", "simple"
- **Performance:** "fast", "quick", "responsive", "efficient"
- **Quality:** "good", "high quality", "robust", "reliable"
- **Security:** "secure", "safe", "protected"
- **Maintainability:** "clean", "maintainable", "well-structured"

**Fix:** Replace with measurable criteria.

### Writing Testable Acceptance Criteria

**Template:**
```markdown
- [ ] **AC-N**: [Action] [measurable outcome] [conditions/constraints]
```

**Examples:**

✅ **Good:**
```markdown
- [ ] **AC-1**: System returns search results within 500ms for queries with ≤ 3 terms
- [ ] **AC-2**: Password reset email delivered within 60 seconds of request
- [ ] **AC-3**: API rate limit triggers at 1000 requests per hour per API key
- [ ] **AC-4**: All database migrations complete in < 30 seconds on 1M record dataset
```

### Verification Methods

For each AC, specify how to verify:

```markdown
- [ ] **AC-1**: TOTP generation follows RFC 6238
      **Verification:** Automated unit test validates against RFC 6238 test vectors
      **Test:** test/auth/test_totp.py::test_rfc6238_compliance

- [ ] **AC-2**: QR code provisioning supported
      **Verification:** Manual test with Google Authenticator and Authy
      **Test:** test/auth/test_qr_provisioning.py::test_qr_scan

- [ ] **AC-3**: Backup codes stored using bcrypt
      **Verification:** Automated test validates bcrypt hashing
      **Test:** test/auth/test_backup_codes.py::test_secure_storage
```

---

## Complete Traceability

Requirements MUST have appropriate traceability links based on status.

### Expected Traceability by Status

| Status | Required Links | Optional Links |
|--------|----------------|----------------|
| `:proposed` | `:runnote` (business need) | `:adr` (constraints) |
| `:accepted` | `:runnote`, `:adr` | `:code` (early impl) |
| `:implemented` | `:runnote`, `:adr`, `:code`, `:tests` | - |
| `:deprecated` | (preserve original) | `:superseded-by` |

### Validation Commands

```bash
# Check overall coverage
req-trace summary

# Find missing ADR links
req-trace gaps adr

# Find missing implementation links
req-trace gaps code

# Find missing test links
req-trace gaps tests

# Check specific requirement
req-trace detail REQ-AUTH-001
```

### Interpretation of Coverage Metrics

**ADR Coverage:**
- **100%:** Excellent - all requirements have architectural context
- **80-99%:** Good - minor gaps acceptable for simple requirements
- **<80%:** Poor - missing architectural documentation

**Code Coverage:**
- **100%:** All requirements implemented (if status = :implemented)
- **<100% for :implemented status:** Gap - implemented code not linked
- **0% for :proposed status:** Expected - not yet implemented

**Test Coverage:**
- **100%:** Ideal - all requirements verified
- **<75%:** Concerning - missing verification
- **<50%:** Critical - major testing gaps

### Fixing Traceability Gaps

#### Gap: Missing ADR Links

```bash
req-trace gaps adr
```

**For each requirement:**
1. Search for relevant ADRs:
   ```bash
   adr-search content "authentication"
   adr-search tag security
   ```

2. If ADR exists, add link:
   ```edn
   {:trace {:adr #{"ADR-00042"}}}
   ```

3. If no ADR exists:
   - Evaluate if architectural decision needed
   - Create ADR if yes
   - Link requirement to new ADR

#### Gap: Missing Code Links

```bash
req-trace gaps code
```

**For each requirement with :implemented status:**
1. Find implementation in codebase
2. Add code links:
   ```edn
   {:trace {:code #{"src/auth/mfa.py:45-67"}}}
   ```

**If not implemented:**
- Change status to `:proposed` or `:deferred`

#### Gap: Missing Test Links

```bash
req-trace gaps tests
```

**For each requirement:**
1. Find tests verifying acceptance criteria
2. Add test links:
   ```edn
   {:trace {:tests #{"test/auth/test_mfa.py:12"}}}
   ```

**If no tests exist:**
- Create tests for acceptance criteria
- Flag for test sprint

---

## Common Pitfalls

### Pitfall 1: Creating Requirements Without Searching First

**Problem:** Duplicate or conflicting requirements created without checking existing docs.

**Impact:**
- Wasted effort on duplicates
- Conflicting requirements
- Inconsistent terminology

**Solution:**

ALWAYS search first:
```bash
# Search existing requirements
req-search content "similar-concept"
req-search tag relevant-tag
req-search category :relevant-category

# Search ADRs for constraints
adr-search content "domain-topic"
adr-search tag relevant-tag

# Search RunNotes for business context
runnote-search content "feature-name"
runnote-search tag planning
```

**If you find related requirements:**
- Reference them in Context section
- Explain relationship (extends, specializes, conflicts)
- Avoid duplication
- Consider updating existing requirement instead

### Pitfall 2: Vague Requirements (Not Testable)

**Problem:** Requirements use subjective or unmeasurable terms.

**Examples:**

❌ **Vague:**
- "System shall be user-friendly"
- "Application must be secure"
- "Performance should be good"
- "Interface must be intuitive"

✅ **Specific, testable:**
- "System **SHALL** allow users to complete checkout in 3 clicks or fewer"
- "Application **MUST** authenticate users with multi-factor authentication per :security/authenticity"
- "System **SHALL** respond to 95% of API requests within 200ms under 1000 concurrent users"
- "New users **SHALL** complete first purchase within 10 minutes without consulting help documentation"

**Test:** If you can't write an automated or manual test for it, rewrite the requirement.

### Pitfall 3: Missing Traceability Links

**Problem:** Requirements exist in isolation without links to business need, architecture, code, or tests.

**Consequences:**
- Cannot answer "why did we build this?"
- Cannot perform impact analysis
- Cannot verify implementation
- Cannot identify orphaned requirements

**Solution:**

Maintain complete traceability:
```edn
{:trace {:adr #{"ADR-00042"}                          ; Architectural constraints
         :runnote #{"RunNotes-2025-10-01-Feature"}    ; Business need
         :code #{"src/module/file.py:45-67"}          ; Implementation
         :tests #{"test/module/test_file.py:12"}}}    ; Verification
```

**Regular audits:**
```bash
req-trace summary    # Check coverage percentages
req-trace gaps adr   # Find missing ADR links
req-trace gaps code  # Find missing implementation
req-trace gaps tests # Find missing verification
```

### Pitfall 4: Incorrect RFC 2119 Keywords

**Problem:** Using wrong keyword weakens requirement precision.

**Common mistakes:**

❌ **Wrong:** "System should authenticate users"
**Reality:** Authentication is MUST, not SHOULD
✅ **Right:** "System **MUST** authenticate users"

❌ **Wrong:** "System must have dark mode"
**Reality:** Dark mode is MAY, not MUST
✅ **Right:** "System **MAY** provide dark mode theme"

❌ **Wrong:** "API should return within 200ms"
**Reality:** If this is SLA, it's MUST
✅ **Right:** "API **MUST** return within 200ms at 95th percentile"

**Decision guide:**
- Ask: "Can we ship without this?" If no → MUST
- Ask: "Is this legally required?" If yes → MUST
- Ask: "Is this best practice but negotiable?" If yes → SHOULD
- Ask: "Is this optional enhancement?" If yes → MAY

### Pitfall 5: Compound Requirements

**Problem:** Single requirement contains multiple distinct requirements connected by "and".

**Example:**

❌ **Wrong:**
```markdown
System SHALL authenticate users using OAuth2 AND shall authorize access based on roles AND shall log all security events.
```

**Why wrong:**
- Three separate concerns
- Cannot independently verify
- Cannot independently implement
- Cannot independently trace

**Solution - decompose:**
```markdown
# REQ-AUTH-001 - OAuth2 Authentication
System **SHALL** authenticate users using OAuth2 protocol.

# REQ-AUTH-002 - Role-Based Authorization
System **SHALL** authorize access based on user roles.

# REQ-AUTH-003 - Security Event Logging
System **SHALL** log all authentication and authorization events.
```

**Test for atomicity:** Multiple "AND" clauses or distinct acceptance criteria → decompose.

### Pitfall 6: Missing Context Section

**Problem:** Requirement has no Context section explaining business need or rationale.

**Consequence:**
- New developers don't understand why
- Requirement seems arbitrary
- Business value unclear
- Connection to planning lost

**Solution:**

Always include Context:
```markdown
## Context

Per RunNotes-2025-10-01-RefundProcess-planning, customer service needs refund processing to comply with Consumer Rights Act 2015.

Current manual process takes 5-7 days. Automated processing will reduce to <24 hours and improve customer satisfaction.

Per ADR-00042, all financial calculations use Decimal type to prevent floating-point errors.
```

**Context should answer:**
- **Why:** Business driver for this requirement
- **What problem:** Business impact being solved
- **What constraints:** Architectural decisions applying
- **Where from:** RunNotes or stakeholder source

---

## Decision Framework

When analyzing requirements, ALWAYS ask these questions:

### 1. Is This Testable?

**Question:** Can we objectively verify this requirement was met?

**Test:**
- Can we write an automated test?
- Can we perform manual verification?
- Is success/failure unambiguous?

**If NO:** Rewrite with measurable criteria

**Example:**
- ❌ "Interface must be user-friendly"
- ✅ "Users complete checkout in ≤ 3 clicks"

### 2. Is It Traceable?

**Question:** Can we connect this requirement to its context?

**Check:**
- [ ] Business need documented? (RunNotes link)
- [ ] Architectural constraints identified? (ADR links)
- [ ] Implementation located? (code links)
- [ ] Verification present? (test links)

**If gaps:** Add missing traceability

### 3. Does It Conflict with Existing ADRs?

**Question:** Have we made architectural decisions that constrain this?

**Process:**
```bash
adr-search content "relevant-domain"
adr-search tag technology
```

**If conflict found:**
- Update requirement to match ADR, OR
- Create new ADR superseding old one, OR
- Reject requirement as infeasible

### 4. MUST/SHOULD/MAY - Which Is It Really?

**Questions:**
- Can we ship without this? (if no → MUST)
- Is this legally required? (if yes → MUST)
- Is this best practice but negotiable? (if yes → SHOULD)
- Is this optional enhancement? (if yes → MAY)

**If wrong:** Update `:priority` field

### 5. Is This One Requirement or Multiple?

**Signs of compound requirement:**
- Multiple "AND" clauses
- Multiple distinct acceptance criteria
- Multiple concerns addressed

**If compound:** Decompose into atomic requirements

### 6. Is There a Non-Functional Aspect?

**Check for:**
- Performance constraints
- Security requirements
- Usability requirements
- Reliability requirements
- Maintainability requirements

**If yes:** Add `:nfr-taxonomy` with ISO 25010 classification

---

## See Also

- [CLAUDE.md](../CLAUDE.md) - Quick reference for AI agents
- [README.md](../README.md) - Human-focused usage guide
- [README-WORKFLOWS.md](README-WORKFLOWS.md) - Detailed workflow examples
- [README-FILE-FORMAT.md](README-FILE-FORMAT.md) - Complete format specifications
- [README-INTEGRATION.md](README-INTEGRATION.md) - Integration patterns
- [RFC 2119](https://www.rfc-editor.org/rfc/rfc2119) - Key words for use in RFCs
- [ISO 25010](https://iso25000.com/index.php/en/iso-25000-standards/iso-25010) - Quality model
