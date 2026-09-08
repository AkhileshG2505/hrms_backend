# HRMS Leave Management Service

A backend service for TexlaCulture's employee leave module: create employees, apply for leave, view leave
requests, and let HR approve or reject them.

## Stack

- Java 17, Spring Boot 3.3.4, Maven
- Spring Data JPA + MySQL
- Spring Security (HTTP Basic Auth)
- Bean Validation (`jakarta.validation`)
- Lombok
- JUnit 5 + Mockito for unit tests

## Running it locally

1. Make sure MySQL is running locally and you have a user that can create databases (the app auto-creates
   the `hrms_leave` schema on first run).
2. Set credentials as environment variables (defaults to `root` / `root` if you don't):
   ```bash
   export DB_USERNAME=root
   export DB_PASSWORD=your_password
   ```
3. Run it:
   ```bash
   mvn spring-boot:run
   ```
4. The app starts on `http://localhost:8080`. On first startup, Hibernate creates the tables and
   `data.sql` seeds a few test employees and users (see below).

### Running tests

```bash
mvn test
```

Unit tests use Mockito to mock the repository layer, so they don't need a real database.

## Test users (seeded via `data.sql`)

| Username  | Password      | Role     | Linked employee |
|-----------|---------------|----------|------------------|
| hr_admin  | hrpass123     | HR       | none             |
| asha      | emppass123    | EMPLOYEE | Asha Rao (id 1)  |
| vikram    | emppass456    | EMPLOYEE | Vikram Shah (id 2)|

Both seeded employees start with a 20 / 18 day annual quota respectively and 0 days taken.

Example call:
```bash
curl -u hr_admin:hrpass123 http://localhost:8080/api/leaves
```

## API overview

| Method | Path                              | Who       | What |
|--------|------------------------------------|-----------|------|
| POST   | `/api/employees`                   | HR        | Create an employee |
| GET    | `/api/employees/{id}`              | HR, or the employee themself | View an employee record |
| POST   | `/api/leaves/employees/{employeeId}` | HR, or the employee themself | Apply for leave |
| GET    | `/api/leaves`                      | Any authenticated user | HR sees all requests; an employee sees only their own |
| GET    | `/api/leaves/{id}`                 | HR, or the owning employee | View one leave request |
| PUT    | `/api/leaves/{id}/approve`         | HR only   | Approve a pending request |
| PUT    | `/api/leaves/{id}/reject`          | HR only   | Reject a pending request |

All endpoints (except `/actuator/health`) require HTTP Basic Auth.

## Design decisions & reasoning

**Why HTTP Basic Auth instead of JWT.** The assignment left the auth mechanism open. Basic Auth satisfies
every stated requirement (authenticated users only, HR-only approve/reject) with the least code — Spring
Security handles credential checking out of the box, so there's no token issuing, signing, or expiry logic
to write, test, or explain live. For a short take-home this felt like the right trade-off: simple, and
does the job asked of it. Documented here in case it comes up in the follow-up.

**Separate `AppUser` (login) from `Employee` (HR record).** These represent different things: an
`Employee` is a business record with a leave balance; an `AppUser` is a login identity. HR staff need to
log in but don't have a leave balance to track, so forcing every login to be an `Employee` would mean
either fake employee rows for HR or a nullable-everything model. Instead, `AppUser` optionally links to an
`Employee` via `employeeId` (`null` for HR). This also keeps password hashes out of the `Employee`
table entirely.

**Leave balance is only reduced on approval, not on apply.** The prompt says "approved leave reduces
[the balance]" — so a `PENDING` request holds a claim on the balance for overlap-checking purposes, but
the balance number itself (`leaveTaken`) doesn't move until HR approves it. Rejected/still-pending
requests never touch the balance.

**Overlap checking blocks on `PENDING` and `APPROVED`, not `REJECTED`.** A rejected request shouldn't
block someone from re-requesting the same dates. Only requests that could still become real leave
(pending) or already are (approved) count as "already booked."

**Balance is re-checked at approval time, not just at apply time.** Between applying and HR approving,
an employee could have had other leave approved in the meantime. Re-validating on approval prevents HR
from accidentally approving more leave than the employee actually has left.

**Validation & errors.** Bean Validation annotations catch malformed input (blank names, invalid emails,
negative quotas, past start dates) before it reaches the service layer. A single `@RestControllerAdvice`
converts every expected failure — validation errors, business rule violations, not-found, wrong role — into
a consistent JSON shape:
```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Bad Request",
  "message": "startDate: startDate cannot be in the past",
  "path": "/api/leaves/employees/1"
}
```
A catch-all handler means an unexpected exception still returns clean JSON (500, generic message) rather
than a raw stack trace — the API never leaks internals, even on a bug.

**Authorization is enforced at two levels.** Role checks (`HR` vs `EMPLOYEE`) use `@PreAuthorize` on
controller methods for endpoints only one role should ever reach (create employee, approve, reject).
Ownership checks (an employee can only see/apply for *their own* records) live in the controller/service,
since Spring Security's role-based `@PreAuthorize` alone can't express "only if this is your own id" without
more machinery than the task needs — a direct comparison against the authenticated user's linked
`employeeId` is simpler to read and test.

**`ddl-auto=update` instead of migrations.** For a scoped take-home this is the pragmatic choice. A real
production service would use Flyway or Liquibase for versioned schema migrations instead.

## Assumptions

- "Yearly leave balance" is a single pool (no separate sick/casual/earned leave types) — kept simple since
  the assignment didn't ask for leave *types*.
- Leave days are counted inclusive of both start and end date, counting all calendar days (no separate
  handling of weekends/holidays) — not specified, so kept simple.
- An employee can have at most one `AppUser` login (enforced by the seed data, not a DB constraint,
  since the assignment didn't specify this either way).
- No endpoint to list *all* employees was requested, so it wasn't built — only create and get-by-id.

## AI assistance disclosure

Per the assignment rules: I used Claude (Anthropic) to help scaffold this project — generating the initial
entity/repository/service/controller structure and the unit tests. I reviewed, understood, and can explain
every part of it, and made the underlying design decisions myself (auth approach, data model, business
rule placement). Happy to walk through and modify any part of it live.
