# REQ-[CATEGORY]-[NUMBER] - [Functional Requirement Title]

```edn :metadata
{:req-id "REQ-[CATEGORY]-[NUMBER]"
 :type :functional
 :category :set-me
 :priority :must
 :status :proposed
 :tag #{:functional :set-me}
 :trace {:adr #{}
         :runnote #{}
         :code #{}
         :tests #{}}}
```

## Functional Requirement Statement

**[MUST|SHALL|SHOULD|MAY]** [Clear statement of system behavior or capability]

### Input

[What inputs does this function process?]

### Processing

[What transformation or logic does the system apply?]

### Output

[What outputs or side effects does this function produce?]

## Business Context

**Business Rule:** [Business logic or constraint this implements]

**User Story:** [As a... I want... So that...]

**Related ADRs:**
- [ADR-#####](path/to/adr.md) - [How it influences this requirement]

## Acceptance Criteria

```edn :acceptance-criteria
[{:id "AC-1"
  :description "Given [precondition], when [action], then [expected result]"
  :testable true
  :test-ref "test/path/file_test.clj:line"}]
```

- [ ] **AC-1**: Given [precondition], when [action], then [expected result]
- [ ] **AC-2**: Given [precondition], when [action], then [expected result]

## Error Conditions

| Condition | Expected Behavior |
|-----------|-------------------|
| [Error condition] | [How system should respond] |

## Edge Cases

- [Edge case 1]: [Expected behavior]
- [Edge case 2]: [Expected behavior]

## Validation Rules

- [Validation rule 1]
- [Validation rule 2]

## Implementation Guidance

**Algorithm Considerations:**
- [Performance characteristics]
- [Complexity considerations]

**Data Structures:**
- [Recommended data structures]

## Test Strategy

**Unit Tests:**
- Test happy path with valid inputs
- Test each error condition
- Test edge cases
- Test validation rules

**Integration Tests:**
- Test interaction with dependent components
- Test end-to-end workflow

## Dependencies

**Requires:**
- REQ-[CATEGORY]-[NUMBER] - [Dependency description]

**Blocks:**
- REQ-[CATEGORY]-[NUMBER] - [What this enables]

## Traceability

**Business Need:** [Link to business requirement document]

**Implementation:** `path/to/implementation.clj:line-range`

**Tests:** `test/path/test_file.clj:line`

## Notes

[Additional context, assumptions, or future considerations]
