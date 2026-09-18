# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

`ithink-mq-web` is a Spring Boot 4.1.1 (Java 21) web app for browsing IBM MQ queues and searching the messages it has pulled off them. Server-rendered UI via Thymeleaf + the Thymeleaf Layout Dialect, styled with Bootstrap 5 loaded from a CDN (no frontend build step, no static asset pipeline). The MQ browse flow, local persistence, and full-text search are implemented; only IBM MQ publish/put functionality is out of scope so far.

## Commands

Use the Maven wrapper (`mvnw`), not a system-installed Maven.

- Run the app: `./mvnw spring-boot:run` (starts on the default port, 8080)
- Build: `./mvnw clean package`
- Run all tests: `./mvnw test`
- Run a single test class: `./mvnw test -Dtest=iThinkMqWebApplicationTests`
- Run a single test method: `./mvnw test -Dtest=iThinkMqWebApplicationTests#contextLoads`

The app creates a `./data` directory on startup (for the SQLite file) — this is gitignored.

## Architecture

- **Everything lives under `hu.ithink.mq`**, split by layer: `controllers`, `services`, `repositories`, `entities`, `models`, `exceptions`. There is no separate top-level package root anymore — new classes should nest under `hu.ithink.mq.<layer>`, matching the existing classes in that layer.
- **Controllers are package-private** (e.g. `IndexController`, `MessageController` have no `public` modifier) — keep this convention for new controllers.
- **Form-backing/UI models use Lombok's `@Data`** (e.g. `MqConnectionModel`). Read-only view models are plain Java `record`s instead (`MessagePage`, `MessageRow`). `Message` (the JPA entity) uses `@Data` + `@NoArgsConstructor`.
- **Thymeleaf layout pattern**: `templates/layout/base.html` is the master layout (declares the HTML shell, CDN Bootstrap CSS/JS, and a `layout:fragment="content"` slot, plus `header`/`footer` fragment includes, plus an optional `layout:fragment="scripts"` slot for page-specific `<script>` blocks). Individual pages use `layout:decorate="~{layout/base}"` and fill in `<div layout:fragment="content">`. Shared nav/footer markup lives in `templates/fragments/header.html` and `templates/fragments/footer.html` as `th:fragment` blocks; `templates/fragments/messages-table.html` (`th:fragment="table"`) is a reusable fragment also returned directly (as `"fragments/messages-table :: table"`) from an AJAX endpoint for live search — reuse these fragments rather than duplicating markup in new pages.
- **Persistence**: SQLite (via `org.xerial:sqlite-jdbc` + `hibernate-community-dialects`' `SQLiteDialect`) at `./data/ithink-mq.db`, with schema managed by Liquibase (`db/changelog/db.changelog-master.xml` includes `db/changelog/changes/000_base.xml`; `ddl-auto` is `none`). Add new schema changes as new changesets/files rather than editing already-applied ones. The `message` table has a companion `message_fts` FTS5 virtual table (trigram tokenizer, external-content, kept in sync via `AFTER INSERT/UPDATE/DELETE` triggers) used for fast substring search over id/content/properties.
- **Data access is split two ways**: `MessageRepository` (`JpaRepository<Message, String>`) backs simple CRUD, used by `MessageService` (find by id, save all, delete by id, purge all). `MessageQueryService` bypasses JPA and uses `NamedParameterJdbcTemplate` directly with hand-written SQL, because paginated/previewed full-text search against the FTS5 virtual table doesn't map well onto Spring Data derived queries; it also falls back from an FTS `MATCH` query to a `LIKE` query for search terms under 3 characters, since SQLite's trigram tokenizer can't match those.
- **MQ browsing**: `MqQueueBrowseService` connects to IBM MQ (via `com.ibm.mq.allclient`) using the client transport, non-destructively browses every message currently on a queue (`MQOO_BROWSE`, so messages stay on MQ), and upserts them into the local `message` table via `MessageService` for the searchable UI. MQ message properties (format, priority, persistence, correlation/group IDs, put date/time, etc.) are serialized to JSON in the `properties` column. Connection/browse failures are wrapped in `MqBrowseException` and surfaced as a flash-style alert on the index page.
- **Controller flow**: `GET /` renders `index.html` with an empty `MqConnectionModel` plus the first page of stored messages; `POST /load` re-binds the submitted `MqConnectionModel`, triggers `MqQueueBrowseService.browseAndStore`, and re-renders `index` with a success/error message. `GET /messages/fragment` returns just the messages-table fragment for the debounced live-search input in the header. `GET /messages/{id}` shows a single message's full content/properties; `POST /messages/{id}/delete` and `POST /messages/purge` delete one or all stored messages.
- **User-facing strings (flash messages, template labels, confirm dialogs) are in Hungarian** — match this when adding new UI text or error messages.
