# GoatFarm backend (AnimalDetective)

Spring Boot 3.4 / Java 21 REST API for goat-farm management: farms, goats, pedigree & inbreeding checks,
breeding/pregnancy tracking and vaccination schedules. Authentication is stateless JWT.

## Configuration (environment variables)

| Variable | Required | Purpose |
|---|---|---|
| `SPRING_DATASOURCE_URL` | yes | e.g. `jdbc:postgresql://host/db?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | yes | database credentials |
| `JWT_SECRET` | yes | Base64 key, >= 256 bit: `openssl rand -base64 48` |
| `APP_CORS_ALLOWED_ORIGINS` | recommended | comma-separated frontend origins (default `*` = any, for compatibility) |
| `APP_TIME_ZONE` | optional | e.g. `Asia/Kolkata`; defaults to the JVM zone |
| `JWT_EXPIRATION_MS` | optional | token lifetime, default 7 days |
| `SPRING_PROFILES_ACTIVE` | optional | `dev` enables verbose SQL/debug logging (never in production) |

The application refuses to start if `JWT_SECRET` is missing or too weak. Copy `.env.example` as a starting point.

## Run / test

```bash
mvn verify                         # unit + integration tests (H2 in memory, no external services)
mvn spring-boot:run                # needs the variables above
docker build -t goatfarm . && docker run -p 10000:10000 --env-file .env goatfarm
```

## Package layout

```
controller/   HTTP layer only (thin, constructor-injected)
service/      use cases, transactional boundaries, DTO mapping
pedigree/     PedigreeSnapshot (one query per request), PedigreeTreeBuilder, InbreedingEvaluator + RelationRule
vaccination/  VaccineScheduleCatalog (data-driven schedule), VaccinationPolicy, BoosterSequencePolicy (not enforced)
breeding/     DeliveryUpdateApplier
security/     JwtUtil, JwtFilter, entry point, PasswordService (BCrypt + legacy upgrade)
exception/    domain exceptions + GlobalExceptionHandler
mapper/       stateless entity <-> DTO mappers
repository/   Spring Data repositories
domain/       enums and business constants
```

Extending the system: a new vaccine is one line in `VaccineScheduleCatalog`; a new kinship rule is one
`RelationRule` added to `InbreedingEvaluator`.

## Optional database hardening

`docs/sql/schema-guards.sql` contains reviewed-by-hand statements (duplicate checks, one-active-pregnancy
partial index, back-fill). They are not run automatically.

> Note: the repository ships `mvnw`/`mvnw.cmd` but no `.mvn/wrapper` folder, so use a locally installed `mvn`
> (or run `mvn wrapper:wrapper` once to generate the wrapper).
