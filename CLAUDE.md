# Requirements Management Tools - AI Agent Guide

This guide is for AI agents working with Requirements Management tools. For human documentation, see [README.md](README.md).

## Philosophy and Design Principles

### Requirements as Contract

Requirements are the binding contract between business need and technical implementation. They translate stakeholder desires into engineering specifications that can be designed, built, tested, and verified. Clear, testable, traceable requirements prevent expensive rework and ensure stakeholder alignment.

### Bidirectional Requirements Engineering

Requirements flow in two directions:

**Forward (Elicitation):** Business need → Requirements → Design → Implementation
- Extract requirements from business processes
- Document what stakeholders want
- Define acceptance criteria

**Reverse (Inference):** Implementation → Requirements → Documentation
- Analyze existing systems
- Document what system actually fulfills
- Identify gaps between intent and reality

Both directions are equally valid and necessary. Greenfield projects emphasize elicitation; legacy systems require inference.

### Testability as First Principle

A requirement that cannot be tested is not a requirement - it's a wish. Every requirement MUST be verifiable through objective criteria. If you cannot measure or observe it, you cannot confirm it was delivered.

**Not testable:** "System shall be user-friendly"
**Testable:** "System shall allow users to complete checkout in 3 clicks or less"

### Traceability as First-Class Concern

Requirements exist in a web of relationships:
- **Upward**: To business needs (RunNotes planning sessions)
- **Sideways**: To architectural constraints (ADRs)
- **Downward**: To implementation (code) and verification (tests)

This web enables impact analysis (what breaks if we change this?), coverage analysis (what's not implemented?), and audit trails (why did we build this?).

### RFC 2119 Semantics: Precision Over Ambiguity

Requirements use RFC 2119 keywords with precise meaning:

- **MUST / SHALL**: Absolute requirement, non-negotiable
- **SHOULD**: Strong preference, deviations need justification
- **MAY**: Optional, discretionary
- **MUST NOT / SHALL NOT**: Absolute prohibition

These keywords force prioritization clarity. "Should we do this?" becomes "Is this MUST, SHOULD, or MAY?"

### ISO 25010 Quality Model for NFRs

Non-functional requirements (NFRs) use ISO 25010 taxonomy to prevent vague quality statements:

**Vague:** "System must be secure"
**Precise:** "System MUST implement authentication with :security/authenticity and :security/accountability characteristics per ISO 25010"

The taxonomy provides a shared vocabulary for quality attributes across projects and teams.

### Atomic Requirements: One Concern Per File

Each requirement file addresses exactly one requirement. Compound requirements ("System shall X and Y and Z") should be decomposed into multiple atomic requirements. This enables:
- Independent verification
- Clear ownership
- Precise traceability
- Granular status tracking

## Tool Overview

### req-validate

Validates requirement format, metadata completeness, traceability structure, and detects duplicate requirement IDs.

**When to use:**
- Before committing new requirements
- In CI/CD pipelines (use `--ci` flag)
- During code review
- In pre-commit hooks (use `--check-new`)

**Key capabilities:**
- Checks EDN metadata syntax and required fields
- Detects duplicate REQ-IDs
- Validates priority, status, type values
- Confirms traceability structure
- Can check only staged files or PR files

**Reference:** See README.md section "Validate Requirements" for complete command documentation.

### req-search

Search and discovery tool for finding requirements by tag, content, priority, status, category, or traceability links.

**When to use:**
- Before creating new requirements (check for duplicates)
- Finding requirements on a specific topic
- Discovering tag and category taxonomy
- Finding requirements linked to ADRs or RunNotes
- Auditing requirement status across project

**Key capabilities:**
- List all requirements with summaries
- Search by tag, content, category, priority, status, type
- Search by ADR or RunNotes links
- Show tag and category usage statistics
- Generate requirement summaries

**Reference:** See README.md section "Search Requirements" for complete command documentation.

### req-trace

Traceability analysis and gap detection for requirements coverage.

**When to use:**
- Identifying requirements missing ADR links
- Finding requirements missing code or test links
- Generating traceability coverage reports
- Analyzing implementation status
- Creating traceability matrices

**Key capabilities:**
- Summary statistics on traceability coverage
- Gap analysis for ADR, code, test, RunNotes links
- Detailed traceability for specific requirements
- Traceability matrix generation

**Reference:** See README.md section "Traceability Analysis" for complete command documentation.

### req-fix-metadata

Automated fixer for common metadata issues across all requirements.

**When to use:**
- Migrating legacy requirements to new format
- Fixing bulk metadata issues after format changes
- Cleaning up requirements after manual edits
- Preparing requirements for validation

**Key capabilities:**
- Adds missing `:metadata` markers
- Converts trace strings to sets
- Adds missing Context sections
- Preview mode (dry-run) before applying

**Reference:** See README.md section "Fix Metadata Issues" for complete command documentation.

## Agent Workflows

### Mode 1: Business Process → Requirements (Elicitation)

When given business process descriptions, planning documents, or stakeholder requests:

#### Step 1: Gather Context

**Search RunNotes for planning sessions:**
```bash
runnote-search tag planning
runnote-search content "feature-name"
```

Read relevant RunNotes to understand:
- Business objectives
- User stories
- Success criteria
- Constraints and assumptions

**Search ADRs for architectural constraints:**
```bash
adr-search content "relevant-domain"
adr-search tag architecture
```

Identify architectural decisions that constrain requirements:
- Technology choices
- Integration patterns
- Security models
- Data models

**Search existing requirements to avoid duplicates:**
```bash
req-search content "similar-concept"
req-search tag relevant-tag
req-search category :relevant-category
```

#### Step 2: Extract Functional Requirements

Functional requirements specify WHAT the system does:

**Ask:**
- What business capabilities must the system provide?
- What workflows must be supported?
- What data must be processed?
- What business rules apply?
- What integrations are required?

**Example extraction from business process:**

Business Process: "Customer service representatives need to process refunds for orders within 30 days of purchase."

Functional Requirements:
- REQ-REFUND-001: System MUST calculate refund amount based on original order total
- REQ-REFUND-002: System MUST verify order was placed within 30 days
- REQ-REFUND-003: System MUST initiate payment reversal through payment gateway
- REQ-REFUND-004: System MUST update order status to "refunded"
- REQ-REFUND-005: System MUST send refund confirmation email to customer

#### Step 3: Identify Non-Functional Requirements

NFRs specify HOW WELL the system performs using ISO 25010 taxonomy:

**Ask:**
- Performance: Response time, throughput, capacity requirements?
- Security: Authentication, authorization, confidentiality, integrity needs?
- Usability: Accessibility, learnability, error prevention requirements?
- Reliability: Availability, fault tolerance, recoverability needs?
- Maintainability: Testability, modularity, analyzability requirements?
- Compatibility: Integration, interoperability needs?
- Portability: Adaptability, installability requirements?

**Example NFRs for refund process:**

- REQ-REFUND-NFR-001: Refund calculation MUST complete within 2 seconds (:performance-efficiency/time-behavior)
- REQ-REFUND-NFR-002: Refund operations MUST be logged with user ID and timestamp (:security/accountability)
- REQ-REFUND-NFR-003: Refund UI MUST be accessible per WCAG 2.1 AA (:usability/accessibility)
- REQ-REFUND-NFR-004: Failed payment reversals MUST retry automatically (:reliability/fault-tolerance)

#### Step 4: Create Requirement Specifications

For each requirement:

1. **Copy template:**
   ```bash
   cp ~/.req/template/functional.md doc/req/REQ-CATEGORY-NNN-title.md
   # or
   cp ~/.req/template/non-functional.md doc/req/REQ-CATEGORY-NFR-NNN-title.md
   ```

2. **Fill metadata:**
   ```edn
   {:req-id "REQ-REFUND-001"
    :type :functional
    :category :payments
    :priority :must
    :status :proposed
    :tag #{:refunds :payments :customer-service}
    :trace {:adr #{"ADR-00042"}
            :runnote #{"RunNotes-2025-10-01-RefundProcess-planning"}}}
   ```

3. **Write requirement statement with RFC 2119 keyword:**
   ```markdown
   ## Requirement Statement

   System **MUST** calculate refund amount based on original order total, including all items, taxes, and shipping charges.
   ```

4. **Document context:**
   ```markdown
   ## Context

   Per RunNotes-2025-10-01-RefundProcess-planning, customer service needs ability to process refunds. Per ADR-00042, all financial calculations use Decimal type to prevent floating-point errors.

   Business policy requires full refund including shipping for orders within 30 days.
   ```

5. **Define testable acceptance criteria:**
   ```markdown
   ## Acceptance Criteria

   - [ ] **AC-1**: Refund amount equals sum of all line items from original order
   - [ ] **AC-2**: Refund amount includes original tax calculation
   - [ ] **AC-3**: Refund amount includes original shipping cost
   - [ ] **AC-4**: Refund calculation uses Decimal type (no floating-point)
   - [ ] **AC-5**: Partial refunds are NOT supported (out of scope)
   ```

#### Step 5: Validate Requirements

Before completing elicitation work:

```bash
# Validate format and metadata
req-validate

# Check traceability coverage
req-trace summary

# Verify no duplicate REQ-IDs
req-search list
```

#### Step 6: Create Requirements Matrix

Generate summary showing priority and status:

```markdown
## Requirements Summary

### Functional Requirements (Priority: MUST)
- REQ-REFUND-001: Calculate refund amount [proposed]
- REQ-REFUND-002: Verify 30-day window [proposed]
- REQ-REFUND-003: Initiate payment reversal [proposed]

### Non-Functional Requirements
- REQ-REFUND-NFR-001: 2-second response time [proposed]
- REQ-REFUND-NFR-002: Audit logging [proposed]
```

### Mode 2: Code/Docs → Requirements (Inference)

When analyzing existing systems to document what they fulfill:

#### Step 1: Analyze Implementation

**Read code and documentation:**
- Read source files
- Read existing documentation (if any)
- Read test files
- Read configuration files

**Identify implemented capabilities:**
- What functions/APIs exist?
- What workflows are supported?
- What business logic is implemented?
- What quality characteristics are present?

#### Step 2: Search for Rationale

**Search ADRs for design decisions:**
```bash
adr-search content "component-name"
adr-search tag relevant-domain
```

Understanding architectural decisions helps explain:
- Why this approach?
- What constraints exist?
- What alternatives were rejected?

**Search RunNotes for implementation context:**
```bash
runnote-search content "feature-name"
runnote-search tag implementation
```

#### Step 3: Infer Functional Requirements

For each implemented capability, ask:
- What business need does this fulfill?
- What is the requirement statement?
- Is this MUST, SHOULD, or MAY?

**Example from code:**

Code: `calculate_refund(order) -> Decimal`

Inferred Requirement:
```markdown
# REQ-REFUND-001 - Calculate Refund Amount

## Requirement Statement

System **MUST** calculate refund amount based on original order total.

## Context

Implementation found in `src/refunds/calculator.py:45-67`. Uses Decimal type per ADR-00042.

## Acceptance Criteria

- [ ] **AC-1**: Returns Decimal type (observed in implementation)
- [ ] **AC-2**: Includes line items, tax, shipping (observed)
```

Add implementation traceability:
```edn
{:trace {:code #{"src/refunds/calculator.py:45-67"}
         :tests #{"test/refunds/calculator_test.py:12"}
         :adr #{"ADR-00042"}}}
```

#### Step 4: Identify Non-Functional Characteristics

Analyze code for quality attributes:

**Security characteristics:**
- Authentication/authorization checks → :security/authenticity
- Audit logging → :security/accountability
- Encryption → :security/confidentiality

**Performance characteristics:**
- Caching → :performance-efficiency/resource-utilization
- Async processing → :performance-efficiency/time-behavior
- Connection pooling → :performance-efficiency/capacity

**Reliability characteristics:**
- Retry logic → :reliability/fault-tolerance
- Error handling → :reliability/recoverability
- Monitoring → :reliability/maturity

**Example NFR from code:**

Code shows retry decorator with 3 attempts:
```python
@retry(max_attempts=3, backoff=exponential)
def reverse_payment(payment_id):
    ...
```

Inferred NFR:
```markdown
# REQ-REFUND-NFR-004 - Payment Reversal Fault Tolerance

## Requirement Statement

System **MUST** retry failed payment reversals with exponential backoff.

## Context

Implementation uses retry decorator in `src/payments/reversal.py:23`. Addresses payment gateway intermittent failures.

## Acceptance Criteria

- [ ] **AC-1**: Maximum 3 retry attempts (observed)
- [ ] **AC-2**: Exponential backoff between retries (observed)
- [ ] **AC-3**: Final failure logged and alerted (observed in line 45)
```

Add to metadata:
```edn
{:nfr-taxonomy {:iso-25010 #{:reliability/fault-tolerance}}}
```

#### Step 5: Document Implementation Status

For inferred requirements, set status to `:implemented`:

```edn
{:req-id "REQ-REFUND-001"
 :status :implemented
 :trace {:code #{"src/refunds/calculator.py:45-67"}
         :tests #{"test/refunds/calculator_test.py:12"}}}
```

#### Step 6: Identify Gaps

**Compare inferred requirements to business needs:**
- What's implemented but not needed? (over-engineering)
- What's needed but not implemented? (missing features)
- What's partially implemented? (incomplete features)

**Document missing requirements:**
```markdown
# REQ-REFUND-006 - Partial Refund Support

## Requirement Statement

System **SHOULD** support partial refunds for individual line items.

## Context

Current implementation (REQ-REFUND-001) only supports full order refunds. Business has requested partial refund capability for damaged items.

## Status

:proposed - Not yet implemented. Gap identified during requirements inference.
```

### Mode 3: Requirements Validation

When reviewing requirements for quality and completeness:

#### Testability Checks

For each requirement, ask:
- Can this be verified objectively?
- Are acceptance criteria measurable?
- Can we write a test for this?

**Red flags:**
- Subjective terms: "user-friendly", "intuitive", "fast", "secure"
- Vague quantities: "many", "few", "quickly"
- Missing criteria: No acceptance criteria section

**Fix by adding measurable criteria:**

Before: "System shall be fast"
After: "System MUST respond to search queries within 500ms at 95th percentile under 1000 concurrent users"

#### Completeness Checks

**Required metadata fields:**
- [ ] `:req-id` present and unique
- [ ] `:type` specified (functional/non-functional/constraint)
- [ ] `:category` specified
- [ ] `:priority` specified (must/shall/should/may)
- [ ] `:status` specified (proposed/accepted/deprecated/implemented)
- [ ] `:tag` set present

**Required content sections:**
- [ ] Requirement Statement with RFC 2119 keyword
- [ ] Context section explaining business need
- [ ] Acceptance Criteria with testable conditions

**Run automated checks:**
```bash
req-validate
```

#### Consistency Checks with ADRs

Requirements must not conflict with architectural decisions:

**Search ADRs for constraints:**
```bash
adr-search content "relevant-domain"
adr-search tag technology
```

**Check for conflicts:**

Requirement: "System MUST use MongoDB for data storage"
ADR-00042: "We will use PostgreSQL for ACID guarantees"

**Conflict!** Either:
1. Change requirement to match ADR
2. Create new ADR superseding old one
3. Reject requirement as architecturally infeasible

**Add ADR traceability:**
```edn
{:trace {:adr #{"ADR-00042"}}}
```

#### Traceability Checks

**Run gap analysis:**
```bash
req-trace summary    # Overview of coverage
req-trace gaps adr   # Missing ADR links
req-trace gaps code  # Missing implementation links
req-trace gaps tests # Missing test links
```

**Expected traceability:**

| Status | Expected Links |
|--------|----------------|
| :proposed | ADR (constraints), RunNotes (business need) |
| :accepted | ADR, RunNotes, possibly early code |
| :implemented | ADR, RunNotes, code, tests |

**Add missing links:**
```edn
{:trace {:adr #{"ADR-00042"}
         :runnote #{"RunNotes-2025-10-01-Feature-planning"}
         :code #{"src/module/file.ext:45-67"}
         :tests #{"test/module/test_file.ext:12"}}}
```

#### Priority Validation

Verify RFC 2119 keywords are used correctly:

**MUST/SHALL criteria:**
- Absolute requirement
- System unusable without it
- Legal/regulatory requirement
- Critical business function

**SHOULD criteria:**
- Strong preference
- Deviations need justification
- Important but not critical
- Best practice

**MAY criteria:**
- Optional enhancement
- Nice to have
- Future consideration
- Discretionary

**Check for priority inflation:**
- Are ALL requirements marked MUST? (probably not true)
- Are there any SHOULD/MAY? (probably should be)
- Force prioritization by limiting MUST to truly critical

#### Atomic Requirement Check

Each file should contain ONE requirement:

**Violation:**
```markdown
# REQ-AUTH-001 - Authentication

System MUST support username/password authentication AND
system MUST support OAuth2 AND system MUST support SAML.
```

**Fix by decomposition:**
```markdown
# REQ-AUTH-001 - Username/Password Authentication
System MUST support username/password authentication.

# REQ-AUTH-002 - OAuth2 Authentication
System MUST support OAuth2 authentication.

# REQ-AUTH-003 - SAML Authentication
System MUST support SAML authentication.
```

## Integration Patterns

### With ADR Tools

Requirements and ADRs form a bidirectional relationship:

#### Requirements → ADRs

Requirements create architectural constraints that need decisions:

**Workflow:**
1. Elicit requirement with quality attributes
2. Recognize architectural implications
3. Search for relevant ADRs: `adr-search content "topic"`
4. If no ADR exists, identify need for new architectural decision
5. Link requirement to ADR (once created)

**Example:**

Requirement: "System MUST handle 10,000 requests/second"

Architectural implications:
- Caching strategy needed
- Database scaling approach needed
- Load balancing needed

Creates need for ADRs:
- ADR-00XXX: Caching Strategy
- ADR-00YYY: Database Sharding Approach
- ADR-00ZZZ: Load Balancer Selection

Link in requirement:
```edn
{:trace {:adr #{"ADR-00XXX" "ADR-00YYY" "ADR-00ZZZ"}}}
```

#### ADRs → Requirements

Architectural decisions create implementation requirements:

**Workflow:**
1. ADR documents architectural decision
2. Decision implies implementation requirements
3. Create requirements specifying what must be built
4. Link requirements to driving ADR

**Example:**

ADR-00042: "Use event sourcing for audit trail"

Implied requirements:
- REQ-EVENTS-001: System MUST persist all state changes as events
- REQ-EVENTS-002: System MUST support event replay
- REQ-EVENTS-003: System MUST maintain event version compatibility

Link in requirements:
```edn
{:trace {:adr #{"ADR-00042"}}}
```

#### Search ADRs Before Creating Requirements

**ALWAYS search ADRs first:**
```bash
adr-search content "authentication"
adr-search tag security
```

ADRs may:
- Establish constraints your requirements must respect
- Document decisions your requirements implement
- Reveal conflicts with proposed requirements
- Provide context for why requirements exist

#### Link Requirements to ADRs in Metadata

```edn
{:req-id "REQ-AUTH-001"
 :trace {:adr #{"ADR-00042" "ADR-00043"}}}
```

Multiple ADRs may constrain a single requirement:
- ADR-00042: Authentication technology choice
- ADR-00043: Session management approach

#### Use req-trace to Find ADR Gaps

```bash
req-trace gaps adr
```

Shows requirements missing ADR links. For each gap:
1. Search for relevant ADRs
2. Add link if ADR exists
3. Identify need for new ADR if missing

### With RunNotes Tools

RunNotes document the journey; requirements document the contract.

#### RunNotes → Requirements (Elicitation)

Planning phase RunNotes contain the raw material for requirements:

**Search for planning sessions:**
```bash
runnote-search tag planning
runnote-search content "feature-name"
```

**Extract requirements from RunNotes:**

RunNotes content:
```yaml
objectives:
  - Enable customer service to process refunds
  - Support 30-day refund window
  - Integrate with payment gateway
```

Generated requirements:
- REQ-REFUND-001: Calculate refund amount
- REQ-REFUND-002: Verify 30-day window
- REQ-REFUND-003: Initiate payment reversal

**Link requirements to source RunNotes:**
```edn
{:trace {:runnote #{"RunNotes-2025-10-01-RefundProcess-planning"}}}
```

#### Requirements → RunNotes (Context)

Requirements reference RunNotes for business context:

```markdown
## Context

Per RunNotes-2025-10-01-RefundProcess-planning, customer service representatives need ability to process refunds within 30 days of purchase to comply with consumer protection regulations.
```

#### Traceability Workflow

1. **Planning Phase (RunNotes):**
   - Document business need
   - Capture stakeholder requirements
   - Identify constraints and assumptions

2. **Requirements Phase:**
   - Elicit requirements from RunNotes
   - Link requirements to source: `:trace {:runnote #{...}}`
   - Link to architectural constraints: `:trace {:adr #{...}}`

3. **Implementation Phase:**
   - Implement features
   - Update requirements with code links: `:trace {:code #{...}}`
   - Reference requirements in code comments

4. **Testing Phase:**
   - Write tests validating acceptance criteria
   - Link requirements to tests: `:trace {:tests #{...}}`

5. **Verification:**
   ```bash
   req-trace detail REQ-REFUND-001
   ```

   Shows complete traceability:
   - RunNotes: Why we need this
   - ADRs: Architectural constraints
   - Code: Implementation
   - Tests: Verification

### With Code and Tests

#### Requirements → Code (Implementation Traceability)

As requirements are implemented, link to code:

```edn
{:trace {:code #{"src/refunds/calculator.py:45-67"
                 "src/refunds/api.py:123-145"}}}
```

**Format:** `path/to/file.ext:line-range` or `path/to/file.ext:line`

**Best practice:**
- Update links when code moves
- Add code comments linking back to requirement
- Keep links precise (specific line ranges)

#### Requirements → Tests (Verification Traceability)

Each acceptance criterion should map to tests:

```markdown
## Acceptance Criteria

- [ ] **AC-1**: Refund amount equals sum of line items
      Test: `test/refunds/calculator_test.py:12-25`

- [ ] **AC-2**: Includes tax in calculation
      Test: `test/refunds/calculator_test.py:27-35`
```

Link in metadata:
```edn
{:trace {:tests #{"test/refunds/calculator_test.py:12-35"}}}
```

#### Bidirectional References

**In requirement file:**
```edn
{:trace {:code #{"src/auth/mfa.py:45-67"}}}
```

**In code file:**
```python
# Implements REQ-AUTH-001: Multi-Factor Authentication
def generate_totp(secret: str) -> str:
    """
    Generates TOTP code per RFC 6238.
    See: doc/req/REQ-AUTH-001-multi-factor-authentication.md
    """
    ...
```

**In test file:**
```python
class TestMFA(unittest.TestCase):
    """
    Validates REQ-AUTH-001 acceptance criteria.
    See: doc/req/REQ-AUTH-001-multi-factor-authentication.md
    """

    def test_totp_follows_rfc6238(self):
        """Validates AC-1: TOTP follows RFC 6238"""
        ...
```

#### Gap Analysis with req-trace

**Find requirements missing implementation:**
```bash
req-trace gaps code
```

Shows requirements with no `:code` links - potentially not implemented.

**Find requirements missing tests:**
```bash
req-trace gaps tests
```

Shows requirements with no `:tests` links - potentially not verified.

**Verify coverage:**
```bash
req-trace summary
```

Shows percentage of requirements with code and test traceability.

## Quality Enforcement

### RFC 2119 Keyword Precision

**Enforce correct usage:**

| Keyword | Meaning | When to Use |
|---------|---------|-------------|
| MUST / SHALL | Absolute requirement | Legal requirement, system unusable without it, critical business function |
| MUST NOT / SHALL NOT | Absolute prohibition | Security violation, legal prohibition, data corruption risk |
| SHOULD | Strong recommendation | Best practice, important but not critical, deviations need justification |
| SHOULD NOT | Strong discouragement | Problematic but not prohibited, alternatives preferred |
| MAY | Optional | Nice to have, future consideration, discretionary |

**Check for violations:**

Wrong: "System should authenticate users"
Right: "System MUST authenticate users before allowing access"

Wrong: "System must be fast"
Right: "System SHOULD respond within 500ms at 95th percentile"

### Atomic Requirements Enforcement

Each requirement file MUST contain exactly one requirement:

**Violation patterns:**
- Multiple SHALL/MUST statements
- "AND" connecting different concerns
- Multiple unrelated acceptance criteria

**Decomposition strategy:**

Compound requirement:
```markdown
System SHALL authenticate users AND authorize access AND log audit trail.
```

Decomposed:
```markdown
REQ-AUTH-001: System SHALL authenticate users
REQ-AUTH-002: System SHALL authorize access based on roles
REQ-AUTH-003: System SHALL log authentication and authorization events
```

### Testable Acceptance Criteria

Every acceptance criterion MUST be:
- **Measurable**: Can be verified objectively
- **Unambiguous**: Single interpretation
- **Observable**: Can be tested
- **Binary**: Pass or fail, no gray area

**Non-testable criteria:**
- "User interface is intuitive"
- "System performs well"
- "Code is maintainable"

**Testable criteria:**
- "New users complete first task within 5 minutes without help documentation"
- "System processes 1000 requests/second at 95th percentile latency < 200ms"
- "Cyclomatic complexity < 10 for all functions"

### Complete Traceability

Requirements MUST have appropriate traceability links based on status:

| Status | Required Links | Optional Links |
|--------|----------------|----------------|
| :proposed | :runnote (business need) | :adr (constraints) |
| :accepted | :runnote, :adr | :code (early implementation) |
| :implemented | :runnote, :adr, :code, :tests | - |
| :deprecated | (original links preserved) | :superseded-by |

**Validate with:**
```bash
req-trace summary
req-trace gaps adr
req-trace gaps code
req-trace gaps tests
```

### ISO 25010 Taxonomy for NFRs

Non-functional requirements MUST map to ISO 25010 quality characteristics:

**Available characteristics:**

**Functional Suitability:**
- :functional-suitability/completeness
- :functional-suitability/correctness
- :functional-suitability/appropriateness

**Performance Efficiency:**
- :performance-efficiency/time-behavior
- :performance-efficiency/resource-utilization
- :performance-efficiency/capacity

**Compatibility:**
- :compatibility/co-existence
- :compatibility/interoperability

**Usability:**
- :usability/learnability
- :usability/operability
- :usability/user-error-protection
- :usability/accessibility
- :usability/ui-aesthetics

**Reliability:**
- :reliability/maturity
- :reliability/availability
- :reliability/fault-tolerance
- :reliability/recoverability

**Security:**
- :security/confidentiality
- :security/integrity
- :security/non-repudiation
- :security/accountability
- :security/authenticity

**Maintainability:**
- :maintainability/modularity
- :maintainability/reusability
- :maintainability/analyzability
- :maintainability/modifiability
- :maintainability/testability

**Portability:**
- :portability/adaptability
- :portability/installability
- :portability/replaceability

**Usage in metadata:**
```edn
{:nfr-taxonomy {:iso-25010 #{:performance-efficiency/time-behavior
                             :reliability/fault-tolerance}}}
```

## Common Pitfalls

### Avoid: Creating Requirements Without Searching First

**Problem:**
Creating duplicate or conflicting requirements because you didn't check existing documentation.

**Solution:**
ALWAYS run these searches first:

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

If you find related requirements:
- Reference them in new requirement
- Explain relationship (extends, specializes, conflicts)
- Avoid duplication

### Avoid: Vague Requirements (Not Testable)

**Problem:**
Requirements use subjective or unmeasurable terms.

**Vague examples:**
- "System shall be user-friendly"
- "Application must be secure"
- "Performance should be good"
- "Interface must be intuitive"

**Specific, testable versions:**
- "System SHALL allow users to complete checkout in 3 clicks or fewer"
- "Application MUST authenticate users with multi-factor authentication per :security/authenticity"
- "System SHALL respond to 95% of API requests within 200ms under 1000 concurrent users"
- "New users SHALL complete first purchase within 10 minutes without consulting help documentation (measured via usability testing)"

**Test:** If you can't write an automated or manual test for it, rewrite the requirement.

### Avoid: Missing Traceability Links

**Problem:**
Requirements exist in isolation without links to business need, architecture, code, or tests.

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

### Avoid: Incorrect RFC 2119 Keywords

**Problem:**
Using wrong keyword weakens requirement precision.

**Common mistakes:**

Wrong: "System should authenticate users"
Reality: This is MUST, not SHOULD - authentication is not optional
Right: "System MUST authenticate users"

Wrong: "System must have dark mode"
Reality: This is MAY, not MUST - dark mode is nice to have
Right: "System MAY provide dark mode theme"

Wrong: "API should return within 200ms"
Reality: If this is a contractual SLA, it's MUST
Right: "API MUST return within 200ms at 95th percentile"

**Decision guide:**
- Ask: "Can we ship without this?" If no → MUST
- Ask: "Is this a legal/regulatory requirement?" If yes → MUST
- Ask: "Is this best practice but negotiable?" If yes → SHOULD
- Ask: "Is this optional enhancement?" If yes → MAY

### Avoid: Compound Requirements (Multiple Concerns)

**Problem:**
Single requirement file contains multiple distinct requirements connected by "and".

**Violation:**
```markdown
# REQ-AUTH-001 - Authentication and Authorization

System SHALL authenticate users using OAuth2 AND shall authorize access based on roles AND shall log all security events.
```

**Why this is wrong:**
- Three separate concerns: authentication, authorization, logging
- Cannot independently verify
- Cannot independently implement
- Cannot independently trace

**Solution - decompose:**
```markdown
# REQ-AUTH-001 - OAuth2 Authentication
System SHALL authenticate users using OAuth2 protocol.

# REQ-AUTH-002 - Role-Based Authorization
System SHALL authorize access based on user roles.

# REQ-AUTH-003 - Security Event Logging
System SHALL log all authentication and authorization events.
```

Now each requirement is:
- Independently testable
- Independently traceable
- Independently implementable

**Test for atomicity:** Does the requirement have multiple distinct acceptance criteria addressing different concerns? If yes, decompose.

### Avoid: Missing Context Section

**Problem:**
Requirement has no Context section explaining business need or rationale.

**Consequence:**
- New developers don't understand why
- Requirement seems arbitrary
- Business value unclear
- Connection to planning lost

**Solution:**
Always include Context:

```markdown
## Context

Per RunNotes-2025-10-01-RefundProcess-planning, customer service representatives need ability to process refunds within 30 days to comply with consumer protection regulations (Consumer Rights Act 2015).

Current manual refund process takes 5-7 days and causes customer complaints. Automated refund processing will reduce time to <24 hours and improve customer satisfaction scores.

Per ADR-00042, all financial calculations use Decimal type to prevent floating-point rounding errors in monetary amounts.
```

Context should answer:
- Why is this requirement needed? (business driver)
- What problem does it solve? (business impact)
- What constraints apply? (architectural decisions)
- Where did this come from? (RunNotes, stakeholder)

## File Format Reference

### Requirements File Structure

Requirements use markdown with embedded EDN metadata:

```markdown
# REQ-[CATEGORY]-[NUMBER] - [Title]

```edn :metadata
{:req-id "REQ-[CATEGORY]-[NUMBER]"
 :type :functional
 :category :your-category
 :priority :must
 :status :proposed
 :tag #{:tag1 :tag2}
 :trace {:adr #{...} :runnote #{...} :code #{...} :tests #{...}}}
```

## Requirement Statement

**[MUST|SHALL|SHOULD|MAY]** [Clear, testable statement]

## Context

[Business need, rationale, related ADRs/RunNotes, constraints]

## Acceptance Criteria

- [ ] **AC-1**: [Testable, measurable criterion]
- [ ] **AC-2**: [Another criterion]
- [ ] **AC-3**: [Another criterion]
```

### Metadata Fields

**Required fields:**
- `:req-id` - Unique requirement ID (format: REQ-[CATEGORY]-[NUMBER])
- `:type` - `:functional`, `:non-functional`, `:constraint`, or `:integration`
- `:category` - Project-specific category (keyword or vector)
- `:priority` - `:must`, `:shall`, `:should`, or `:may` (RFC 2119)
- `:status` - `:proposed`, `:accepted`, `:deprecated`, `:deferred`, or `:implemented`
- `:tag` - Set of keyword tags

**Optional fields:**
- `:updated` - Last update date (YYYY-MM-DD string)
- `:trace` - Traceability map with sets:
  - `:adr` - Set of ADR references (strings)
  - `:runnote` - Set of RunNotes references (strings)
  - `:code` - Set of code file references (strings with line ranges)
  - `:tests` - Set of test file references (strings)
- `:nfr-taxonomy` - Quality attribute taxonomy map:
  - `:iso-25010` - Set of ISO 25010 characteristics (keywords)
  - `:furps+` - Set of FURPS+ categories (keywords)
- `:acceptance-criteria` - Structured acceptance criteria (project-defined)

### File Naming Convention

```
REQ-[CATEGORY]-[NUMBER]-title.md

Examples:
REQ-AUTH-001-multi-factor-authentication.md
REQ-PERF-042-response-time-sla.md
REQ-UI-015-accessibility-compliance.md

Hyphenated categories (multi-word):
REQ-SECURITY-CRYPTO-001-cryptographic-agility.md
REQ-INTEGRATION-VERSION-CONTROL-001-policy-versioning.md

Non-functional requirements:
REQ-AUTH-NFR-001-authentication-performance.md
REQ-DATA-NFR-005-backup-recovery.md
```

**Format rules:**
- **CATEGORY**: Uppercase, may include hyphens for multi-word
- **NUMBER**: 3-5 digit sequence (001, 042, 00123)
- **title**: Lowercase with hyphens

### Traceability Structure

Traceability uses nested map with sets:

```edn
{:trace
 {:adr #{"ADR-00042" "ADR-00043"}
  :runnote #{"RunNotes-2025-10-01-Feature-planning"
             "RunNotes-2025-10-01-Feature-research"}
  :code #{"src/auth/mfa.py:45-67"
          "src/auth/api.py:123-145"}
  :tests #{"test/auth/test_mfa.py:12-45"
           "test/auth/test_api.py:67"}}}
```

**Key points:**
- Each field (`:adr`, `:runnote`, `:code`, `:tests`) is a SET of strings
- Use `#{}` for sets, not `[]` (vectors) or `""` (single string)
- Code and test references include line numbers/ranges
- Multiple references allowed per field

### ISO 25010 Taxonomy Structure

For non-functional requirements:

```edn
{:nfr-taxonomy
 {:iso-25010 #{:performance-efficiency/time-behavior
               :performance-efficiency/resource-utilization
               :reliability/fault-tolerance
               :security/authenticity
               :usability/accessibility}
  :furps+ #{:performance :reliability :usability}}}
```

**Format:**
- `:iso-25010` - Set of qualified keywords (`:category/subcategory`)
- `:furps+` - Set of FURPS+ category keywords (optional supplement)

### EDN Syntax Quick Reference

**Maps:**
```edn
{:key1 "value1"
 :key2 42
 :key3 #{:set :of :keywords}}
```

**Keywords:**
```edn
:functional :must :proposed :authentication
:performance-efficiency/time-behavior
```

**Strings:**
```edn
"2025-10-14"
"REQ-AUTH-001"
"src/module/file.py:45-67"
```

**Sets:**
```edn
#{:tag1 :tag2 :tag3}
#{"ADR-00042" "ADR-00043"}
```

**Common mistakes:**
```edn
# Wrong
{:priority "must"}           → {:priority :must}
{:tag [:authentication]}     → {:tag #{:authentication}}
{:trace {:adr "ADR-001"}}   → {:trace {:adr #{"ADR-001"}}}
{:date 2025-10-14}          → {:date "2025-10-14"}

# Right
{:priority :must}
{:tag #{:authentication}}
{:trace {:adr #{"ADR-001"}}}
{:date "2025-10-14"}
```

For complete specifications, see [README.md section "Requirements Format"](README.md#requirements-format).

## Decision Framework

When analyzing requirements, ALWAYS ask these questions:

### 1. Is This Testable?

Can we objectively verify this requirement was met?

**Test:**
- Can we write an automated test?
- Can we perform manual verification?
- Is success/failure unambiguous?

**If no:** Rewrite with measurable criteria

### 2. Is It Traceable?

Can we connect this requirement to its context?

**Check:**
- [ ] Business need documented? (RunNotes link)
- [ ] Architectural constraints identified? (ADR links)
- [ ] Implementation located? (code links)
- [ ] Verification present? (test links)

**If gaps:** Add missing traceability

### 3. Does It Conflict with Existing ADRs?

Have we made architectural decisions that constrain this requirement?

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

Have we correctly classified priority per RFC 2119?

**Questions:**
- Can we ship without this? (if no → MUST)
- Is this legally required? (if yes → MUST)
- Is this best practice but negotiable? (if yes → SHOULD)
- Is this optional enhancement? (if yes → MAY)

**If wrong:** Update `:priority` field

### 5. Is This One Requirement or Multiple?

Is this requirement atomic or compound?

**Signs of compound requirement:**
- Multiple "AND" clauses
- Multiple distinct acceptance criteria
- Multiple concerns addressed

**If compound:** Decompose into atomic requirements

### 6. Is There a Non-Functional Aspect?

Does this requirement have quality attribute characteristics?

**Check for:**
- Performance constraints
- Security requirements
- Usability requirements
- Reliability requirements
- Maintainability requirements
- Compatibility requirements

**If yes:** Add `:nfr-taxonomy` with ISO 25010 classification

## Appendix: Integration with Agent Ecosystem

### Coordination with Other Agents

**software-architect agent:**
- Architect identifies architectural implications of requirements
- requirements-analyst ensures requirements don't conflict with ADRs
- Architect reviews NFR taxonomy for technical accuracy

**documentation-manager agent:**
- Validates requirement documentation completeness
- Checks traceability links are current
- Detects drift between requirements and implementation
- Ensures requirements documentation stays synchronized

**test-strategist agent:**
- Reviews acceptance criteria for testability
- Links test cases to requirements
- Validates test coverage matches requirements
- Ensures all requirements are verified

**adr-curator agent:**
- Documents architectural decisions driven by requirements
- Ensures requirements reference relevant ADRs
- Identifies when requirements need new ADRs
- Maintains bidirectional traceability

### When Multiple Agents Interact

**Scenario: New Feature Requiring Requirements**

1. **Planning Phase:**
   - RunNotes document business need
   - requirements-analyst searches existing requirements and ADRs

2. **Requirements Phase:**
   - requirements-analyst elicits requirements from RunNotes
   - software-architect identifies architectural implications
   - adr-curator creates ADRs for significant decisions
   - requirements-analyst links requirements to ADRs

3. **Implementation Phase:**
   - Developers implement features
   - requirements-analyst updates requirements with code traceability
   - documentation-manager validates links stay current

4. **Testing Phase:**
   - test-strategist validates acceptance criteria
   - Tests implemented per criteria
   - requirements-analyst updates requirements with test traceability

5. **Verification:**
   - All agents verify their concerns in requirements
   - requirements-analyst runs `req-trace summary` to confirm complete traceability

**Scenario: Legacy System Documentation**

1. **Analysis Phase:**
   - requirements-analyst infers requirements from code
   - software-architect searches for existing ADRs
   - documentation-manager identifies documentation gaps

2. **Requirements Phase:**
   - requirements-analyst creates requirements with `:implemented` status
   - requirements-analyst links to existing code and tests
   - adr-curator creates retrospective ADRs for undocumented decisions

3. **Gap Analysis:**
   - requirements-analyst identifies missing features
   - software-architect evaluates architectural debt
   - All agents contribute to improvement backlog

### Tool and Agent Boundaries

**Requirements Tools (this toolkit):**
- Format validation and metadata checking
- Search and discovery
- Traceability gap analysis
- Automated metadata fixes
- Coverage reporting

**requirements-analyst agent (AI):**
- Identifying when requirements needed
- Eliciting requirements from business processes
- Inferring requirements from code
- Ensuring testability and traceability
- RFC 2119 keyword selection
- ISO 25010 taxonomy application
- Decomposition of compound requirements

**Other agents:**
- Use requirements as specification contracts
- Reference requirements in their work
- Validate compliance with requirements
- Identify gaps in requirements coverage
- Report when requirements need updates

---

**For human documentation:** See [README.md](README.md)

**For requirement templates:** See `~/.req/template/`

**For validation:** Run `req-validate` before committing

**For search:** Use `req-search` to find existing requirements before creating new ones

**For traceability:** Use `req-trace` to analyze coverage and identify gaps
