# Requirements Management Tools - AI Agent Guide

**Distributed via GitHub Releases** - See [README.md](README.md) for installation and CI/CD integration.

## When Working on Requirements

This toolkit supports **bidirectional requirements engineering**: elicitation (business → requirements) and inference (code → requirements). All documents are modular—read only what you need, when you need it.

### Choose Your Workflow

| Task | Read This Document |
|------|-------------------|
| **Creating requirements from business needs** | [doc/README-WORKFLOWS.md](doc/README-WORKFLOWS.md) - Workflow 1: Elicitation |
| **Documenting existing implementation** | [doc/README-WORKFLOWS.md](doc/README-WORKFLOWS.md) - Workflow 2: Inference |
| **Reviewing requirement quality** | [doc/README-WORKFLOWS.md](doc/README-WORKFLOWS.md) - Workflow 3: Validation |
| **Understanding file format/metadata** | [doc/README-FILE-FORMAT.md](doc/README-FILE-FORMAT.md) |
| **Linking to ADRs, RunNotes, code, tests** | [doc/README-INTEGRATION.md](doc/README-INTEGRATION.md) |
| **Ensuring quality (RFC 2119, testability)** | [doc/README-QUALITY.md](doc/README-QUALITY.md) |
| **Tool usage (req-validate, req-search, etc.)** | [README.md](README.md) |

### Quick Tools Reference

```bash
req-search content "topic"    # Find requirements before creating
req-validate                  # Validate format and completeness
req-trace summary             # Check traceability coverage
req-fix-metadata              # Auto-fix metadata issues
```

### Core Principles

1. **Testable** - If you can't test it, it's not a requirement
2. **Traceable** - Link to RunNotes, ADRs, code, tests
3. **RFC 2119** - Use MUST/SHOULD/MAY precisely
4. **Atomic** - One requirement per file
5. **ISO 25010** - Map non-functional requirements to standard taxonomy

### Integration with Other Tools

- **ADR Tools** - Architectural decisions inform requirements ([doc/README-INTEGRATION.md](doc/README-INTEGRATION.md))
- **RunNotes** - Planning notes are requirements sources ([doc/README-INTEGRATION.md](doc/README-INTEGRATION.md))
- **Code/Tests** - Bidirectional traceability ([doc/README-INTEGRATION.md](doc/README-INTEGRATION.md))

---

**Read modular docs on-demand** - Don't load all documentation upfront; each doc is self-contained and focused on a specific workflow or topic.
