# REQ-NFR-[NUMBER] - [Non-Functional Requirement Title]

```edn :metadata
{:req-id "REQ-NFR-[NUMBER]"
 :type :non-functional
 :category :set-me
 :priority :must
 :status :proposed
 :tag #{:non-functional :set-me}
 :nfr-taxonomy {:iso-25010 #{:set-characteristic/set-sub-characteristic}
                :furps+ #{:set-category}}
 :trace {:adr #{}
         :runnote #{}
         :code #{}
         :tests #{}}}
```

## Non-Functional Requirement Statement

**[MUST|SHALL|SHOULD|MAY]** [Clear, measurable statement of quality attribute or constraint]

## Quality Attribute

**ISO 25010 Classification:**
- **Characteristic:** [e.g., Performance Efficiency, Security, Usability]
- **Sub-characteristic:** [e.g., Time Behavior, Authenticity, Learnability]

**FURPS+ Classification:**
- **Category:** [e.g., Performance, Reliability, Supportability]

## Measurable Criteria

| Metric | Target | Measurement Method |
|--------|--------|-------------------|
| [Metric name] | [Specific target value] | [How to measure] |
| [Metric name] | [Specific target value] | [How to measure] |

## Context

[Why is this quality attribute important for this system?]

**Related ADRs:**
- [ADR-#####](path/to/adr.md) - [Architectural decision supporting this NFR]

**Related RunNotes:**
- [RunNotes-YYYY-MM-DD-Topic-phase.md](path/to/runnote.md) - [Analysis or investigation]

## Rationale

[Business or technical justification for this requirement]

**Cost of Non-Compliance:**
- [What happens if we don't meet this requirement?]

**Benefit of Compliance:**
- [What value does meeting this requirement provide?]

## Acceptance Criteria

```edn :acceptance-criteria
[{:id "AC-1"
  :description "[Measurable criterion]"
  :testable true
  :measurement-method "[How to verify]"
  :test-ref "test/path/file_test.clj:line"}]
```

- [ ] **AC-1**: [Metric] achieves [target] under [conditions], measured by [method]
- [ ] **AC-2**: [Metric] achieves [target] under [conditions], measured by [method]

## Measurement & Verification

**Test Environment:**
- [Description of test environment required to verify this NFR]

**Test Procedure:**
1. [Step-by-step procedure to measure the quality attribute]
2. [Include load conditions, data volumes, etc.]

**Acceptance Threshold:**
- **Target:** [Ideal value]
- **Minimum:** [Acceptable minimum]
- **Maximum:** [If applicable, maximum acceptable value]

## Implementation Constraints

**Technical Constraints:**
- [Technology or approach limitations]

**Resource Constraints:**
- [Budget, time, or resource limitations]

**Architectural Impact:**
- [How this NFR influences architectural decisions]

## Trade-offs

**Conflicts With:**
- [Other requirements or quality attributes that conflict]

**Prioritization:**
- [Why this NFR takes precedence or how conflicts are resolved]

## Monitoring Strategy

**Production Monitoring:**
- [How will we continuously verify this NFR in production?]
- [Metrics to track]
- [Alerting thresholds]

## ISO 25010 Quality Model Mapping

**Primary Characteristic:** [e.g., Performance Efficiency]
- **Sub-characteristics:**
  - [Sub-characteristic 1]: [How this NFR relates]
  - [Sub-characteristic 2]: [How this NFR relates]

**Secondary Characteristics:** [Any other quality characteristics affected]

## Dependencies

**Requires:**
- REQ-[CATEGORY]-[NUMBER] - [Functional requirement this depends on]

**Enables:**
- REQ-[CATEGORY]-[NUMBER] - [What this NFR makes possible]

## Traceability

**Business Driver:** [Business need or compliance requirement]

**Architecture:** [Link to relevant ADRs]

**Implementation:** [Link to code implementing this NFR]

**Verification:** [Link to performance tests, security tests, etc.]

## Notes

[Additional considerations, future enhancements, or related documentation]
