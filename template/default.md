# REQ-[CATEGORY]-[NUMBER] - [Title]

```edn :metadata
{:req-id "REQ-[CATEGORY]-[NUMBER]"
 :type :functional
 :category :set-me
 :priority :must
 :status :proposed
 :tag #{:set-me}
 :trace {:adr #{}
         :runnote #{}
         :code #{}
         :tests #{}}}
```

## Requirement Statement

**[MUST|SHALL|SHOULD|MAY]** [Clear, concise statement of what the system must/should/may do]

## Context

[What business need or problem does this requirement address?]

**Related ADRs:**
- [ADR-#####](path/to/adr.md) - [Relevance]

**Related RunNotes:**
- [RunNotes-YYYY-MM-DD-Topic-phase.md](path/to/runnote.md) - [Relevance]

## Rationale

[Why is this requirement necessary? What value does it provide?]

## Acceptance Criteria

```edn :acceptance-criteria
[{:id "AC-1"
  :description "[Testable criterion]"
  :testable true
  :test-ref ""}
 {:id "AC-2"
  :description "[Another testable criterion]"
  :testable true
  :test-ref ""}]
```

- [ ] **AC-1**: [Testable criterion]
- [ ] **AC-2**: [Another testable criterion]
- [ ] **AC-3**: [Another testable criterion]

## Non-Functional Characteristics

**ISO 25010 Quality Attributes:**
- [Quality characteristic]: [Specific target or constraint]

**FURPS+ Categories:**
- [Category]: [Specific requirement]

## Implementation Notes

[Technical considerations, constraints, or guidance for implementation]

## Test Strategy

**Unit Tests:**
- [Test approach]

**Integration Tests:**
- [Test approach]

**Acceptance Tests:**
- [How to verify the requirement is satisfied]

## Dependencies

**Depends On:**
- REQ-[CATEGORY]-[NUMBER] - [Description]

**Blocks:**
- REQ-[CATEGORY]-[NUMBER] - [Description]

## Traceability

**Business Need:** [Link to business requirement or user story]

**Architecture:** [Link to relevant ADRs]

**Implementation:** [Link to code files and line numbers]

**Verification:** [Link to test files]

## Notes

[Additional information, assumptions, or clarifications]
