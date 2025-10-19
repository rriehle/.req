# Requirements Management Workflows

This document provides detailed workflows for AI agents working with requirements management tools. For a quick reference, see [CLAUDE.md](../CLAUDE.md).

## Table of Contents

- [Workflow 1: Business Process → Requirements (Elicitation)](#workflow-1-business-process--requirements-elicitation)
- [Workflow 2: Code/Docs → Requirements (Inference)](#workflow-2-codedocs--requirements-inference)
- [Workflow 3: Requirements Validation](#workflow-3-requirements-validation)

---

## Workflow 1: Business Process → Requirements (Elicitation)

When given business process descriptions, planning documents, or stakeholder requests, follow this detailed workflow to elicit requirements.

### Step 1: Gather Context

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

### Step 2: Extract Functional Requirements

Functional requirements specify WHAT the system does.

**Ask:**
- What business capabilities must the system provide?
- What workflows must be supported?
- What data must be processed?
- What business rules apply?
- What integrations are required?

**Example extraction from business process:**

**Business Process:** "Customer service representatives need to process refunds for orders within 30 days of purchase."

**Functional Requirements:**
- REQ-REFUND-001: System MUST calculate refund amount based on original order total
- REQ-REFUND-002: System MUST verify order was placed within 30 days
- REQ-REFUND-003: System MUST initiate payment reversal through payment gateway
- REQ-REFUND-004: System MUST update order status to "refunded"
- REQ-REFUND-005: System MUST send refund confirmation email to customer

### Step 3: Identify Non-Functional Requirements

NFRs specify HOW WELL the system performs using ISO 25010 taxonomy.

**Ask:**
- **Performance**: Response time, throughput, capacity requirements?
- **Security**: Authentication, authorization, confidentiality, integrity needs?
- **Usability**: Accessibility, learnability, error prevention requirements?
- **Reliability**: Availability, fault tolerance, recoverability needs?
- **Maintainability**: Testability, modularity, analyzability requirements?
- **Compatibility**: Integration, interoperability needs?
- **Portability**: Adaptability, installability requirements?

**Example NFRs for refund process:**

- REQ-REFUND-NFR-001: Refund calculation MUST complete within 2 seconds (:performance-efficiency/time-behavior)
- REQ-REFUND-NFR-002: Refund operations MUST be logged with user ID and timestamp (:security/accountability)
- REQ-REFUND-NFR-003: Refund UI MUST be accessible per WCAG 2.1 AA (:usability/accessibility)
- REQ-REFUND-NFR-004: Failed payment reversals MUST retry automatically (:reliability/fault-tolerance)

### Step 4: Create Requirement Specifications

For each requirement:

**1. Copy template:**
```bash
cp ~/.req/template/functional.md doc/req/REQ-CATEGORY-NNN-title.md
# or
cp ~/.req/template/non-functional.md doc/req/REQ-CATEGORY-NFR-NNN-title.md
```

**2. Fill metadata:**
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

**3. Write requirement statement with RFC 2119 keyword:**
```markdown
## Requirement Statement

System **MUST** calculate refund amount based on original order total, including all items, taxes, and shipping charges.
```

**4. Document context:**
```markdown
## Context

Per RunNotes-2025-10-01-RefundProcess-planning, customer service needs ability to process refunds. Per ADR-00042, all financial calculations use Decimal type to prevent floating-point errors.

Business policy requires full refund including shipping for orders within 30 days.
```

**5. Define testable acceptance criteria:**
```markdown
## Acceptance Criteria

- [ ] **AC-1**: Refund amount equals sum of all line items from original order
- [ ] **AC-2**: Refund amount includes original tax calculation
- [ ] **AC-3**: Refund amount includes original shipping cost
- [ ] **AC-4**: Refund calculation uses Decimal type (no floating-point)
- [ ] **AC-5**: Partial refunds are NOT supported (out of scope)
```

### Step 5: Validate Requirements

Before completing elicitation work:

```bash
# Validate format and metadata
req-validate

# Check traceability coverage
req-trace summary

# Verify no duplicate REQ-IDs
req-search list
```

### Step 6: Create Requirements Matrix

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

---

## Workflow 2: Code/Docs → Requirements (Inference)

When analyzing existing systems to document what they fulfill, follow this workflow to infer requirements from implementation.

### Step 1: Analyze Implementation

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

### Step 2: Search for Rationale

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

### Step 3: Infer Functional Requirements

For each implemented capability, ask:
- What business need does this fulfill?
- What is the requirement statement?
- Is this MUST, SHOULD, or MAY?

**Example from code:**

**Code:** `calculate_refund(order) -> Decimal`

**Inferred Requirement:**
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

### Step 4: Identify Non-Functional Characteristics

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

**Code shows retry decorator with 3 attempts:**
```python
@retry(max_attempts=3, backoff=exponential)
def reverse_payment(payment_id):
    ...
```

**Inferred NFR:**
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

### Step 5: Document Implementation Status

For inferred requirements, set status to `:implemented`:

```edn
{:req-id "REQ-REFUND-001"
 :status :implemented
 :trace {:code #{"src/refunds/calculator.py:45-67"}
         :tests #{"test/refunds/calculator_test.py:12"}}}
```

### Step 6: Identify Gaps

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

---

## Workflow 3: Requirements Validation

When reviewing requirements for quality and completeness, use this comprehensive validation workflow.

### Testability Checks

For each requirement, ask:
- Can this be verified objectively?
- Are acceptance criteria measurable?
- Can we write a test for this?

**Red flags:**
- Subjective terms: "user-friendly", "intuitive", "fast", "secure"
- Vague quantities: "many", "few", "quickly"
- Missing criteria: No acceptance criteria section

**Fix by adding measurable criteria:**

❌ **Before:** "System shall be fast"

✅ **After:** "System MUST respond to search queries within 500ms at 95th percentile under 1000 concurrent users"

### Completeness Checks

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

### Consistency Checks with ADRs

Requirements must not conflict with architectural decisions.

**Search ADRs for constraints:**
```bash
adr-search content "relevant-domain"
adr-search tag technology
```

**Check for conflicts:**

**Requirement:** "System MUST use MongoDB for data storage"
**ADR-00042:** "We will use PostgreSQL for ACID guarantees"

**Conflict!** Either:
1. Change requirement to match ADR, OR
2. Create new ADR superseding old one, OR
3. Reject requirement as architecturally infeasible

**Add ADR traceability:**
```edn
{:trace {:adr #{"ADR-00042"}}}
```

### Traceability Checks

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

### Priority Validation

Verify RFC 2119 keywords are used correctly.

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

### Atomic Requirement Check

Each file should contain ONE requirement.

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

---

## See Also

- [CLAUDE.md](../CLAUDE.md) - Quick reference for AI agents
- [README.md](../README.md) - Human-focused usage guide
- [README-FILE-FORMAT.md](README-FILE-FORMAT.md) - Complete format specifications
- [README-INTEGRATION.md](README-INTEGRATION.md) - Integration patterns
- [README-QUALITY.md](README-QUALITY.md) - Quality enforcement guidelines
