#!/usr/bin/env bb

(ns ^:clj-kondo/ignore req-core
  "Core utilities for requirements management system.
   Delegates to config-core.bb with :req system type."
  (:require [babashka.fs :as fs]
            [clojure.java.io :as io]
            [clojure.string :as str]))

;; Load shared libraries
(def lib-dir (str (System/getenv "HOME") "/.lib"))
(load-file (str lib-dir "/config-core.bb"))
(load-file (str (System/getenv "HOME") "/.req/req-metadata-extractor.bb"))

;; Import functions from config-core
(def discover-project-root config-core/discover-project-root)
(def find-git-root config-core/find-git-root)
(def load-edn-file config-core/load-edn-file)
(def deep-merge config-core/deep-merge)
(def merge-configs config-core/merge-configs)
(def expand-path config-core/expand-path)
(def get-config-value config-core/get-config-value)
(def req-config config-core/req-config)

;; ============================================================================
;; Requirements-Specific Configuration
;; ============================================================================

(def global-config-path
  "Path to global requirements configuration"
  (fs/expand-home "~/.req/config.edn"))

(def project-config-name
  "Name of project-specific config file"
  ".req.edn")

;; ============================================================================
;; Requirements-Specific Wrappers
;; ============================================================================

(defn load-global-config
  "Load global requirements configuration from ~/.req/config.edn"
  []
  (config-core/load-global-config :req))

(defn load-project-config
  "Load project-specific config from project-root/.req.edn"
  [project-root]
  (config-core/load-project-config :req project-root))

(defn load-config
  "Load and merge requirements configuration.

   Returns:
   {:config {...}           ; Merged configuration
    :system-type :req       ; System type
    :project-root \"...\"    ; Absolute path to project root
    :sources {:global \"...\" ; Path to global config (or nil)
              :project \"...\"}  ; Path to project config (or nil)
    }"
  ([]
   (load-config nil))
  ([project-root-override]
   (config-core/load-config :req project-root-override)))

(defn resolve-req-path
  "Resolve requirements directory path from config."
  [config-result]
  (config-core/resolve-req-path config-result))

(defn resolve-template-dir
  "Resolve requirements template directory from config."
  [config-result]
  (config-core/resolve-req-template-dir config-result))

;; ============================================================================
;; Requirements-Specific Utilities
;; ============================================================================

(defn find-req-files
  "Find all requirement markdown files in the configured directory.

   Filters out:
   - Template file (from config)
   - Excluded files (from config)

   Args:
     config: Configuration map with :req-dir, :template-file, :excluded-files, :recursive

   Returns:
     Sequence of file paths, or nil if directory doesn't exist"
  [config]
  (let [req-dir (:req-dir config)
        recursive? (:recursive config false)
        pattern (if recursive? "**/*.md" "*.md")]
    (when (fs/exists? req-dir)
      (let [files (fs/glob req-dir pattern)]
        (filter #(and (not= (fs/file-name %) (:template-file config))
                      (not ((:excluded-files config) (fs/file-name %)))) files)))))

(defn read-req-metadata
  "Read requirement file and extract metadata.

   Args:
     file: File object (java.io.File) to read

   Returns:
     Map with extracted metadata including:
     - :file - filename
     - :path - full path
     - :title - document title
     - :req-id - requirement ID
     - :type - requirement type
     - :category - requirement category
     - :priority - priority level
     - :status - requirement status
     - :tags - set of tags
     - :trace - traceability links
     - :nfr-taxonomy - NFR taxonomy references
     - :content - full file content
     - :metadata - raw metadata

     Returns nil if file cannot be read or has no valid metadata"
  [file]
  (try
    (let [content (slurp file)
          lines (str/split-lines content)
          title-line (first (filter #(str/starts-with? % "#") lines))
          title (when title-line (str/trim (subs title-line 1)))
          extract-req-metadata (:extract-req-metadata req-metadata-extractor/exports)
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

(defn list-reqs
  "List all requirement files in directory with metadata.

   Args:
     req-dir: Directory path containing requirement files
     recursive?: If true, search subdirectories (default false)

   Returns:
     Sorted sequence of requirement metadata maps (sorted by :req-id),
     or nil if directory doesn't exist"
  ([req-dir]
   (list-reqs req-dir false))
  ([req-dir recursive?]
   (when (fs/exists? req-dir)
     (let [pattern (if recursive? "**/*.md" "*.md")
           files (fs/glob req-dir pattern)]
       (->> files
            (filter #(re-matches #"REQ-(?:[A-Z]+-)+\d{3,5}.*\.md" (fs/file-name %)))
            (map #(read-req-metadata (io/file (str %))))
            (filter some?)
            (sort-by :req-id))))))

;; ============================================================================
;; Export for use by other scripts
;; ============================================================================

(def exports
  "Exported functions for requirements scripts"
  {:discover-project-root discover-project-root
   :load-config load-config
   :resolve-req-path resolve-req-path
   :resolve-template-dir resolve-template-dir
   :get-config-value get-config-value
   :req-config req-config
   :expand-path expand-path
   :find-req-files find-req-files
   :read-req-metadata read-req-metadata
   :list-reqs list-reqs})
