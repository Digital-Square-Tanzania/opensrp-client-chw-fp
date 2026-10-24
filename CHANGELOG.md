 Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).


2.0.1-SNAPSHOT - 2026-10-24

- Count active household Family Planning clients on both current and legacy
  register schemas, preserving the legacy non-emergency flag where present.
- Retain the published 2.0 SQLCipher and client-core compatibility changes.
- Use the thread-safe native-form 3.2.0 NACP release in the library and sample.
- Preserve AAR dependency types, the JaCoCo runtime classifier and existing
  SQLCipher exclusions in published Maven metadata.
