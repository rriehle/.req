#!/usr/bin/env bb

(ns ^:clj-kondo/ignore validator-error-reporting-test
  "Test suite for requirement validator error reporting.
   Tests that validator provides clear, actionable error messages."
  (:require [babashka.fs :as fs]
            [clojure.string :as str]))

;; Load test framework
(load-file (str (fs/expand-home "~/.lib/test/test-framework.bb")))
(def test-case (:test-case test-framework/exports))
(def assert-true (:assert-true test-framework/exports))
(def assert-false (:assert-false test-framework/exports))
(def assert-string-contains (:assert-string-contains test-framework/exports))
(def with-temp-dir (:with-temp-dir test-framework/exports))
(def run-tests (:run-tests test-framework/exports))

;; Load validator
(load-file (str (fs/expand-home "~/.req/bin/req-validate")))
(def validate-metadata (:validate-metadata req-validate/exports))
(def validate-sections (:validate-sections req-validate/exports))

;; ============================================================================
;; Test Fixtures
;; ============================================================================

(def fixture-valid-req
  "# REQ-TEST-001 - Valid Requirement

```edn :metadata
{:req-id \"REQ-TEST-001\"
 :type :functional
 :category :testing
 :priority :must
 :status :proposed
 :tag #{:test :validation}
 :trace {:adr #{}
         :runnote #{}
         :code #{}
         :tests #{}}}
```

## Requirement Statement

This is a valid requirement for testing.

## Context

Test context.

## Acceptance Criteria

- AC-001: Test criterion
")

(def fixture-vector-category
  "# REQ-TEST-002 - Multi-Dimensional Category

```edn :metadata
{:req-id \"REQ-TEST-002\"
 :type :functional
 :category [:pppost :integration :events]
 :priority :must
 :status :proposed
 :tag #{:test}
 :trace {:adr #{}
         :runnote #{}
         :code #{}
         :tests #{}}}
```

## Requirement Statement

Testing vector categories.

## Context

Test context.

## Acceptance Criteria

- AC-001: Test criterion
")

(def fixture-hyphenated-req-id
  "# REQ-SECURITY-CRYPTO-001 - Hyphenated Category

```edn :metadata
{:req-id \"REQ-SECURITY-CRYPTO-001\"
 :type :functional
 :category :security-crypto
 :priority :must
 :status :proposed
 :tag #{:security :crypto}
 :trace {:adr #{}
         :runnote #{}
         :code #{}
         :tests #{}}}
```

## Requirement Statement

Testing hyphenated requirement IDs.

## Context

Test context.

## Acceptance Criteria

- AC-001: Test criterion
")

(def fixture-triple-hyphenated-req-id
  "# REQ-INTEGRATION-VERSION-CONTROL-001 - Triple Hyphen

```edn :metadata
{:req-id \"REQ-INTEGRATION-VERSION-CONTROL-001\"
 :type :functional
 :category :integration-version-control
 :priority :must
 :status :proposed
 :tag #{:integration}
 :trace {:adr #{}
         :runnote #{}
         :code #{}
         :tests #{}}}
```

## Requirement Statement

Testing triple-hyphenated requirement IDs.

## Context

Test context.

## Acceptance Criteria

- AC-001: Test criterion
")

(def fixture-integration-type
  "# REQ-INTEGRATION-001 - Integration Type

```edn :metadata
{:req-id \"REQ-INTEGRATION-001\"
 :type :integration
 :category :integration
 :priority :must
 :status :proposed
 :tag #{:integration}
 :trace {:adr #{}
         :runnote #{}
         :code #{}
         :tests #{}}}
```

## Integration Requirement Statement

Testing integration requirement type and section header.

## Context

Test context.

## Acceptance Criteria

- AC-001: Test criterion
")

(def fixture-invalid-type
  "# REQ-TEST-003 - Invalid Type

```edn :metadata
{:req-id \"REQ-TEST-003\"
 :type :invalid-type
 :category :testing
 :priority :must
 :status :proposed
 :tag #{:test}
 :trace {:adr #{}
         :runnote #{}
         :code #{}
         :tests #{}}}
```

## Requirement Statement

This has an invalid type.

## Context

Test context.

## Acceptance Criteria

- AC-001: Test criterion
")

(def fixture-invalid-priority
  "# REQ-TEST-004 - Invalid Priority

```edn :metadata
{:req-id \"REQ-TEST-004\"
 :type :functional
 :category :testing
 :priority :high
 :status :proposed
 :tag #{:test}
 :trace {:adr #{}
         :runnote #{}
         :code #{}
         :tests #{}}}
```

## Requirement Statement

This has an invalid priority (should be :must, :shall, :should, :may).

## Context

Test context.

## Acceptance Criteria

- AC-001: Test criterion
")

(def fixture-missing-metadata-marker
  "# REQ-TEST-005 - Missing :metadata Marker

```edn
{:req-id \"REQ-TEST-005\"
 :type :functional
 :category :testing
 :priority :must
 :status :proposed
 :tag #{:test}
 :trace {:adr #{}
         :runnote #{}
         :code #{}
         :tests #{}}}
```

## Requirement Statement

Missing :metadata marker on EDN block.

## Context

Test context.

## Acceptance Criteria

- AC-001: Test criterion
")

(def fixture-trace-strings
  "# REQ-TEST-006 - Trace as Strings

```edn :metadata
{:req-id \"REQ-TEST-006\"
 :type :functional
 :category :testing
 :priority :must
 :status :proposed
 :tag #{:test}
 :trace {:adr \"ADR-001\"
         :runnote \"RunNote-001\"
         :code \"code.clj\"
         :tests \"test.clj\"}}
```

## Requirement Statement

Trace fields should be sets, not strings.

## Context

Test context.

## Acceptance Criteria

- AC-001: Test criterion
")

(def fixture-missing-context
  "# REQ-TEST-007 - Missing Context

```edn :metadata
{:req-id \"REQ-TEST-007\"
 :type :functional
 :category :testing
 :priority :must
 :status :proposed
 :tag #{:test}
 :trace {:adr #{}
         :runnote #{}
         :code #{}
         :tests #{}}}
```

## Requirement Statement

This requirement is missing the Context section.

## Acceptance Criteria

- AC-001: Test criterion
")

(def fixture-missing-acceptance
  "# REQ-TEST-008 - Missing Acceptance Criteria

```edn :metadata
{:req-id \"REQ-TEST-008\"
 :type :functional
 :category :testing
 :priority :must
 :status :proposed
 :tag #{:test}
 :trace {:adr #{}
         :runnote #{}
         :code #{}
         :tests #{}}}
```

## Requirement Statement

This requirement is missing Acceptance Criteria.

## Context

Test context.
")

;; ============================================================================
;; Helper Functions
;; ============================================================================

(defn create-test-file
  "Create a test requirement file with given content"
  [temp-dir filename content]
  (let [file-path (str temp-dir "/" filename)]
    (spit file-path content)
    (fs/file file-path)))

(defn validate-file
  "Validate a single requirement file and return results"
  [file-path]
  (let [content (slurp (str file-path))
        file-obj (fs/file file-path)]
    {:metadata (validate-metadata file-obj content)
     :sections (validate-sections file-obj content)}))

;; ============================================================================
;; Tests - Valid Requirements
;; ============================================================================

(defn test-valid-requirement []
  (with-temp-dir
    (fn [temp-dir]
      (let [file (create-test-file temp-dir "REQ-TEST-001.md" fixture-valid-req)
            result (validate-file file)]
        (assert-true (:valid (:metadata result))
                     "Valid requirement should pass metadata validation")
        (assert-true (:valid (:sections result))
                     "Valid requirement should pass section validation")))))

(defn test-vector-category []
  (with-temp-dir
    (fn [temp-dir]
      (let [file (create-test-file temp-dir "REQ-TEST-002.md" fixture-vector-category)
            result (validate-file file)]
        (assert-true (:valid (:metadata result))
                     "Vector categories should be valid after schema fix")))))

(defn test-hyphenated-req-id []
  (with-temp-dir
    (fn [temp-dir]
      (let [file (create-test-file temp-dir "REQ-SECURITY-CRYPTO-001.md" fixture-hyphenated-req-id)
            result (validate-file file)]
        (assert-true (:valid (:metadata result))
                     "Hyphenated requirement IDs should be valid")))))

(defn test-triple-hyphenated-req-id []
  (with-temp-dir
    (fn [temp-dir]
      (let [file (create-test-file temp-dir "REQ-INTEGRATION-VERSION-CONTROL-001.md"
                                   fixture-triple-hyphenated-req-id)
            result (validate-file file)]
        (assert-true (:valid (:metadata result))
                     "Triple-hyphenated requirement IDs should be valid")))))

(defn test-integration-type []
  (with-temp-dir
    (fn [temp-dir]
      (let [file (create-test-file temp-dir "REQ-INTEGRATION-001.md" fixture-integration-type)
            result (validate-file file)]
        (assert-true (:valid (:metadata result))
                     "Integration type should be valid")
        (assert-true (:valid (:sections result))
                     "Integration Requirement Statement section should be valid")))))

;; ============================================================================
;; Tests - Invalid Requirements (Error Reporting)
;; ============================================================================

(defn test-invalid-type-error []
  (with-temp-dir
    (fn [temp-dir]
      (let [file (create-test-file temp-dir "REQ-TEST-003.md" fixture-invalid-type)
            result (validate-file file)
            error (:error (:metadata result))]
        (assert-false (:valid (:metadata result))
                      "Invalid type should fail validation")
        (assert-string-contains error ":type"
                                "Error should mention :type field")
        (assert-string-contains error "invalid-type"
                                "Error should show the invalid value")))))

(defn test-invalid-priority-error []
  (with-temp-dir
    (fn [temp-dir]
      (let [file (create-test-file temp-dir "REQ-TEST-004.md" fixture-invalid-priority)
            result (validate-file file)
            error (:error (:metadata result))]
        (assert-false (:valid (:metadata result))
                      "Invalid priority should fail validation")
        (assert-string-contains error ":priority"
                                "Error should mention :priority field")
        (assert-string-contains error "high"
                                "Error should show the invalid value")))))

(defn test-trace-strings-error []
  (with-temp-dir
    (fn [temp-dir]
      (let [file (create-test-file temp-dir "REQ-TEST-006.md" fixture-trace-strings)
            result (validate-file file)
            error (:error (:metadata result))]
        (assert-false (:valid (:metadata result))
                      "Trace strings should fail validation")
        ;; Should mention that trace fields need to be sets
        (assert-string-contains error "trace"
                                "Error should mention trace field")))))

(defn test-missing-context-error []
  (with-temp-dir
    (fn [temp-dir]
      (let [file (create-test-file temp-dir "REQ-TEST-007.md" fixture-missing-context)
            result (validate-file file)
            error (:error (:sections result))]
        (assert-false (:valid (:sections result))
                      "Missing context should fail validation")
        (assert-string-contains error "Context"
                                "Error should mention missing Context section")))))

(defn test-missing-acceptance-error []
  (with-temp-dir
    (fn [temp-dir]
      (let [file (create-test-file temp-dir "REQ-TEST-008.md" fixture-missing-acceptance)
            result (validate-file file)
            error (:error (:sections result))]
        (assert-false (:valid (:sections result))
                      "Missing acceptance criteria should fail validation")
        (assert-string-contains error "Acceptance Criteria"
                                "Error should mention missing Acceptance Criteria section")))))

;; ============================================================================
;; Test Suite
;; ============================================================================

(def tests
  [(test-case "Valid requirement passes validation" test-valid-requirement)
   (test-case "Vector categories are valid" test-vector-category)
   (test-case "Hyphenated requirement IDs are valid" test-hyphenated-req-id)
   (test-case "Triple-hyphenated requirement IDs are valid" test-triple-hyphenated-req-id)
   (test-case "Integration type and section are valid" test-integration-type)
   (test-case "Invalid type shows clear error" test-invalid-type-error)
   (test-case "Invalid priority shows clear error" test-invalid-priority-error)
   (test-case "Trace strings show clear error" test-trace-strings-error)
   (test-case "Missing context shows clear error" test-missing-context-error)
   (test-case "Missing acceptance criteria shows clear error" test-missing-acceptance-error)])

;; ============================================================================
;; Main
;; ============================================================================

(defn -main []
  (println "\n🧪 Validator Error Reporting Tests")
  (println "===================================\n")
  (doseq [tc tests] tc)  ; Execute each test case (they print results themselves)
  (let [passed (count (filter true? tests))
        failed (count (filter false? tests))]
    (println (str "\n📊 Summary: " passed " passed, " failed " failed"))
    (System/exit (if (zero? failed) 0 1))))

(when (= *file* (System/getProperty "babashka.file"))
  (-main))
