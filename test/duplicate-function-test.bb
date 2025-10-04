#!/usr/bin/env bb

(ns ^:clj-kondo/ignore duplicate-function-test
  "Tests for REQ duplicate functions before refactoring.
   Validates current behavior of find-req-files, list-reqs, and read-req-metadata."
  (:require [babashka.fs :as fs]
            [clojure.java.io :as io]
            [clojure.string :as str]))

;; Load test framework
(def lib-dir (str (System/getenv "HOME") "/.lib"))
(load-file (str lib-dir "/test/test-framework.bb"))
(load-file (str (System/getenv "HOME") "/.req/req-metadata-extractor.bb"))

;; Import test utilities
(def test-case (:test-case test-framework/exports))
(def test-suite (:test-suite test-framework/exports))
(def with-temp-dir (:with-temp-dir test-framework/exports))
(def create-test-file (:create-test-file test-framework/exports))
(def create-test-files (:create-test-files test-framework/exports))
(def assert-true (:assert-true test-framework/exports))
(def assert-equal (:assert-equal test-framework/exports))
(def assert-count (:assert-count test-framework/exports))
(def assert-contains (:assert-contains test-framework/exports))
(def assert-not-nil (:assert-not-nil test-framework/exports))
(def run-tests-and-exit (:run-tests-and-exit test-framework/exports))

;; Import metadata extractor
(def extract-req-metadata (:extract-req-metadata req-metadata-extractor/exports))

;; ============================================================================
;; Functions Under Test
;; ============================================================================

(defn find-req-files [config]
  "Copy of find-req-files from req-validate:49"
  (let [req-dir (:req-dir config)]
    (when (fs/exists? req-dir)
      (let [files (fs/glob req-dir "*.md")]
        (filter #(and (not= (fs/file-name %) (:template-file config))
                      (not ((:excluded-files config) (fs/file-name %)))) files)))))

(defn read-req-metadata [file]
  "Copy of read-req-metadata from req-search:31"
  (try
    (let [content (slurp file)
          lines (str/split-lines content)
          title-line (first (filter #(str/starts-with? % "#") lines))
          title (when title-line (str/trim (subs title-line 1)))
          metadata (extract-req-metadata content)]
      (when metadata
        {:file (.getName file)
         :path (.getPath file)
         :title title
         :req-id (:req-id metadata)
         :type (:type metadata)
         :category (:category metadata)
         :priority (:priority metadata)
         :status (:status metadata)
         :tags (set (:tag metadata))
         :trace (:trace metadata)
         :nfr-taxonomy (:nfr-taxonomy metadata)
         :content content
         :metadata metadata}))
    (catch Exception e
      nil)))

(defn list-reqs [req-dir]
  "Copy of list-reqs from req-search:60"
  (let [dir (io/file req-dir)]
    (when (.exists dir)
      (->> (.listFiles dir)
           (filter #(and (.isFile %)
                         (str/ends-with? (.getName %) ".md")
                         (re-matches #"REQ-[A-Z]+-\d{3,5}.*\.md" (.getName %))))
           (map read-req-metadata)
           (filter some?)
           (sort-by :req-id)))))

;; ============================================================================
;; Test Data
;; ============================================================================

(def sample-req-metadata
  "{:req-id \"REQ-SEC-001\"
    :type :functional
    :category :security
    :priority :must
    :status :accepted
    :tag #{:authentication :security}}")

(def sample-req-content
  (str "# User Authentication Requirement

```edn :metadata
" sample-req-metadata "
```

## Requirement Statement
The system must provide secure user authentication.

## Acceptance Criteria
- AC-001: Users can log in with username/password
- AC-002: Failed attempts are logged
"))

;; ============================================================================
;; Test Cases for find-req-files
;; ============================================================================

(defn test-find-req-files-empty-dir []
  (with-temp-dir
    (fn [temp-dir]
      (let [config {:req-dir (str temp-dir)
                    :template-file "REQ-00000-template.md"
                    :excluded-files #{"README.md"}}
            result (find-req-files config)]
        (assert-equal [] (vec result)
                      "Empty directory should return empty list")))))

(defn test-find-req-files-basic []
  (with-temp-dir
    (fn [temp-dir]
      (create-test-files temp-dir
                         {"REQ-SEC-001-authentication.md" "# Auth"
                          "REQ-DATA-002-validation.md" "# Validation"})
      (let [config {:req-dir (str temp-dir)
                    :template-file "REQ-00000-template.md"
                    :excluded-files #{"README.md"}}
            result (find-req-files config)
            filenames (set (map fs/file-name result))]
        (assert-count 2 result "Should find 2 requirement files")
        (assert-contains filenames "REQ-SEC-001-authentication.md" "Should contain first req")
        (assert-contains filenames "REQ-DATA-002-validation.md" "Should contain second req")))))

(defn test-find-req-files-excludes-template []
  (with-temp-dir
    (fn [temp-dir]
      (create-test-files temp-dir
                         {"REQ-00000-template.md" "# Template"
                          "REQ-SEC-001-auth.md" "# Auth"})
      (let [config {:req-dir (str temp-dir)
                    :template-file "REQ-00000-template.md"
                    :excluded-files #{"README.md"}}
            result (find-req-files config)
            filenames (set (map fs/file-name result))]
        (assert-count 1 result "Should exclude template")
        (assert-contains filenames "REQ-SEC-001-auth.md" "Should contain req")))))

;; ============================================================================
;; Test Cases for read-req-metadata
;; ============================================================================

(defn test-read-req-metadata-valid []
  (with-temp-dir
    (fn [temp-dir]
      (let [file-path (create-test-file temp-dir "REQ-SEC-001-auth.md" sample-req-content)
            file-obj (io/file (str file-path))
            result (read-req-metadata file-obj)]
        (assert-not-nil result "Should return metadata map")
        (assert-equal "REQ-SEC-001" (:req-id result) "Should extract req-id")
        (assert-equal :functional (:type result) "Should extract type")
        (assert-equal :security (:category result) "Should extract category")
        (assert-equal :must (:priority result) "Should extract priority")
        (assert-equal :accepted (:status result) "Should extract status")
        (assert-contains (:tags result) :authentication "Should extract tags")
        (assert-contains (:tags result) :security "Should extract security tag")))))

(defn test-read-req-metadata-missing-file []
  (let [result (read-req-metadata (io/file "/nonexistent/file.md"))]
    (assert-true (nil? result)
                 "Should return nil for missing file")))

(defn test-read-req-metadata-invalid-content []
  (with-temp-dir
    (fn [temp-dir]
      (let [file-path (create-test-file temp-dir "REQ-BAD-001.md" "# Bad\n\nNo metadata")
            result (read-req-metadata file-path)]
        (assert-true (nil? result)
                     "Should return nil for invalid metadata")))))

;; ============================================================================
;; Test Cases for list-reqs
;; ============================================================================

(defn test-list-reqs-empty-dir []
  (with-temp-dir
    (fn [temp-dir]
      (let [result (list-reqs (str temp-dir))]
        (assert-equal [] (vec result)
                      "Empty directory should return empty list")))))

(defn test-list-reqs-valid-files []
  (with-temp-dir
    (fn [temp-dir]
      (create-test-file temp-dir "REQ-SEC-001-auth.md" sample-req-content)
      (create-test-file temp-dir "REQ-SEC-002-authz.md"
                        (str/replace sample-req-content "REQ-SEC-001" "REQ-SEC-002"))
      (let [result (list-reqs (str temp-dir))]
        (assert-count 2 result "Should find 2 requirements")
        (assert-equal "REQ-SEC-001" (:req-id (first result)) "Should be sorted by req-id")
        (assert-equal "REQ-SEC-002" (:req-id (second result)) "Second should be REQ-SEC-002")))))

(defn test-list-reqs-filters-invalid []
  (with-temp-dir
    (fn [temp-dir]
      (create-test-file temp-dir "REQ-SEC-001-auth.md" sample-req-content)
      (create-test-file temp-dir "README.md" "# README")
      (create-test-file temp-dir "notes.txt" "Notes")
      (let [result (list-reqs (str temp-dir))]
        (assert-count 1 result "Should only include valid requirement files")
        (assert-equal "REQ-SEC-001" (:req-id (first result)) "Should be the valid req")))))

(defn test-list-reqs-naming-pattern []
  (with-temp-dir
    (fn [temp-dir]
      (create-test-file temp-dir "REQ-SEC-001.md" sample-req-content)
      (create-test-file temp-dir "REQ-DATA-12345.md"
                        (str/replace sample-req-content "REQ-SEC-001" "REQ-DATA-12345"))
      (create-test-file temp-dir "REQ-BAD.md" "# Bad - no number")
      (let [result (list-reqs (str temp-dir))
            req-ids (set (map :req-id result))]
        (assert-count 2 result "Should match REQ-[A-Z]+-\\d{3,5} pattern")
        (assert-contains req-ids "REQ-SEC-001" "Should include REQ-SEC-001")
        (assert-contains req-ids "REQ-DATA-12345" "Should include REQ-DATA-12345")))))

(defn test-list-reqs-nonexistent-dir []
  (let [result (list-reqs "/nonexistent/directory")]
    (assert-true (nil? result)
                 "Non-existent directory should return nil")))

;; ============================================================================
;; Test Runner
;; ============================================================================

(defn run-all-tests []
  (println "\n🧪 Testing REQ Duplicate Functions")
  (println "====================================")

  (test-suite "\n📁 find-req-files Tests"
              [["Empty directory" test-find-req-files-empty-dir]
               ["Basic file finding" test-find-req-files-basic]
               ["Excludes template" test-find-req-files-excludes-template]])

  (test-suite "\n📄 read-req-metadata Tests"
              [["Valid metadata" test-read-req-metadata-valid]
               ["Missing file" test-read-req-metadata-missing-file]
               ["Invalid content" test-read-req-metadata-invalid-content]])

  (test-suite "\n📋 list-reqs Tests"
              [["Empty directory" test-list-reqs-empty-dir]
               ["Valid files" test-list-reqs-valid-files]
               ["Filters invalid files" test-list-reqs-filters-invalid]
               ["Naming pattern validation" test-list-reqs-naming-pattern]
               ["Non-existent directory" test-list-reqs-nonexistent-dir]])

  (run-tests-and-exit))

;; ============================================================================
;; Main
;; ============================================================================

(when (= *file* (System/getProperty "babashka.file"))
  (run-all-tests))
