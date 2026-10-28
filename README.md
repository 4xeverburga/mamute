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

The repository is packaged as a container stack: `docker compose up --build`
starts the application and its database (`compose.yaml`, `Dockerfile`,
`entrypoint.sh`, `.env.example`). `AGENTS.md` is the local runbook,
including how to run it outside the container with JDK 8 and how the web
assets are built.

## Tests

`mvn test -Dmamute.database=h2` runs the suite against an embedded H2
database and needs no external service.

## License

Apache License 2.0 — see `LICENSE`.
