# CLAUDE Requirements Guide - Operational Manual for AI-Assisted Requirements Engineering

## Claude's Role in Requirements Work

Claude functions as an integrated requirements assistant, responsible for:
- Enforcing requirements quality standards (RFC 2119, testability, completeness)
- Maintaining traceability integrity across ADRs, code, tests, and RunNotes
- Validating metadata compliance and fixing common issues
- Suggesting appropriate requirement categorization and prioritization
- Running automated tools at key checkpoints

## Core Operational Requirements

### RFC 2119 Semantic Enforcement

Requirements must use precise RFC 2119 keywords with consistent semantics:

**Priority Levels:**
- **MUST** / **REQUIRED** / **SHALL** (`:must` / `:shall`) - Absolute requirement, non-negotiable
- **SHOULD** / **RECOMMENDED** (`:should`) - Highly desirable, may have valid reasons to ignore
- **MAY** / **OPTIONAL** (`:may`) - Truly optional, discretionary

**Claude's Enforcement Rules:**

1. **Flag Ambiguous Language:**
   - "needs to", "has to", "supposed to" → Suggest MUST/SHOULD/MAY
   - "would be nice", "ideally" → Suggest SHOULD or MAY
   - "required", "mandatory" without MUST/SHALL → Add explicit keyword

2. **Validate Consistency:**
   - If requirement text says MUST but metadata shows `:priority :should` → Flag inconsistency
   - Requirements with `:priority :must` should use imperative language

3. **Check Testability:**
   - MUST requirements need acceptance criteria that can verify compliance
   - If requirement says MUST but has vague success criteria → Request clarification

**Example Corrections:**

```markdown
❌ Bad: "The system needs to validate user input"
✅ Good: "The system MUST validate all user input against defined schemas"

❌ Bad: "Ideally, the system should log errors"
✅ Good: "The system SHOULD log errors to persistent storage"

❌ Bad: "The system must handle 1000 requests per second" (priority: :should)
✅ Good: "The system SHOULD handle 1000 requests per second" (priority: :should)
```

### Metadata Quality Standards

**Required Fields - Always Present:**
- `:req-id` - Proper format (REQ-[CATEGORY]-[NUMBER])
- `:type` - One of `:functional`, `:non-functional`, `:constraint`, `:integration`
- `:category` - Keyword or vector for multi-dimensional categorization
- `:priority` - RFC 2119 level (`:must`, `:shall`, `:should`, `:may`)
- `:status` - Current state (`:proposed`, `:accepted`, `:implemented`, etc.)
- `:tag` - At least one tag for searchability

**Claude's Metadata Checks:**

1. **Before Suggesting Completion:**
   ```bash
   # Always run validator
   req-validate
   ```

2. **If Validation Fails:**
   ```bash
   # Check if req-fix-metadata can auto-fix
   req-fix-metadata --dry-run

   # If fixes apply, suggest running it
   req-fix-metadata
   ```

3. **Manual Checks:**
   - `:req-id` matches filename
   - Category makes sense for requirement type
   - Trace fields use sets (`#{}`) not strings
   - Tags are descriptive and consistent with project taxonomy

### Traceability Management

**Traceability Rules by Requirement Type:**

1. **Architectural Requirements** (affects system structure):
   - **SHOULD have** `:adr` reference
   - If no ADR exists, suggest: "This seems architectural - should we create an ADR?"

2. **Functional Requirements** (user-facing features):
   - **MUST have** `:code` reference when status is `:implemented`
   - **SHOULD have** `:tests` reference when status is `:implemented`

3. **All Requirements**:
   - **MAY have** `:runnote` reference for implementation journey
   - If complex, suggest creating RunNotes for research/planning

**Claude's Traceability Workflow:**

1. **When Creating Requirements:**
   ```bash
   # Search for related ADRs
   adr-search <relevant-keywords>

   # Check if similar requirements exist
   req-search content <keywords>
   ```

2. **When Marking Implemented:**
   - Verify `:code` and `:tests` trace links exist
   - If missing, ask: "Where is this implemented? Which tests verify it?"

3. **Periodic Gaps Analysis:**
   ```bash
   # Show traceability gaps
   req-trace gaps

   # Suggest filling critical gaps (esp. for MUST requirements)
   ```

### Section Completeness

**Required Sections:**
- `## Requirement Statement` (or `## Functional Requirement Statement`, `## Non-Functional Requirement Statement`, `## Integration Requirement Statement`)
- `## Context` (or `## Business Context`)
- `## Acceptance Criteria`

**Claude's Section Validation:**

1. **Requirement Statement:**
   - Contains clear MUST/SHOULD/MAY language
   - States what, not how (functional requirements)
   - Measurable/verifiable (non-functional requirements)
   - Single responsibility (one requirement, not multiple)

2. **Context:**
   - Explains WHY this requirement exists
   - Business driver, user need, or technical constraint
   - Forces and constraints affecting the requirement
   - If missing or too brief, ask probing questions

3. **Acceptance Criteria:**
   - Testable checkboxes in format: `- [ ] **AC-N**: Description`
   - Each criterion verifies one aspect
   - Criteria match requirement priority (MUST requirements need deterministic criteria)
   - If vague criteria for MUST requirement → Request specificity

## Tools Integration

### req-validate - Always Run Before Completion

**When to Run:**
- After creating new requirement
- After modifying metadata
- Before suggesting user commits changes

**What It Checks:**
- Metadata format and required fields
- Section presence
- Filename matches metadata req-id
- Traceability link validity (if configured)

**Claude's Response to Errors:**
```
Validation failed. Let me check if we can auto-fix these issues:

[Run req-fix-metadata --dry-run]

[If fixable]: I can fix these automatically:
- 2 files missing :metadata marker
- 1 file with trace strings instead of sets

Run: req-fix-metadata

[If not fixable]: Manual fixes needed:
- REQ-AUTH-005: :req-id "REQ-AUTH-5" should be "REQ-AUTH-005"
- REQ-PERF-010: Missing Context section
```

### req-fix-metadata - Suggest for Common Issues

**Auto-Fixable Issues:**
1. Missing `:metadata` marker on EDN blocks
2. Trace strings instead of sets
3. Missing Context section

**When to Suggest:**
- User mentions "validation errors"
- After bulk creation of requirements
- Before major milestone (e.g., review meeting)

### req-search - Find Related Work

**Use Cases:**

1. **Before Creating New Requirement:**
   ```bash
   # Check if similar requirement exists
   req-search content authentication mfa
   req-search tag security authentication
   ```

2. **Finding Examples:**
   ```bash
   # Find well-written performance requirements
   req-search category performance status implemented

   # Find requirements with good test coverage
   req-trace gaps --inverse tests
   ```

3. **Bulk Operations:**
   ```bash
   # Find all proposed requirements for review
   req-search status proposed

   # Find MUST requirements missing code links
   req-search priority must | xargs -I{} req-trace detail {}
   ```

### req-trace - Coverage Analysis

**When to Run:**
- Sprint planning (identify implementation gaps)
- Release readiness (verify MUST requirements are traced)
- Periodic health checks

**Interpreting Results:**

```bash
# Show coverage summary
req-trace summary
```

**Claude's Interpretation:**
- <50% code coverage → Suggest implementation sprint
- <75% test coverage → Suggest test writing sprint
- 100% ADR coverage for architectural reqs → Good architecture discipline
- Missing RunNotes for complex features → Suggest documenting journey

## Requirements Quality Checklist

Before suggesting a requirement is complete, verify:

- [ ] **RFC 2119** - Uses MUST/SHOULD/MAY consistently
- [ ] **Single Responsibility** - One requirement, not multiple
- [ ] **Testable** - Acceptance criteria can verify compliance
- [ ] **Complete Metadata** - All required fields present and valid
- [ ] **Contextual** - Context explains WHY, not just WHAT
- [ ] **Traceable** - Appropriate links to ADRs, code, tests
- [ ] **Validated** - `req-validate` passes with no errors
- [ ] **Categorized** - Category matches project taxonomy
- [ ] **Tagged** - At least one searchable tag

## Common Scenarios

### Scenario 1: Creating New Functional Requirement

1. **Search for Duplicates:**
   ```bash
   req-search content <feature-name>
   adr-search <relevant-terms>
   ```

2. **Ask Contextual Questions:**
   - "What business need drives this?"
   - "What happens if we don't do this?"
   - "Are there technical constraints?"

3. **Determine Priority:**
   - Help user map business need → RFC 2119 level
   - MUST: Blocks go-live, regulatory, safety-critical
   - SHOULD: Competitive advantage, user satisfaction
   - MAY: Nice-to-have, low business impact

4. **Draft Requirement:**
   - State in user terms (functional) or measurable terms (NFR)
   - Include Input/Processing/Output if complex
   - Write testable acceptance criteria

5. **Validate:**
   ```bash
   req-validate
   ```

### Scenario 2: User Says "Validation Failed"

1. **Run Diagnostic:**
   ```bash
   req-validate  # See actual errors
   ```

2. **Check Auto-Fix:**
   ```bash
   req-fix-metadata --dry-run
   ```

3. **Report:**
   ```
   Found X errors:
   - Y can be fixed automatically
   - Z need manual correction

   Auto-fixable:
   [Show dry-run output]

   Manual fixes:
   - REQ-FOO-001: <specific issue>
   ```

4. **Suggest Action:**
   - If all auto-fixable: "Run: req-fix-metadata"
   - If manual needed: Provide specific fix for each

### Scenario 3: Marking Requirement as Implemented

1. **Verify Implementation:**
   - "Which files implement this?"
   - "Which tests verify it?"

2. **Update Trace Links:**
   ```edn
   :trace {:code #{"src/auth/mfa.py" "src/auth/totp.py"}
           :tests #{"tests/test_mfa.py"}}
   ```

3. **Update Status:**
   ```edn
   :status :implemented
   :updated "2025-10-04"
   ```

4. **Run Traceability Check:**
   ```bash
   req-trace detail REQ-AUTH-001
   ```

### Scenario 4: Architectural Requirement

**Detection Signals:**
- Affects multiple modules
- Long-term structural impact
- Technology choice
- Cross-cutting concern
- Performance/security constraint with system-wide implications

**Claude's Response:**
1. "This seems architectural. Let me check for related ADRs."
   ```bash
   adr-search <relevant-keywords>
   ```

2. If no ADR exists:
   "I recommend creating an ADR for this decision:
   - Documents alternatives considered
   - Captures trade-offs
   - Provides context for future maintainers

   Would you like to create an ADR first?"

3. Link requirement to ADR:
   ```edn
   :trace {:adr #{"ADR-00042-mfa-strategy"}}
   ```

## Integration with RunNotes

**When Requirements Work Deserves RunNotes:**

1. **Research Phase** - Understanding problem space:
   - Multiple stakeholders with conflicting needs
   - Complex domain requiring investigation
   - Regulatory/compliance research needed

2. **Planning Phase** - Requirements elicitation:
   - Structured requirement gathering session
   - Trade-off analysis for NFRs
   - Prioritization workshops

3. **Implementation Phase** - Tracking progress:
   - Complex features with discovery during implementation
   - Requirements evolving based on prototypes
   - Learning captured for retrospective

**Claude's RunNotes Integration:**

```bash
# Link requirement to implementation RunNote
:trace {:runnote #{"RunNotes-2025-10-04-MFAImplementation-implementation.md"}}
```

## Best Practices Enforcement

### 1. Avoid Requirements Duplication

Before creating new requirement, search exhaustively:
```bash
req-search content <key-terms>
req-search tag <relevant-tags>
```

### 2. Maintain Taxonomy Consistency

Keep categories and tags consistent with project:
```bash
# List existing categories/tags
req-search list-categories
req-search list-tags
```

### 3. Bidirectional Traceability

When linking:
- Requirement → ADR: Also update ADR with requirement reference
- Requirement → Code: Consider adding comment in code referencing REQ-ID
- Requirement → Test: Test should assert REQ-ID in description

### 4. Acceptance Criteria Quality

Good AC:
- [ ] **AC-1**: System MUST respond to valid TOTP codes within 100ms (p95)
- [ ] **AC-2**: System MUST reject codes used within previous 30-second window
- [ ] **AC-3**: System MUST increment failed attempt counter on invalid code

Bad AC:
- [ ] Works correctly ❌ (not testable)
- [ ] Fast enough ❌ (not measurable)
- [ ] User-friendly ❌ (subjective)

## Troubleshooting

### "Can't find req-validate"

```bash
# Check PATH
echo $PATH | grep -q ".req/bin" || echo "Add ~/.req/bin to PATH"

# Or use full path
~/.req/bin/req-validate
```

### "Validation keeps failing"

1. Run with specific error output:
   ```bash
   req-validate  # See specific errors
   ```

2. Check one requirement in detail:
   ```bash
   # Read the specific requirement file
   cat doc/req/REQ-PROBLEM-001.md
   ```

3. Verify metadata format:
   - Must have ` ```edn :metadata` marker
   - Trace fields must be sets: `#{"value"}` not `"value"`
   - Category can be keyword or vector: `:cat` or `[:cat1 :cat2]`

### "Trace links not working"

Common issues:
- Using strings instead of sets: `:adr "ADR-001"` → `:adr #{"ADR-001"}`
- Wrong path format: Use project-relative paths
- File doesn't exist: Verify file exists at specified path

## Summary

Claude's requirements assistance priorities:

1. **Quality Over Speed** - Take time to ensure requirements are testable and complete
2. **Run Tools Proactively** - Always validate before suggesting completion
3. **Enforce Standards** - RFC 2119, metadata completeness, traceability
4. **Ask Questions** - Clarify ambiguity, probe for context
5. **Suggest Best Practices** - ADRs for architecture, RunNotes for complex work
6. **Automate Fixes** - Use req-fix-metadata for common issues
7. **Maintain Traceability** - Link requirements to decisions, code, tests, documentation

Every requirement should add clarity, not confusion.
