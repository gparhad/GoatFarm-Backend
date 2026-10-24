# GoatFarm Backend: Refactoring Report

Scope: Phase 1 (analysis), Phase 2 (plan) and Phase 3 (implementation) for the Spring Boot backend in
`GoatFarm-backend.zip`. This report lists every change, every behaviour change, what was verified and
what was **not**, and what you must do before deploying.

---

## 0. Read this first

### 0.1 Verification status (honest summary)

| Check | Result |
|---|---|
| All 99 Java files (86 main + 13 test) parse with `javac` | Yes, 0 syntax errors |
| Business code (entities, DTOs, repositories, mappers, services, controllers, pedigree, vaccination, breeding) **type-checked** with `javac` against stand-ins for Spring/JPA/Lombok/Jackson (the real jars cannot be downloaded in my sandbox) | Yes, 0 errors; a deliberate-error control confirmed the setup catches mistakes |
| Pure logic (inbreeding rules, pedigree trees, vaccine schedule, vaccination policy, delivery updates) **compiled and executed** with a 76-assertion harness | 76 / 76 pass |
| Secret scan: original DB host/user/password and JWT secret do not appear anywhere in the output | Verified |
| `mvn verify`, Spring context start-up, Hibernate schema/query validation, the 13 test classes (67 test methods) | **NOT run.** There is no Maven or dependency cache in my environment. |
| Config/security/exception-handler/logging-aspect classes (`SecurityConfig`, `JwtUtil`, `JwtFilter`, `GlobalExceptionHandler`, `LoggingAspect`) | Parsed and manually reviewed against the Spring Boot 3.4.3 / Spring Security 6.4 / JJWT 0.12.5 APIs, but **not compiled** |

**Run `mvn verify` first.** Treat any failure as a defect in my work, not in your environment. The
integration tests (`ApiIntegrationTest`) are the most likely place for small adjustments (H2 dialect quirks, JSON
path details), because I could not execute them.

### 0.2 Actions required before deploying

1. **Rotate secrets.** The original repo contained a real Neon DB username and password as property defaults and the JWT signing secret in `JwtUtil.java`. Treat both as compromised if the repo was ever pushed or shared. Change the DB password at the provider and generate a new JWT secret.
2. **Set environment variables** (see `.env.example`). The app now **refuses to start** without `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` and `JWT_SECRET` (Base64, at least 256 bits: `openssl rand -base64 48`). This is deliberate: no secret has a default any more.
3. **All users must log in again** after deploy, because the signing secret changes and old tokens become invalid.
4. **Set `APP_CORS_ALLOWED_ORIGINS`** to your frontend origin(s). I kept the permissive `*` default so your existing frontend keeps working, and a start-up warning reminds you. Tightening it is a one-line config change.
5. **Set `APP_TIME_ZONE`** (for example `Asia/Kolkata`) if you host on a UTC machine such as Render; otherwise "today" / "upcoming" windows are off by up to 5.5 hours for Indian users. Default behaviour is unchanged (JVM zone).
6. **Check your frontend against section 3** (behaviour changes). The two most likely to matter: unauthenticated requests now get **401** (was 403), and `POST /api/farm/addFarm` now rejects a second farm for the same user.
7. Optionally run `docs/sql/schema-guards.sql` by hand (duplicate checks and a one-active-pregnancy partial index). It is not run automatically.

---

## 1. What changed, by stage of the plan

### Stage 0: Safety net
- Added H2 (test scope only) and a `test` profile (`src/test/resources/application-test.properties`).
- **67 test methods in 13 classes**: pedigree (evaluator, snapshot, tree builder), vaccination (catalog, policy), delivery updates, password service, JWT, auth service (Mockito), mappers, and `ApiIntegrationTest` (end-to-end HTTP through the real security chain).
- `DemoApplicationTests.contextLoads` now runs on H2 instead of needing a live PostgreSQL.

### Stage 1: Hygiene and wiring
| Plan step | Done |
|---|---|
| 1.1 Dead code / unused imports | Removed `FarmUtil` (unused and would have thrown `ClassCastException`), unused repository methods, commented-out blocks, `Jwks`/`AuthenticatedPrincipal` imports, mixed line endings. Typo package `contoller` renamed to `controller`. |
| 1.2 Constructor injection | All field `@Autowired` removed (controllers, services). |
| 1.3 Transactions | Spring's `@Transactional` (class-level `readOnly = true`, writes opt in). Self-invocation no-ops removed. |
| 1.4 Log safety | `UserData` masks the password in `toString`; `LoginRequest`, `ResetPasswordRequest`, `TokenResponse`, reset response mask secrets; arguments named password/token/secret are masked. **Done before** the package rename so the newly-active controller logging cannot leak credentials. Failures log one line (no 8-argument `log.error` with a lost stack trace); unexpected errors log the full stack once in the exception handler. |
| 1.5 Config | `JwtProperties`/`CorsProperties` via `@ConfigurationProperties`; no secrets or DB defaults in source; `JwtException` import fixed (was Spring's, not JJWT's); `oauth2-resource-server` starter removed (it existed only for that wrong import); verbose Hibernate/Jackson logging moved to the `dev` profile. |
| 1.6 Clock and constants | One `Clock` bean (defaults to the JVM zone = old behaviour; optional `APP_TIME_ZONE`). Magic numbers moved to `BreedingRules` / `VaccinationRules` (150 days, 21 days, 3 months, depths 3/5, 7 days). |

### Stage 2: Types and errors
- **Enums** `PregnancyStatus`, `Gender`; DB columns and JSON keep plain strings.
- **Exceptions:** `ResourceNotFoundException`, `BusinessRuleViolationException`, `InvalidCredentialException`, `InvalidTokenException`. `GlobalExceptionHandler` keeps the existing body shapes (plain text for 401/404/500, `{"error": ...}` for 400).
- **Records** for DTOs that nothing mutates: `AuthUser`, `LoginRequest`, `ResetPasswordRequest`, `ForgotPasswordVerifyRequest/Response`, `BreedingCheckResult`, `UpcomingDeliveryDateData`, `FarmUpdateData`, `InbreedingCheckRequest`, plus the new typed responses. Mutable DTOs that are filled incrementally stay as Lombok classes.
- **Mappers:** one style (stateless final classes), no `@Component` classes with static methods, no boolean-flag method (`toUserDTO(user, true)`), unused parameters removed.
- **Typed responses** replace every `Map<String,Object>` / `HashMap` (JSON keys identical): `InbreedingCheckResponse`, `ApiResult<T>`, `DueVaccinationData`, `TokenResponse`, `MessageResponse`.

### Stage 3: Responsibilities extracted (SOLID)
- `GoatLookup`: the "find goat in farm or 404" snippet that was copy-pasted 8 times, with the caller's message preserved.
- `PedigreeSnapshot` + `PedigreeService`: **one query per request** instead of one per ancestor (see section 4).
- `PedigreeTreeBuilder`: one tree algorithm instead of two different ones.
- `InbreedingEvaluator` + `RelationRule`: ordered, pluggable rules (parent/child, ancestor/descendant, sibling, cousin) with no argument mutation. A new kinship rule is one lambda; the evaluator is untouched (Open/Closed).
- `InbreedingService`, `DeliveryUpdateApplier`: split out of `BreedingRecordService` (370 to 180 lines).
- `VaccineScheduleCatalog`: data-driven table replaces the 60-line string `switch`. `VaccinationPolicy`: the 21-day and deworming-age rules.
- `BoosterSequencePolicy`: the ~60 lines of dead booster-validation code, isolated and documented as **not enforced** (as before: every call was commented out).
- `AuthService`: login logic moved out of `AuthController`.

### Stage 4: Persistence
- Controllers no longer return JPA entities. `GoatResponse`, `FarmRegistrationResponse` reproduce the previous JSON **field for field** (including the nested `vaccinationRecords` / `breedingRecords` arrays and `"goats": null` for a new farm).
- All `@ManyToOne` / owning `@OneToOne` are `LAZY`; targeted join-fetch queries for vaccination lists; `hibernate.default_batch_fetch_size=50` for child collections; `spring.jpa.open-in-view=false`. All DTO mapping happens inside service transactions.
- `@Data` removed from entities (id-based `equals`/`hashCode`; no password in `toString`).
- Repository tidy-up: consistent `@Param`, DB-side ordering for vaccination history, unscoped finder removed.
- `Goat` has `@UniqueConstraint(farm_id, tag_number)`.
- **Not done by design:** `@Version` (see section 5).

### Stage 5: Behaviour-changing fixes
Implemented, because you asked for all changes. Each is listed with before/after in section 3.

### Stage 6: Ops
- `dockerFile` renamed `Dockerfile`; heap sized from container limit (`MaxRAMPercentage`), clean exit on OOM, `HEALTHCHECK` on `/api/auth/health`, `.dockerignore`, `.gitignore` covers `.env`.
- `.github/workflows/ci.yml` runs `mvn verify` (tests no longer silently skipped as the only gate).
- Real `README.md`, `.env.example`, `docs/sql/schema-guards.sql`.

---

## 2. Java API changes (compile-level)

You asked for public signatures to stay unchanged "unless absolutely necessary". HTTP paths, verbs and JSON are preserved (see section 3 for the exceptions); these **Java** signatures changed:

| Where | Before | After | Why |
|---|---|---|---|
| `GoatService.addGoat`, `updateGoatByTagNumberAndFarmId` | returns `Goat` entity | `GoatResponse` | stop leaking entities; same JSON |
| `GoatService.getGoatsByFarm` | `List<Goat>` | `List<GoatResponse>` | same |
| `FarmService.registerFarm` | `Farm` | `FarmRegistrationResponse` | same |
| `BreedingRecordService.checkInbreeding` | `Map<String,Object>` | `InbreedingCheckResponse` | typed, same JSON keys |
| `BreedingRecordService.getLatestPregnantRecord` / `updateDeliveryDetails` | `Map<String,Object>` | `ApiResult<BreedingRecordData>` | same |
| `VaccinationService.getTomorrowVaccinationsByFarm2` | `List<Map<String,Object>>` | **renamed** `getVaccinationsDueInOneWeek` returning `List<DueVaccinationData>` | the old name was misleading (+7 days, suffix `2`) |
| `BreedingRecordController.checkInbreeding` | `@RequestBody Map<String,String>` | `@Valid InbreedingCheckRequest` | same JSON body |
| `AuthUser`, `LoginRequest`, `ResetPasswordRequest`, `ForgotPassword*`, `UpcomingDeliveryDateData`, `FarmUpdateData`, `BreedingCheckResult` | Lombok getters (`getUserId()`) | records (`userId()`) | records; JSON identical |
| `UserService.getUsers`, `UserService.findByUsername`, `BreedingRecordService.getUpcomingDeliveriesWindowDefault`, `FarmUtil` | present | **removed** | unused; `getUsers` returned password hashes |
| Packages | `contoller`, `exceptionHandler`, `util` | `controller`, `exception`, `security` | typo fix (this typo had silently disabled controller logging) |

---

## 3. Behaviour-change ledger

Everything that a client or operator could observe. "Revert" says how to undo it if your frontend depends on the old behaviour.

### 3.1 Security and isolation
| # | Before | After | Revert / note |
|---|---|---|---|
| 1 | Passwords stored and compared in **plaintext** | New/reset passwords are **BCrypt**. Existing plaintext accounts still log in and are **upgraded to BCrypt on their next successful login** (constant-time compare). | No lock-outs. Accounts that never log in again stay plaintext until they do. |
| 2 | Registration accepted any password, even none | Min 6 characters (as reset already required), max 72 bytes (BCrypt limit) | `PasswordService.validateNewPassword` |
| 3 | Login email case-sensitive (registration lower-cased it) | Trimmed, case-insensitive | |
| 4 | Unauthenticated request: **403**. Bad or expired token: **500** (even on `/login`) | **401** JSON `{"error": ...}`. A stale token no longer breaks public endpoints. | Frontend that checks for 403 must check 401. Easiest revert: replace the entry point with the default. |
| 5 | `/forgot-password/verify`: unknown user gave 404 "User not found", wrong phone gave 400 (account enumeration) | Both give **400** "Username and phone number do not match" | |
| 6 | Reset token = UUID stored in plaintext, expired rows kept forever | 256-bit random token; **only its SHA-256 is stored**; expired rows purged; single use enforced atomically. Tokens in flight at deploy time (10 min lifetime) become invalid. | The token is **still returned in the response** (see 5.1). |
| 7 | `POST /api/farm/addFarm` took `farmId` from the body (could overwrite an existing farm) | `farmId` ignored; a user can register **one** farm (second attempt: 400). Still public. | See 5.2 |
| 8 | `POST` goat / breeding / vaccination copied `goatId` / `breedingId` / `vaccinationId` from the body (merge could overwrite another farm's row) | Ids are always generated by the database | |
| 9 | `PUT /api/breeding/{id}` loaded by id only; `goatId` / `mateId` unscoped (any farm) | Scoped to the caller's farm; 404 otherwise | |
| 10 | `passwordHash: null` appeared in user JSON | Field is write-only; absent from responses | Registration still sends the plain password in `passwordHash`. |
| 11 | Sessions default, `/h2-console` allowed (H2 not even a dependency) | Stateless; `/h2-console` rule removed; `/error` permitted so error statuses render correctly | |
| 12 | CORS hardcoded, `*` plus credentials | Configurable via `APP_CORS_ALLOWED_ORIGINS`; **default still `*`** (start-up warning) | Set it in production. |
| 13 | JWT secret in source; `jwt.expiration-ms` (90 days) never read, 7 days hardcoded | Secret from `JWT_SECRET`; lifetime from `JWT_EXPIRATION_MS`, **default 7 days (the value really in effect before)** | Set 7776000000 for 90 days. |

### 3.2 Errors and validation
| # | Before | After |
|---|---|---|
| 14 | Malformed JSON, missing parameter, unsupported method/media type, unknown path: **500** | 400 / 400 / 405 / 415 / 404 with `{"error": ...}` |
| 15 | Unique-constraint violation: 500 | **409** `{"error": "Request conflicts with existing data"}` |
| 16 | 500 body leaked `ex.getMessage()`; a second handler printed stack traces to stdout | `Error: Internal server error`; full stack logged via SLF4J. The `Throwable` handler is gone (JVM `Error`s are no longer swallowed). |
| 17 | Missing `tagNumber` on goat / blank tags on breeding or inbreeding checks: 500 or 404 | 400 with a message (Bean Validation) |
| 18 | Duplicate goat tag within a farm allowed (later lookups crashed) | 400 "Goat with tag 'X' already exists in this farm" |

### 3.3 Domain defects fixed
| # | Before | After |
|---|---|---|
| 19 | Grandsire x granddaughter (when the grandsire has no recorded parents) reported **"Safe for breeding!"** | Detected: "Ancestor-descendant relation detected!" (any direct ancestor within 3 generations) |
| 20 | Dam x son reported safe | Detected as parent-child (both directions) |
| 21 | Common-ancestor list in unspecified (HashSet) order | Sorted, deterministic |
| 22 | Pedigree trees in `check-inbreeding`: one shared "visited" set, so an ancestor appearing on both the father's and mother's side vanished from the second branch; nodes carried only the tag | Same builder as `/details`: ancestor shown in both branches, real cycles flagged, full attributes (breed, gender, birth date, weight, height), unregistered parents shown as marked nodes (the old tree left that branch empty) |
| 23 | `ABORTED` delivery: counts zeroed, then immediately overwritten with the request's values; a missing status nulled the record's status | Counts really are zeroed; missing status is rejected (400) |
| 24 | `PUT /api/user` never changed the name (`setFullName(getFullName())`) | Name updated when supplied; kept when blank. "Farm not found" message for a missing user corrected to "User not found". |
| 25 | Deleting a goat used as a sire failed (FK violation, 500) | Works; the dam's breeding record keeps the sire's tag number, `mate` link cleared |
| 26 | Breeding `expectedKiddingDate` only auto-computed on update | Also computed on create when the client sends none (+150 days) |
| 27 | Updating a record's `goatId` / `mateId` left the tag-number columns stale | Tag numbers re-synced |
| 28 | Concurrent breeding requests could both pass the "no active pregnancy" check | The goat row is locked (`SELECT ... FOR UPDATE`) for the transaction; duplicate-tolerant finder (`findFirst...Desc`) |
| 29 | Upcoming-vaccination queries skipped legacy rows whose denormalised `farm_id` was null | Scoped through the goat; those rows are included |
| 30 | Forgot-password double-use race | Atomic `UPDATE ... WHERE used = false` |
| 31 | Users without a farm: farm-scoped list queries ran with `farm_id IS NULL` (could match orphan rows) | Guarded: empty result / 404 |

### 3.4 Logging
| # | Before | After |
|---|---|---|
| 32 | Controller logging dead (package typo); service logging printed `UserData.toString()` **including the password** | Controller and service logging both active, secrets masked, failures one line (WARN for expected 4xx outcomes, ERROR for bugs) |
| 33 | `BasicBinder=TRACE`, SQL and Jackson `DEBUG` in the default config | `INFO` by default; verbose only with profile `dev` |

---

## 4. Deliberately preserved behaviour (pinned by tests)

- `GET /api/farm` and `GET /api/user` return an empty object with **200** when nothing is found.
- `/api/vaccinations/schedule/tomorrow` still returns what is due in **7 days** (renamed internally, documented, path unchanged).
- Unknown vaccine names get "Not Defined" in 3 months; vaccine names match case-sensitively.
- Goat `PUT` is a full overwrite (nulls included); breeding `PUT` ignores nulls; farm update overwrites `size` / `goatTypes` (null becomes `""`).
- A user without an email can still be created; blank email becomes null.
- Status codes of success paths (201 for breeding and vaccination `POST`, 200 elsewhere) and every existing error **message** text.
- `GET /api/vaccinations/upcoming` returns an empty list for invalid input instead of an error.
- Plain-password-in-`passwordHash` request field name (frontend contract).

### Performance effect (pedigree)
`findSafeBreeders` used to run dozens of single-row queries **per candidate male** and recompute the goat's own ancestors for each. `check-inbreeding` now loads the farm's goats **once** and answers every pedigree question in memory: roughly `1 + 2 (lookups)` queries instead of thousands on a mid-size herd. `GoatService.getGoatHistoryData...` went from 3-4 goat lookups plus up to 62 tree lookups to one goat lookup plus one snapshot query. `GET /allGoats` loads children in batches instead of 2 queries per goat.

---

## 5. Not done, and why

1. **Reset flow still returns the token to the caller.** Securing it properly needs a delivery channel (email/SMS) the project does not have, and removing the token would break your frontend. Anyone who knows an account's email and phone number can still reset that password. Recommended next step: send the token by email or SMS and stop returning it.
2. **`/api/farm/addFarm` and `/api/user/addUser` remain public** because sign-up calls them before a token exists. Mitigated (no overwrite, one farm per user) but anyone can still attach a farm to a user id that has none. Fix: make the frontend call `addFarm` after login and require authentication.
3. **`@Version` (optimistic locking), a real partial unique index and Flyway/Liquibase migrations are not applied.** Adding a version column to existing rows with `ddl-auto=update` can leave nulls that break updates. The index and back-fill are in `docs/sql/schema-guards.sql`; `ddl-auto` is still `update` by default (override with `SPRING_JPA_HIBERNATE_DDL_AUTO=validate` once migrations exist).
4. **No rate limiting / account lockout.** That needs infrastructure or an extra library; out of scope for "no new libraries".
5. **`User.farm` stays eagerly loaded** (inverse side of a one-to-one cannot be lazy without bytecode enhancement). One cheap extra query per user load.
6. **`BoosterSequencePolicy` is not wired in**, matching the old behaviour; enabling it is one line in `VaccinationService` once the business confirms the rules.
7. **`data.sql` is unchanged** (plaintext `password123` seed; Spring only runs it on embedded databases, and plaintext logins get upgraded by the new login path). Do not load it into production.
8. `mvnw` is in the repo but `.mvn/wrapper` is not (that was already the case), so use a local `mvn`.
9. `spring.jpa.database-platform` was dropped (Hibernate 6 auto-detects PostgreSQL); `hikari.connection-test-query` and `connection-timeout` were kept.

---

## 6. Phase 1 smell resolution (traceability)

| Phase 1 category | Status |
|---|---|
| 1. Security and tenant isolation | Largely fixed; items 5.1-5.2 remain by design |
| 2. Defects / dead logic | All listed defects fixed or isolated (section 3.3); dead code removed or parked |
| 3. Architecture / SOLID | `BreedingRecordService` split; Open/Closed via `RelationRule` and `VaccineScheduleCatalog`; constants and enums; DTOs instead of entities; one mapper style |
| 4. Persistence / performance | N+1 pedigree removed; lazy associations; batch fetching; open-in-view off; `@Version` and migrations deferred (5.3) |
| 5. Concurrency / transactions | Spring `@Transactional` with `readOnly`; no self-invocation; pessimistic lock on breeding; atomic token claim; injectable `Clock` |
| 6. Error handling | Typed exceptions; correct 4xx statuses; DB conflicts mapped; no message leakage |
| 7. Duplication / boilerplate | `GoatLookup`, `Values.ifNotNull`, shared tree builder, no hand-built maps |
| 8. Config / build / ops | Secrets out of source; profiles; Dockerfile rename, healthcheck, JVM flags; CI; README; real tests |
| 9. Java 17/21 | Records, switch-free data tables, `Stream.toList()`, pattern-matching `instanceof`, `HexFormat`, text blocks |

---

## 7. File map

```
pom.xml                       + validation starter, + h2 (test); - oauth2-resource-server
Dockerfile, .dockerignore, .gitignore, .env.example, README.md, REFACTORING_REPORT.md
.github/workflows/ci.yml
docs/sql/schema-guards.sql
src/main/resources/           application.properties (no secrets), application-dev.properties, data.sql (unchanged)
src/main/java/com/goatfarm/
  DemoApplication.java
  config/        AppConfig, SecurityConfig, JwtProperties, CorsProperties
  controller/    Auth, BreedingRecord, Farm, Goat, User, Vaccination
  domain/        PregnancyStatus, Gender, BreedingRules, VaccinationRules
  entity/        BreedingRecord, Farm, Goat, PasswordResetRequest, User, VaccinationRecord
  exception/     GlobalExceptionHandler + 4 domain exceptions
  logging/       LoggingAspect
  mapper/        Goat, User, Farm, BreedingRecord, VaccinationRecord
  model/         23 DTOs and records
  pedigree/      PedigreeSnapshot, PedigreeService, PedigreeTreeBuilder, InbreedingEvaluator, RelationRule, RelationMatch
  vaccination/   VaccineScheduleCatalog, VaccinationPolicy, BoosterSequencePolicy
  breeding/      DeliveryUpdateApplier
  repository/    6 repositories
  security/      JwtUtil, JwtFilter, JwtAuthenticationEntryPoint, PasswordService
  service/       Auth, User, Farm, Goat, BreedingRecord, Inbreeding, Vaccination, ForgotPassword
  support/       GoatLookup, Values, Hashing
src/test/...     13 test classes
```

Size: main code grew from 52 files / 2,789 lines to 86 files / 3,604 lines (more, smaller, single-purpose classes plus the new documentation comments); `BreedingRecordService` shrank from 370 to 180 lines.
