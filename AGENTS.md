# AGENTS.md

Guidelines and technical rules for working on the **NetPay Manager** backend (`net-pay-backend`).

## Project Overview

REST API backend for the NetPay Manager application. It exposes the business logic, HTTP endpoints, JWT-based authentication, and database versioning consumed by the frontend client.

Stack:

- Java 17
- Spring Boot 4.1.1 (managed via Maven parent BOM)
- Spring Data JPA / Hibernate
- Spring Security + JWT (`jjwt` 0.12.6)
- PostgreSQL
- Flyway (schema migrations)
- Lombok

## Architecture

Clean-layered architecture. Dependency rule: **infrastructure → application → domain**. The `domain` layer must never depend on `application` or `infrastructure`.

Source root: `src/main/java/com/navi/net_pay_backend/`

- **`domain/`** — Pure business logic, framework-free.
  - `entity/` — Domain entities (e.g. `AppUser`, `AdmTypology`).
  - `repository/` — Repository interfaces (contracts) implemented by infrastructure.
  - `service/` — Service interfaces (e.g. `PasswordHashService`, `TokenService`, `SecurityContext`).
  - `dto/` — Query/result transfer objects used across layers (e.g. `QueryDto`, `PaginatedResult`).
  - `exception/` — Domain exceptions (extend `DomainException` / `RuntimeException`).
  - `enums/` — Typology-style enums (e.g. `TypologyEnum`).
- **`application/`** — Orchestration of use cases.
  - `use_case/` — Spring `@Service` classes named `*UseCase` (e.g. `LoginUseCase`, `AppUserUseCase`).
  - `model/` — Application-level request/response models (e.g. `LoginRequest`, `LoginResponse`).
- **`infrastructure/`** — Adapters/frameworks.
  - `db/` — DB implementation of domain repository contracts. Names:
    - JPA entities: `*Entity` (e.g. `AppUserEntity`).
    - Spring Data repositories: `*JpaRepository`.
    - Domain contract implementations: `Db*Repository` (e.g. `DbAppUserRepository`).
  - `http/`
    - `controller/` — Spring `@RestController`, paths under `/api/...`.
    - `dto/` — HTTP DTOs (`AppUserResponseDto`), mapped from/to domain via `HttpMapper`.
    - `mapper/` — `HttpMapper` converts domain entities to HTTP DTOs.
    - `middleware/` — Global exception handling (`GlobalExceptionMiddleware`, `@RestControllerAdvice`).
    - `filter/` — HTTP-level filters.
  - `security/` — Security config, JWT filter, Spring Security context adapter.
  - `service/` — Infrastructure implementations of domain service interfaces (e.g. `JwtTokenService`, `BcryptHashService`).
  - `config/` — Bean definitions / wiring.

### Request flow

`Controller` → `*UseCase` → domain repository interface → `Db*Repository` (JPA) → database. Responses are mapped back through `HttpMapper` to HTTP DTOs.

### Key rules

- **Never expose internal DB IDs** (`id`, BIGSERIAL). Use `hashId` (public UUID string) in URLs and responses.
- Domain entities must stay free of Spring/JPA annotations; those belong in `*Entity` infrastructure classes. `Db*Repository` maps between them.
- Keep controller and use-case code thin: no SQL/query logic in controllers.
- Use typed domain exceptions for error flow (see Error handling).

## Dependency Management

Dependencies are managed with **Maven** in the root `pom.xml`.

- Prefer versions managed by the Spring Boot parent BOM — do **not** pin a version for starters.
- Pin an explicit version only when the artifact is not BOM-managed (e.g. `jjwt` `0.12.6`).
- Add a dependency only after verifying it is required and belongs in the correct scope (runtime, test, optional).
- Lombok is configured as an annotation processor in `maven-compiler-plugin`; keep that configuration when touching the build.
- Tools: use the Maven wrapper.

| Command | Purpose |
| --- | --- |
| `./mvnw clean compile` | Build / check compilation |
| `./mvnw test` | Run tests |
| `./mvnw spring-boot:run` | Local dev server (with DevTools hot reload) |

## Code Standards

### Naming conventions

- Domain entities: plain names (`AppUser`, `AdmTypology`).
- Infrastructure JPA entities: `*Entity`.
- Spring Data repositories: `*JpaRepository`.
- Domain repository implementations: `Db*Repository`.
- Use cases: `*UseCase`.
- Domain service interfaces and their infrastructure implementations share the `*Service` name in their respective layers.
- HTTP DTOs: `*Dto` (or `*ResponseDto` / `*RequestDto`).
- Application models: `*Request` / `*Response`.
- Exceptions: `*Exception` (e.g. `InvalidCredentialsException`, `EntityNotFoundException`, `EntityAlreadyExistsException`, `UnauthorizeException`).

### Lombok

Use Lombok consistently: `@Getter`/`@Setter`, `@Builder`, `@RequiredArgsConstructor` for constructor injection, `@NoArgsConstructor`/`@AllArgsConstructor` for entities as needed.

### Dependency injection

Prefer constructor injection via `@RequiredArgsConstructor` (or explicit constructors). Avoid field injection.

### Error handling

- Throw **domain exceptions** from use cases and repositories. Keep messages as short, kebab/snake-case keys (e.g. `"user_not_found"`, `"invalid_email"`) for future i18n.
- `GlobalExceptionMiddleware` maps domain exceptions to HTTP status codes:
  - `EntityNotFoundException` → 404
  - `EntityAlreadyExistsException` → 409
  - `InvalidCredentialsException` / `UnauthorizeException` → 401
  - `DomainException` → 400
  - any other `Exception` → 500
- Add new handlers to `GlobalExceptionMiddleware` for new exception types; do not leak internal stack traces.

### HTTP API

- Controllers use `@RestController` under `/api/<resource>` and return `ResponseEntity` with explicit status codes.
- Use `X-Total-Count` header for pagination metadata.
- CRUD uses `hashId` path variables (`PUT/DELETE /api/users/{hashId}`), never raw DB IDs.
- Public endpoints must be explicitly listed in `SecurityConfig` (`permitAll`); everything else requires authentication.

### Database / soft delete

- Records are soft-deleted by status (`TypologyEnum.DELETED`), not physically removed.
- Repositories/use cases always filter out `DELETED` status (see `AppUserRepository.findByIdAndStatusNot`, etc.).

### Pagination

- Query parameters are captured in a `QueryDto` subclass (`offset`, `size`, `columnOrder`, `asc`); results are wrapped in `PaginatedResult<T>`.

## Database & Migrations

- All schema changes go through **Flyway** scripts in `src/main/resources/db/migration/`.
- Migration naming: `V<n>__<description>.sql` (e.g. `V3__add_root_user.sql`). Version numbers are monotonic and must never be reused.
- **Never modify or rewrite an already-applied migration** — add a new `V<n+1>` migration instead.
- `spring.jpa.hibernate.ddl-auto=validate`: entities must match the schema; update entities and migrations together.
- `spring.flyway.clean-disabled=true`: never drop data via the app.

## Security

- JWT-based stateless auth (see `SecurityConfig`, `JwtAuthenticationFilter`, `JwtTokenService`).
- CORS restricted to `http://localhost:4200` (Angular dev server) — extend via config when the frontend origin changes.
- **Credentials and secrets must NOT be hardcoded.** Move `spring.datasource.*` credentials and `security.jwt.secret` to environment variables / external config when committing. Never commit new secrets.

## Git Workflow

- Branches: `main` (stable), `develop` (integration). Feature work uses `feature/NPM-<id>-<short-slug>` branches based on `develop`.
- Commit message convention: `NPM-<id> #comment <type>: <summary> #time <duration>` (Redmine activity format), e.g. `NPM-15 #comment feat: add login use case #time 1h`. Use conventional prefixes: `feat:`, `fix:`, `refactor:`, `docs:`, `test:`.
- Keep commits focused on a single change; stage only intended files.
- Verify with `./mvnw clean compile` (and tests where relevant) before committing.