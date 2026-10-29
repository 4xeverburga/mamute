# Mamute

A Stack Overflow-style Q&A web application: questions, answers, votes, tags,
reputation and moderation. The pages are rendered on the server from JSP
views; the backend is Java behind VRaptor 4.

## Stack

- VRaptor 4 + Weld CDI + Hibernate, Java 7 source/target
- JSP views (49 pages, 78 tag files); LESS/JS assets built with grunt
- MySQL 5.7 for the application, embedded H2 for the test suite
- Embedded Jetty 8 for local development

## Running it

`AGENTS.md` is the runbook: JDK 8, Maven, a MySQL 5.7 service and the grunt
build of the web assets, plus the OAuth setup for local sign-in. The
synthetic credentials a checkout boots with are in `.env`.

## Tests

`mvn test -Dmamute.database=h2` runs the suite against an embedded H2
database and needs no external service.

## License

Apache License 2.0 — see `LICENSE`.
