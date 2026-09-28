---
name: rest-api-architecture-and-specs
description: Architectural specifications and standards for designing, documenting, and validating RESTful APIs and Global Exception Handling in Spring Boot.
---

# REST API Architecture & Specification Guidelines

This skill provides comprehensive standards for building consistent, maintainable, and robust RESTful Web APIs across Spring Boot applications.

---

## 1. Resource URI & HTTP Method Standards

### 1.1 URI Design
- Use plural nouns for resource endpoints: `/projects`, `/employees`, `/groups`.
- Avoid verbs in URIs: use `DELETE /projects/{id}` instead of `/projects/delete/{id}`.
- Sub-resources represent relationships: `/projects/{id}/employees`.

### 1.2 HTTP Verbs & Status Code Contracts
| Method | URI | Success Status | Semantics |
|---|---|---|---|
| `GET` | `/projects` | `200 OK` | Retrieve list or search page of projects |
| `GET` | `/projects/{id}` | `200 OK` | Retrieve project detail by ID |
| `POST` | `/projects` | `201 Created` | Create new project |
| `PUT` | `/projects/{id}` | `200 OK` | Update existing project |
| `DELETE` | `/projects/{id}` | `204 No Content` | Delete single project |
| `DELETE` | `/projects` (with body/params) | `200 OK` or `204 No Content` | Bulk delete projects |

---

## 2. Unified Error Contract & Exception Handling

### 2.1 Error Response Payload
All non-2xx responses must consistently adhere to the following unified JSON schema:
```json
{
  "timestamp": "2026-09-28T17:30:00.000+07:00",
  "status": 400,
  "errorCode": "VALIDATION_FAILED",
  "message": "Validation failed for request object",
  "details": [
    "Field 'name' is mandatory",
    "Field 'startDate' must be before 'endDate'"
  ]
}
```

### 2.2 Standard HTTP Error Mappings
- **`400 Bad Request`:**
  - `MethodArgumentNotValidException` (Bean Validation on `@Valid @RequestBody`)
  - `BindException` (form/query param binding errors)
  - `MethodArgumentTypeMismatchException` (malformed path variable or query param)
  - `InvalidProjectStatusException`
- **`404 Not Found`:**
  - `ProjectNotFoundException`, `GroupNotFoundException`, `VisaNotFoundException`
- **`409 Conflict`:**
  - `ProjectNumberAlreadyExistsException` (business key conflict)
  - `DataIntegrityViolationException` (database unique constraint violation)
  - `ObjectOptimisticLockingFailureException` (concurrent update conflict)
- **`500 Internal Server Error`:**
  - Generic unhandled exceptions, database connection errors.

---

## 3. Bean Validation & Defensive Checks

- Enforce standard annotations on DTOs:
  - `@NotNull`, `@NotBlank`, `@Size(max = 50)`, `@Pattern`
  - Custom cross-field validation: `@StartBeforeEndDate(startDate = "startDate", endDate = "endDate")`
- Keep validation annotations strictly on Request DTOs, keeping domain entities clean of presentation-specific validation rules.
