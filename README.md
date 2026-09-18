# ithink-mq-web

A small Spring Boot web app for browsing IBM MQ queues, storing what it finds locally, and searching/managing those messages through a server-rendered UI — without needing a full MQ Explorer install.

## Features

- **Browse a queue** — connect to an IBM MQ queue manager and non-destructively browse every message currently on a queue (messages stay on MQ; nothing is consumed). Each load replaces the local message store with exactly what's on the queue.
- **Search** — full-text search over message id/content/properties, backed by SQLite's FTS5 trigram index, with a live-search box and pagination.
- **Message details** — view a single message's full content and properties (format, priority, persistence, correlation/group IDs, timestamps, etc., captured as JSON), or delete individual messages / purge everything.
- **Connection profiles** — save named MQ connection presets (host, port, queue manager, channel, queue, credentials) and pick one from a dropdown on the home page to prefill the connection form. Full CRUD under *Connection profiles* in the nav bar.
- **Test data** — a "Load test data" action (hidden by default in the header) can PUT a batch of generated sample messages onto the configured dev queue, useful for exercising the browse/search flow without a real producer.

## Requirements

- Java 21
- Access to an IBM MQ queue manager to actually browse messages (the app itself has no other external dependency — it ships a bundled SQLite database)

Use the included Maven wrapper; no local Maven install is required.

## Running the app

```bash
./mvnw spring-boot:run
```

The app starts on port 8080 by default (override with `ITHINK_MQ_APP_PORT`). On first run it creates a `./data` directory with a `ithink-mq.db` SQLite file; the schema is created automatically via Liquibase.

Open `http://localhost:8080` and fill in the connection form (or pick a saved connection profile) with your queue manager's host, port, queue manager name, channel, queue, and optional credentials, then click **Load**.

## Building & testing

```bash
./mvnw clean package   # build
./mvnw test             # run all tests
./mvnw test -Dtest=iThinkMqWebApplicationTests#contextLoads   # run a single test
```

## Tech stack

Spring Boot 4.1.1 (Java 21), Thymeleaf + Thymeleaf Layout Dialect for server-rendered views, Bootstrap 5 (CDN, no frontend build step), SQLite for local storage with Liquibase-managed schema migrations, and the IBM MQ Java client for connecting to queue managers.

## Development

See `CLAUDE.md` for a detailed architecture/conventions guide aimed at contributors (and AI coding assistants) working on this codebase.

A local development queue manager can be reached with:

| Field         | Value                 |
|---------------|-----------------------|
| Host          | `localhost`           |
| Port          | `1414`                |
| Queue Manager | `QM1`                 |
| Channel       | `SYSTEM.ADMIN.SVRCONN`|
| Queue         | `MAIN.Q`              |
