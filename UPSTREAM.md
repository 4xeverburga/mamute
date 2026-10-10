# Upstream provenance

This repository is a **shallow mirror** of an open-source project, kept for internal
migration / modernization demos and dataset building. It is **not** affiliated with,
endorsed by, or maintained by the original authors.

| | |
|---|---|
| Upstream | https://github.com/caelum/mamute |
| Commit | `bc0a94d` (2019-03-07) |
| License | Apache License 2.0 — see [`LICENSE`](LICENSE) |
| Clone depth | 1 (upstream history is truncated) |
| Local changes | added this `UPSTREAM.md` only |

## About the project

Mamute is a Stack Overflow-style Q&A web application built with VRaptor + Hibernate,
serving JSP views. Java 7 source/target level.

## Runtime requirements (verified)

- **The running application requires MySQL** — Hibernate `SessionFactory` is built at
  CDI startup and only MySQL configs exist in main scope.
- **The test suite needs no external database:** `mvn test -Dmamute.database=h2`
  uses embedded H2 (`src/test/resources/test/hibernate-h2.cfg.xml`).
  Verified green: 294 tests, 0 failures, 0 errors.
- Solr is embedded (`solr.embedded=true`), so no external search server is needed.

All rights remain with the original authors. Redistributed under the terms of the
Apache License 2.0.
