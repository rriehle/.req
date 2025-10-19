# Requirements Management Tools - AI Agent Quick Reference

This is a condensed quick reference for AI agents working with Requirements Management tools.

For detailed documentation:
- **Workflows:** [doc/README-WORKFLOWS.md](doc/README-WORKFLOWS.md) - Detailed elicitation, inference, and validation workflows
- **File Format:** [doc/README-FILE-FORMAT.md](doc/README-FILE-FORMAT.md) - Complete format specifications
- **Integration:** [doc/README-INTEGRATION.md](doc/README-INTEGRATION.md) - Integration with ADR/RunNotes/Code/Tests
- **Quality:** [doc/README-QUALITY.md](doc/README-QUALITY.md) - Quality enforcement and pitfalls
- **Human Guide:** [README.md](README.md) - Installation, configuration, usage

---

## Core Principles

| Principle | Description |
|-----------|-------------|
| **Requirements as Contract** | Binding contract between business need and technical implementation |
| **Bidirectional Engineering** | Forward (elicitation) and reverse (inference) both valid |
| **Testability First** | If you can't test it, it's not a requirement - it's a wish |
| **Traceability** | Link upward (business needs), sideways (ADRs), downward (code/tests) |
| **RFC 2119 Precision** | MUST/SHOULD/MAY with precise meaning, no ambiguity |
| **ISO 25010 for NFRs** | Standard taxonomy for non-functional requirements |
| **Atomic Requirements** | One concern per file - enables independent verification |

---

## Tools Quick Reference

### When to Use Each Tool

| Tool | When to Use | Key Output |
|------|-------------|------------|
| **req-validate** | Before commits, in CI/CD, during code review | Validation errors, duplicate detection |
| **req-search** | Before creating requirements, finding examples | Requirements matching criteria, tag taxonomy |
| **req-trace** | Sprint planning, release readiness, health checks | Coverage metrics, gap analysis |
| **req-fix-metadata** | After bulk changes, fixing validation errors | Auto-fixed metadata issues |

**Detailed tool documentation:** See [README.md](README.md) sections on each tool

---

## Workflows Summary

### Mode 1: Business Process → Requirements (Elicitation)

**When:** Given business process descriptions, planning documents, or stakeholder requests

**Quick steps:**
1. **Gather context** - Search RunNotes, ADRs, existing requirements
2. **Extract functional requirements** - What the system does
3. **Identify non-functional requirements** - How well it performs (ISO 25010)
4. **Create specifications** - Copy template, fill metadata, write testable AC
5. **Validate** - Run `req-validate` and `req-trace summary`

**Detailed workflow with examples:** [doc/README-WORKFLOWS.md#workflow-1](doc/README-WORKFLOWS.md#workflow-1-business-process--requirements-elicitation)

### Mode 2: Code/Docs → Requirements (Inference)

**When:** Analyzing existing systems to document what they fulfill

**Quick steps:**
1. **Analyze implementation** - Read code, tests, configs
2. **Search for rationale** - Find ADRs and RunNotes explaining decisions
3. **Infer functional requirements** - What business need does code fulfill?
4. **Identify non-functional characteristics** - Security, performance, reliability in code
5. **Document implementation status** - Set `:status :implemented`
6. **Identify gaps** - What's missing? What's over-engineered?

**Detailed workflow with examples:** [doc/README-WORKFLOWS.md#workflow-2](doc/README-WORKFLOWS.md#workflow-2-codedocs--requirements-inference)

### Mode 3: Requirements Validation

**When:** Reviewing requirements for quality and completeness

**Quick checks:**
- [ ] **Testability** - Objective verification possible?
- [ ] **Completeness** - All required metadata and sections present?
- [ ] **Consistency** - Conflicts with ADRs?
- [ ] **Traceability** - Appropriate links for status?
- [ ] **Priority** - RFC 2119 keywords correct?
- [ ] **Atomicity** - One requirement or multiple?

**Detailed validation workflow:** [doc/README-WORKFLOWS.md#workflow-3](doc/README-WORKFLOWS.md#workflow-3-requirements-validation)

---

## Integration Quick Reference

### With ADR Tools

**Requirements → ADRs:**
Requirements with architectural implications need ADR decisions.

**Search first:**
```bash
adr-search content "relevant-domain"
adr-search tag technology
```

**Link in metadata:**
```edn
{:trace {:adr #{"ADR-00042"}}}
```

**ADRs → Requirements:**
Architectural decisions create implementation requirements.

**Detailed integration patterns:** [doc/README-INTEGRATION.md#integration-with-adr-tools](doc/README-INTEGRATION.md#integration-with-adr-tools)

### With RunNotes Tools

**RunNotes → Requirements:**
Planning phase RunNotes contain raw material for requirements.

**Search:**
```bash
runnote-search tag planning
runnote-search content "feature-name"
```

**Link:**
```edn
{:trace {:runnote #{"RunNotes-2025-10-01-Feature-planning"}}}
```

**Detailed integration patterns:** [doc/README-INTEGRATION.md#integration-with-runnotes-tools](doc/README-INTEGRATION.md#integration-with-runnotes-tools)

### With Code and Tests

**Requirements → Code:**
```edn
{:trace {:code #{"src/module/file.py:45-67"}}}
```

**Requirements → Tests:**
```edn
{:trace {:tests #{"test/module/test_file.py:12"}}}
```

**Find gaps:**
```bash
req-trace gaps code   # Missing implementation
req-trace gaps tests  # Missing verification
```

**Detailed integration patterns:** [doc/README-INTEGRATION.md#integration-with-code-and-tests](doc/README-INTEGRATION.md#integration-with-code-and-tests)

---

## Quality Standards

### RFC 2119 Keywords

| Keyword | Use When | Example |
|---------|----------|---------|
| **MUST** / **SHALL** | System unusable without it, legal requirement, critical function | "System MUST authenticate users" |
| **SHOULD** | Best practice, important but negotiable | "API SHOULD return within 200ms" |
| **MAY** | Optional, nice-to-have | "System MAY provide dark mode" |

**Detailed RFC 2119 guidelines:** [doc/README-QUALITY.md#rfc-2119-keyword-precision](doc/README-QUALITY.md#rfc-2119-keyword-precision)

### Testability

**Non-testable:** "System shall be user-friendly"
**Testable:** "Users complete checkout in ≤ 3 clicks"

**Non-testable:** "System must be fast"
**Testable:** "System responds in < 500ms at 95th percentile"

**Detailed testability criteria:** [doc/README-QUALITY.md#testable-acceptance-criteria](doc/README-QUALITY.md#testable-acceptance-criteria)

### Traceability by Status

| Status | Required Links | Optional Links |
|--------|----------------|----------------|
| `:proposed` | `:runnote` | `:adr` |
| `:accepted` | `:runnote`, `:adr` | `:code` |
| `:implemented` | `:runnote`, `:adr`, `:code`, `:tests` | - |

**Check coverage:**
```bash
req-trace summary
req-trace gaps adr    # Missing ADR links
req-trace gaps code   # Missing code links
req-trace gaps tests  # Missing test links
```

**Detailed traceability guidelines:** [doc/README-QUALITY.md#complete-traceability](doc/README-QUALITY.md#complete-traceability)

---

## File Format Quick Reference

### Metadata Required Fields

```edn
{:req-id "REQ-CATEGORY-NNN"              ; Unique ID, matches filename
 :type :functional                       ; :functional | :non-functional | :constraint
 :category :your-category                ; Keyword or vector
 :priority :must                         ; :must | :should | :may
 :status :proposed                       ; :proposed | :accepted | :implemented | :deprecated
 :tag #{:tag1 :tag2}}                    ; At least one tag
```

### Metadata Optional Fields

```edn
{:updated "2025-10-14"                   ; Last update date
 :trace {:adr #{"ADR-00042"}             ; Architecture decisions
         :runnote #{"RunNotes-2025-10-01-Feature"}  ; Planning docs
         :code #{"src/file.py:45-67"}    ; Implementation
         :tests #{"test/file.py:12"}}    ; Verification
 :nfr-taxonomy {:iso-25010 #{:performance-efficiency/time-behavior}}}
```

### Required Sections

```markdown
## Requirement Statement
**[MUST|SHOULD|MAY]** [Clear, testable statement]

## Context
[Why this requirement exists, business driver, ADR constraints]

## Acceptance Criteria
- [ ] **AC-1**: [Testable, measurable criterion]
- [ ] **AC-2**: [Another criterion]
```

**Complete format specification:** [doc/README-FILE-FORMAT.md](doc/README-FILE-FORMAT.md)

---

## Decision Framework

Before completing a requirement, ask these 6 questions:

### 1. Is This Testable?
- Can we write a test (automated or manual)?
- Are acceptance criteria measurable?
- Is success/failure unambiguous?

**If NO:** Rewrite with measurable criteria

### 2. Is It Traceable?
- [ ] Business need documented? (RunNotes)
- [ ] Architectural constraints identified? (ADRs)
- [ ] Implementation located? (code)
- [ ] Verification present? (tests)

**If gaps:** Add missing links

### 3. Does It Conflict with ADRs?
Search ADRs before creating requirements:
```bash
adr-search content "domain"
```

**If conflict:** Update requirement, supersede ADR, or reject

### 4. MUST/SHOULD/MAY - Which Is It?
- Can we ship without this? (No → MUST)
- Legally required? (Yes → MUST)
- Best practice but negotiable? (Yes → SHOULD)
- Optional enhancement? (Yes → MAY)

### 5. Is This One Requirement or Multiple?
**Signs of compound:** Multiple "AND" clauses, unrelated acceptance criteria

**If compound:** Decompose into atomic requirements

### 6. Is There a Non-Functional Aspect?
Check for: performance, security, usability, reliability constraints

**If yes:** Add `:nfr-taxonomy` with ISO 25010

**Detailed decision framework:** [doc/README-QUALITY.md#decision-framework](doc/README-QUALITY.md#decision-framework)

---

## Common Pitfalls Checklist

Before finalizing a requirement:

- [ ] **Search first** - Checked for duplicates? (`req-search`, `adr-search`, `runnote-search`)
- [ ] **Testable** - Objective verification criteria defined?
- [ ] **RFC 2119** - Correct keyword (MUST/SHOULD/MAY)?
- [ ] **Atomic** - Single concern, not compound?
- [ ] **Context** - Explains WHY, not just WHAT?
- [ ] **Traceability** - Appropriate links for status?
- [ ] **Validated** - `req-validate` passes?
- [ ] **ISO 25010** - NFRs mapped to quality taxonomy?

**Detailed pitfalls and fixes:** [doc/README-QUALITY.md#common-pitfalls](doc/README-QUALITY.md#common-pitfalls)

---

## ISO 25010 Quick Reference

For non-functional requirements, map to ISO 25010 quality characteristics:

| Category | Subcategories (examples) |
|----------|--------------------------|
| **Performance Efficiency** | time-behavior, resource-utilization, capacity |
| **Security** | confidentiality, integrity, authenticity, accountability |
| **Reliability** | maturity, availability, fault-tolerance, recoverability |
| **Usability** | learnability, operability, accessibility |
| **Maintainability** | modularity, reusability, testability |
| **Compatibility** | co-existence, interoperability |
| **Portability** | adaptability, installability |

**Usage:**
```edn
{:nfr-taxonomy {:iso-25010 #{:performance-efficiency/time-behavior
                             :reliability/fault-tolerance}}}
```

**Complete ISO 25010 taxonomy:** [doc/README-FILE-FORMAT.md#iso-25010-taxonomy-structure](doc/README-FILE-FORMAT.md#iso-25010-taxonomy-structure)

---

## Agent Workflow Integration

### Coordination with Other Agents

- **software-architect:** Identifies architectural implications, reviews NFR taxonomy
- **documentation-manager:** Validates completeness, checks traceability currency
- **test-strategist:** Reviews acceptance criteria, validates test coverage
- **adr-curator:** Documents architectural decisions, maintains bidirectional links

**Detailed multi-agent workflows:** [doc/README-INTEGRATION.md#agent-ecosystem-integration](doc/README-INTEGRATION.md#agent-ecosystem-integration)

---

## Quick Command Reference

### Search Before Creating
```bash
req-search content "topic"        # Find similar requirements
req-search tag :security          # Search by tag
adr-search content "tech"         # Check architectural constraints
runnote-search tag planning       # Find business context
```

### Validate
```bash
req-validate                      # Validate all requirements
req-validate --check-new          # Pre-commit: check staged files only
req-validate --ci                 # CI mode: minimal output
```

### Traceability
```bash
req-trace summary                 # Coverage overview
req-trace gaps adr                # Missing ADR links
req-trace gaps code               # Missing implementation
req-trace gaps tests              # Missing verification
req-trace detail REQ-AUTH-001     # Specific requirement traceability
```

### Fix Issues
```bash
req-fix-metadata --dry-run        # Preview fixes
req-fix-metadata                  # Apply auto-fixes
```

### Discovery
```bash
req-search list                   # List all requirements
req-search list-tags              # Show tag taxonomy
req-search list-categories        # Show category taxonomy
req-search summary                # Summary statistics
```

**Complete command documentation:** [README.md](README.md)

---

## See Also

- **[README.md](README.md)** - Installation, configuration, usage (for humans)
- **[doc/README-WORKFLOWS.md](doc/README-WORKFLOWS.md)** - Detailed elicitation, inference, validation workflows
- **[doc/README-FILE-FORMAT.md](doc/README-FILE-FORMAT.md)** - Complete format specifications, metadata fields, EDN syntax
- **[doc/README-INTEGRATION.md](doc/README-INTEGRATION.md)** - Integration with ADR, RunNotes, code, tests
- **[doc/README-QUALITY.md](doc/README-QUALITY.md)** - Quality enforcement, RFC 2119, testability, pitfalls
- **Templates:** `~/.req/template/` - Functional, non-functional, default templates
- **[RFC 2119](https://www.rfc-editor.org/rfc/rfc2119)** - Key words for use in RFCs
- **[ISO 25010](https://iso25000.com/index.php/en/iso-25000-standards/iso-25010)** - Quality model standard
