# Requirements Integration Patterns

Detailed guidance on integrating requirements with ADRs, RunNotes, code, and tests. For a quick reference, see [CLAUDE.md](../CLAUDE.md).

## Table of Contents

- [Integration with ADR Tools](#integration-with-adr-tools)
- [Integration with RunNotes Tools](#integration-with-runnotes-tools)
- [Integration with Code and Tests](#integration-with-code-and-tests)
- [Bidirectional Traceability](#bidirectional-traceability)
- [Agent Ecosystem Integration](#agent-ecosystem-integration)

---

## Integration with ADR Tools

Requirements and ADRs form a bidirectional relationship where architectural decisions constrain requirements and requirements drive architectural decisions.

### Requirements → ADRs

Requirements create architectural constraints that need decisions.

#### Workflow

1. **Elicit requirement with quality attributes**
   - Identify non-functional requirements
   - Recognize performance, security, scalability needs

2. **Recognize architectural implications**
   - Technology choices needed
   - Integration patterns required
   - Infrastructure decisions necessary

3. **Search for relevant ADRs**
   ```bash
   adr-search content "authentication"
   adr-search tag security
   ```

4. **If no ADR exists, identify need for new architectural decision**
   - Document decision needed
   - Flag for architecture review
   - Create ADR before or during implementation

5. **Link requirement to ADR (once created)**
   ```edn
   {:trace {:adr #{"ADR-00042"}}}
   ```

#### Example: Performance Requirement Drives Architecture

**Requirement:**
```markdown
# REQ-PERF-001 - High-Throughput Request Handling

## Requirement Statement

System **MUST** handle 10,000 requests per second at 95th percentile latency under 200ms.
```

**Architectural implications:**
- Caching strategy needed
- Database scaling approach needed
- Load balancing needed

**Creates need for ADRs:**
- ADR-00XXX: Caching Strategy (Redis vs Memcached)
- ADR-00YYY: Database Sharding Approach
- ADR-00ZZZ: Load Balancer Selection (Nginx vs HAProxy)

**Link in requirement:**
```edn
{:req-id "REQ-PERF-001"
 :trace {:adr #{"ADR-00XXX" "ADR-00YYY" "ADR-00ZZZ"}}}
```

### ADRs → Requirements

Architectural decisions create implementation requirements.

#### Workflow

1. **ADR documents architectural decision**
   - Technology chosen
   - Pattern selected
   - Constraint established

2. **Decision implies implementation requirements**
   - Specific capabilities needed
   - Constraints to enforce
   - Standards to follow

3. **Create requirements specifying what must be built**
   - Functional requirements for implementation
   - Non-functional requirements for quality

4. **Link requirements to driving ADR**
   ```edn
   {:trace {:adr #{"ADR-00042"}}}
   ```

#### Example: Event Sourcing Decision Creates Requirements

**ADR-00042: Use Event Sourcing for Audit Trail**

Decision: Adopt event sourcing pattern for complete audit trail of state changes.

**Implied requirements:**

```markdown
# REQ-EVENTS-001 - Event Persistence

System **MUST** persist all state changes as immutable events with timestamp, user, and change details.

{:trace {:adr #{"ADR-00042"}}}

---

# REQ-EVENTS-002 - Event Replay

System **MUST** support replaying events to reconstruct state at any point in time.

{:trace {:adr #{"ADR-00042"}}}

---

# REQ-EVENTS-003 - Event Version Compatibility

System **MUST** maintain backward compatibility when event schemas evolve.

{:trace {:adr #{"ADR-00042"}}}
```

### Search ADRs Before Creating Requirements

**ALWAYS search ADRs first:**

```bash
# Search by content
adr-search content "authentication"
adr-search content "database"

# Search by tag
adr-search tag security
adr-search tag infrastructure
```

**ADRs may:**
- Establish constraints your requirements must respect
- Document decisions your requirements implement
- Reveal conflicts with proposed requirements
- Provide context for why requirements exist

**If conflict found:**

**Requirement:** "System MUST use MongoDB for data storage"
**ADR-00042:** "We will use PostgreSQL for ACID guarantees"

**Resolution options:**
1. Change requirement to match ADR (use PostgreSQL)
2. Create new ADR superseding old one (justify MongoDB)
3. Reject requirement as architecturally infeasible

### Link Requirements to ADRs in Metadata

```edn
{:req-id "REQ-AUTH-001"
 :trace {:adr #{"ADR-00042" "ADR-00043"}}}
```

**Multiple ADRs may constrain a single requirement:**
- ADR-00042: Authentication technology choice
- ADR-00043: Session management approach

### Use req-trace to Find ADR Gaps

```bash
# Show requirements missing ADR links
req-trace gaps adr
```

**For each gap:**
1. Search for relevant ADRs: `adr-search content <topic>`
2. Add link if ADR exists
3. Identify need for new ADR if missing

---

## Integration with RunNotes Tools

RunNotes document the journey; requirements document the contract.

### RunNotes → Requirements (Elicitation)

Planning phase RunNotes contain the raw material for requirements.

#### Search for Planning Sessions

```bash
# Find planning RunNotes
runnote-search tag planning
runnote-search content "feature-name"

# Find research RunNotes
runnote-search tag research
runnote-search content "spike"
```

#### Extract Requirements from RunNotes

**RunNotes content example:**

```yaml
phase: planning
objectives:
  - Enable customer service to process refunds
  - Support 30-day refund window
  - Integrate with payment gateway
constraints:
  - Must use existing Stripe integration
  - Cannot exceed 5-second processing time
```

**Generated requirements:**

```markdown
# REQ-REFUND-001 - Calculate Refund Amount
System MUST calculate refund amount based on original order total.
{:trace {:runnote #{"RunNotes-2025-10-01-RefundProcess-planning"}}}

# REQ-REFUND-002 - Verify Refund Window
System MUST verify order was placed within 30 days of refund request.
{:trace {:runnote #{"RunNotes-2025-10-01-RefundProcess-planning"}}}

# REQ-REFUND-003 - Payment Gateway Integration
System MUST initiate payment reversal through Stripe API.
{:trace {:runnote #{"RunNotes-2025-10-01-RefundProcess-planning"}
         :adr #{"ADR-00055"}}}  ; Stripe integration decision

# REQ-REFUND-NFR-001 - Processing Time Limit
System MUST complete refund processing within 5 seconds.
{:trace {:runnote #{"RunNotes-2025-10-01-RefundProcess-planning"}}}
```

#### Link Requirements to Source RunNotes

```edn
{:trace {:runnote #{"RunNotes-2025-10-01-RefundProcess-planning"}}}
```

**Multiple RunNotes for complex features:**
```edn
{:trace {:runnote #{"RunNotes-2025-10-01-RefundProcess-planning"
                    "RunNotes-2025-10-05-RefundProcess-research"
                    "RunNotes-2025-10-12-RefundProcess-spike"}}}
```

### Requirements → RunNotes (Context)

Requirements reference RunNotes for business context.

#### Example Context Section

```markdown
## Context

Per RunNotes-2025-10-01-RefundProcess-planning, customer service representatives need ability to process refunds within 30 days of purchase to comply with consumer protection regulations (Consumer Rights Act 2015).

Current manual refund process takes 5-7 days and causes customer complaints. Automated refund processing will reduce time to <24 hours and improve customer satisfaction scores.

Research documented in RunNotes-2025-10-05-RefundProcess-research identified Stripe's refund API as most efficient approach with 99.9% success rate.
```

### Traceability Workflow

Full lifecycle traceability from planning to verification:

#### 1. Planning Phase (RunNotes)
- Document business need
- Capture stakeholder requirements
- Identify constraints and assumptions
- Tag with: `#planning`, `#research`

#### 2. Requirements Phase
- Elicit requirements from RunNotes
- Link requirements to source:
  ```edn
  :trace {:runnote #{"RunNotes-2025-10-01-Feature-planning"}}
  ```
- Link to architectural constraints:
  ```edn
  :trace {:adr #{"ADR-00042"}}
  ```

#### 3. Implementation Phase
- Implement features
- Update requirements with code links:
  ```edn
  :trace {:code #{"src/module/file.py:45-67"}}
  ```
- Reference requirements in code comments
- Create implementation RunNotes if complex
- Tag with: `#implementation`, `#discovery`

#### 4. Testing Phase
- Write tests validating acceptance criteria
- Link requirements to tests:
  ```edn
  :trace {:tests #{"test/module/test_file.py:12"}}
  ```
- Reference REQ-ID in test docstrings

#### 5. Verification
```bash
req-trace detail REQ-REFUND-001
```

**Shows complete traceability:**
- **RunNotes:** Why we need this (business need)
- **ADRs:** Architectural constraints
- **Code:** Implementation location
- **Tests:** Verification coverage

---

## Integration with Code and Tests

### Requirements → Code (Implementation Traceability)

As requirements are implemented, link to code.

#### Format for Code Links

```edn
{:trace {:code #{"src/refunds/calculator.py:45-67"
                 "src/refunds/api.py:123-145"
                 "src/refunds/service.py"}}}
```

**Formats:**
- **With line range:** `"path/to/file.ext:45-67"`
- **Single line:** `"path/to/file.ext:45"`
- **Whole file:** `"path/to/file.ext"`

#### Best Practices

1. **Update links when code moves**
   - Refactoring invalidates line numbers
   - Maintain accurate traceability
   - Use `req-trace gaps code` to find broken links

2. **Add code comments linking back to requirement**
   ```python
   # Implements REQ-REFUND-001: Calculate Refund Amount
   def calculate_refund(order):
       # See: doc/req/REQ-REFUND-001-calculate-refund-amount.md
       ...
   ```

3. **Keep links precise (specific line ranges)**
   - Better: `"calculator.py:45-67"` (specific function)
   - Worse: `"calculator.py"` (entire file)

### Requirements → Tests (Verification Traceability)

Each acceptance criterion should map to tests.

#### Link in Acceptance Criteria

```markdown
## Acceptance Criteria

- [ ] **AC-1**: Refund amount equals sum of line items
      Test: `test/refunds/calculator_test.py:12-25`

- [ ] **AC-2**: Refund amount includes tax in calculation
      Test: `test/refunds/calculator_test.py:27-35`

- [ ] **AC-3**: Refund amount includes original shipping cost
      Test: `test/refunds/calculator_test.py:37-42`
```

#### Link in Metadata

```edn
{:trace {:tests #{"test/refunds/calculator_test.py:12-42"}}}
```

**Multiple test files:**
```edn
{:trace {:tests #{"test/refunds/calculator_test.py:12-42"
                  "test/refunds/api_test.py:56"
                  "test/integration/refund_flow_test.py:120-145"}}}
```

---

## Bidirectional Traceability

Maintain bidirectional references between requirements and implementation.

### In Requirement File

```edn
{:req-id "REQ-AUTH-001"
 :trace {:adr #{"ADR-00042"}
         :code #{"src/auth/mfa.py:45-67"}
         :tests #{"test/auth/mfa_test.py:12"}}}
```

### In Code File

```python
# Implements REQ-AUTH-001: Multi-Factor Authentication
def generate_totp(secret: str) -> str:
    """
    Generates TOTP code per RFC 6238.

    See: doc/req/REQ-AUTH-001-multi-factor-authentication.md
    Acceptance Criteria: AC-1 (RFC 6238 compliance)
    """
    # Implementation constrained by ADR-00042 (TOTP over SMS)
    ...
```

### In Test File

```python
class TestMFA(unittest.TestCase):
    """
    Validates REQ-AUTH-001 acceptance criteria.

    See: doc/req/REQ-AUTH-001-multi-factor-authentication.md
    """

    def test_totp_follows_rfc6238(self):
        """
        Validates AC-1: TOTP follows RFC 6238

        REQ-AUTH-001: Multi-Factor Authentication
        """
        secret = "JBSWY3DPEHPK3PXP"
        totp = generate_totp(secret)
        assert len(totp) == 6  # RFC 6238: 6-digit code
        assert totp.isdigit()  # RFC 6238: numeric only
```

### In ADR File

```markdown
# ADR-00042 - Use TOTP for Multi-Factor Authentication

## Decision

Use Time-based One-Time Passwords (TOTP) for second authentication factor.

## Consequences

### Requirements Impacted

- REQ-AUTH-001: Multi-Factor Authentication (implements this ADR)
- REQ-AUTH-NFR-002: Authentication Performance (affected by TOTP computation)

## Implementation

See requirements doc/req/REQ-AUTH-001-multi-factor-authentication.md for acceptance criteria.
```

### Gap Analysis with req-trace

#### Find Requirements Missing Implementation

```bash
req-trace gaps code
```

**Output shows requirements with no `:code` links:**
```
Requirements missing code traceability:
- REQ-REFUND-004: Update order status
- REQ-REFUND-005: Send confirmation email
- REQ-AUTH-002: OAuth2 authentication
```

**Action:** Either:
1. Implement missing requirements
2. Add code links if already implemented
3. Update status to `:deferred` if postponed

#### Find Requirements Missing Tests

```bash
req-trace gaps tests
```

**Output shows requirements with no `:tests` links:**
```
Requirements missing test traceability:
- REQ-REFUND-001: Calculate refund amount
- REQ-AUTH-001: Multi-factor authentication
```

**Action:** Either:
1. Write missing tests
2. Add test links if tests exist
3. Flag for test sprint

#### Verify Coverage

```bash
req-trace summary
```

**Output shows percentages:**
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

**Interpretation:**
- <50% code coverage → Suggest implementation sprint
- <75% test coverage → Suggest test writing sprint
- 100% ADR coverage for architectural reqs → Good discipline

---

## Agent Ecosystem Integration

### Coordination with Other Agents

#### software-architect agent
- Identifies architectural implications of requirements
- Requirements-analyst ensures requirements don't conflict with ADRs
- Architect reviews NFR taxonomy for technical accuracy

#### documentation-manager agent
- Validates requirement documentation completeness
- Checks traceability links are current
- Detects drift between requirements and implementation
- Ensures requirements documentation stays synchronized

#### test-strategist agent
- Reviews acceptance criteria for testability
- Links test cases to requirements
- Validates test coverage matches requirements
- Ensures all requirements are verified

#### adr-curator agent
- Documents architectural decisions driven by requirements
- Ensures requirements reference relevant ADRs
- Identifies when requirements need new ADRs
- Maintains bidirectional traceability

### Multi-Agent Workflow: New Feature

#### 1. Planning Phase
- **RunNotes:** Document business need
- **requirements-analyst:** Search existing requirements and ADRs
  ```bash
  req-search content "similar-feature"
  adr-search content "relevant-tech"
  ```

#### 2. Requirements Phase
- **requirements-analyst:** Elicit requirements from RunNotes
- **software-architect:** Identify architectural implications
- **adr-curator:** Create ADRs for significant decisions
- **requirements-analyst:** Link requirements to ADRs
  ```edn
  {:trace {:adr #{"ADR-00042"}
           :runnote #{"RunNotes-2025-10-01-Feature-planning"}}}
  ```

#### 3. Implementation Phase
- **Developers:** Implement features
- **requirements-analyst:** Update requirements with code traceability
  ```edn
  {:trace {:code #{"src/feature/module.py:45-67"}}}
  ```
- **documentation-manager:** Validate links stay current

#### 4. Testing Phase
- **test-strategist:** Validate acceptance criteria testability
- **Developers:** Implement tests per criteria
- **requirements-analyst:** Update requirements with test traceability
  ```edn
  {:trace {:tests #{"test/feature/test_module.py:12"}}}
  ```

#### 5. Verification
- All agents verify their concerns in requirements
- **requirements-analyst:** Run traceability check
  ```bash
  req-trace summary
  req-trace detail REQ-FEATURE-001
  ```

### Multi-Agent Workflow: Legacy System Documentation

#### 1. Analysis Phase
- **requirements-analyst:** Infer requirements from code
- **software-architect:** Search for existing ADRs
- **documentation-manager:** Identify documentation gaps

#### 2. Requirements Phase
- **requirements-analyst:** Create requirements with `:implemented` status
- **requirements-analyst:** Link to existing code and tests
  ```edn
  {:status :implemented
   :trace {:code #{"src/existing/feature.py:45-67"}
           :tests #{"test/existing/test_feature.py:12"}}}
  ```
- **adr-curator:** Create retrospective ADRs for undocumented decisions

#### 3. Gap Analysis
- **requirements-analyst:** Identify missing features
- **software-architect:** Evaluate architectural debt
- All agents contribute to improvement backlog

### Tool and Agent Boundaries

#### Requirements Tools (this toolkit)
- Format validation and metadata checking
- Search and discovery
- Traceability gap analysis
- Automated metadata fixes
- Coverage reporting

#### requirements-analyst agent (AI)
- Identifying when requirements needed
- Eliciting requirements from business processes
- Inferring requirements from code
- Ensuring testability and traceability
- RFC 2119 keyword selection
- ISO 25010 taxonomy application
- Decomposition of compound requirements

#### Other agents
- Use requirements as specification contracts
- Reference requirements in their work
- Validate compliance with requirements
- Identify gaps in requirements coverage
- Report when requirements need updates

---

## See Also

- [CLAUDE.md](../CLAUDE.md) - Quick reference for AI agents
- [README.md](../README.md) - Human-focused usage guide
- [README-WORKFLOWS.md](README-WORKFLOWS.md) - Detailed workflow examples
- [README-FILE-FORMAT.md](README-FILE-FORMAT.md) - Complete format specifications
- [README-QUALITY.md](README-QUALITY.md) - Quality enforcement guidelines
