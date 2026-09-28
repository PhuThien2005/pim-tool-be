---
name: elca-coding-standards
description: Enforces ELCA Coding Best Practices (document v1.16) for Java/Spring Boot projects, focusing on General Programming (Sec 2), Exception Handling (Sec 3), Architecture & Design (Sec 5), and Persistence (Sec 9).
---

# ELCA Coding Best Practices & Engineering Guidelines

This skill defines core coding standards, defensive programming rules, and architectural patterns based on the official ELCA Coding Best Practices guidelines (v1.16).

---

## 1. General Programming (Section 2)

### 1.1 Defensive Programming & Clean Code
- **Never Hardcode Magic Numbers or Strings:**
  - Define static final constants with descriptive, self-explanatory names (e.g., `DEFAULT_PAGE_SIZE = 5`).
- **Null Safety & Modern Java Idioms:**
  - Avoid returning `null` for collections or arrays; always return `Collections.emptyList()`, `Collections.emptySet()`, or empty arrays.
  - Utilize `java.util.Optional` strictly for method return values where absence is expected; never use `Optional` as method parameters or class fields.
  - Use `Objects.requireNonNull(val, "errorMessage")` or Spring Assertions for constructor / pre-condition validation.
- **Collection Management:**
  - Program to interfaces (`List`, `Set`, `Map`) rather than concrete classes (`ArrayList`, `HashSet`).
  - Specify initial capacities when collection size is known in advance to avoid repeated array allocations.
  - Prefer immutable collections (`List.copyOf()`, `Collections.unmodifiableList()`) when returning internal state from domain entities or services.
- **String Manipulation:**
  - Use `StringBuilder` when concatenating strings within loops or large dynamic expressions.
  - Use `StringUtils.hasText()` or `StringUtils.isBlank()` consistently across all input checks.

---

## 2. Exception Handling (Section 3)

### 2.1 Exception Strategy & Design Rules
- **Never Swallow Exceptions:**
  - Empty `catch (Exception e) {}` blocks are strictly forbidden. Every caught exception must either be rethrown, translated, or properly logged at an appropriate level (`WARN`/`ERROR`).
- **Do Not Log and Rethrow:**
  - Either log the exception OR rethrow it wrapped in a business exception. Doing both leads to duplicate, cluttered log traces.
- **Custom Business Exception Hierarchy:**
  - Extend unchecked `RuntimeException` for business/domain exceptions (`BusinessException`, `ProjectNotFoundException`, `ProjectNumberAlreadyExistsException`, `VisaNotFoundException`, `InvalidProjectStatusException`).
  - Never throw generic `java.lang.Exception` or `java.lang.RuntimeException`.
  - Never catch `java.lang.Throwable` or `java.lang.Error`.
- **Resource Management:**
  - Always use `try-with-resources` for `AutoCloseable` resources (streams, connections, readers/writers).
- **Internationalization (i18n) & Error Codes:**
  - Exceptions should hold structured error codes or message keys (e.g., `project.number.exists`, `project.notfound`), which are resolved to localized messages by the `GlobalExceptionHandler` using Spring's `MessageSource`.

---

## 3. Architecture & Layering (Section 5)

### 3.1 Strict Layered Boundaries
- **Controller Layer:**
  - Handles HTTP protocol concerns: URI routing, HTTP status codes, request serialization/deserialization, Bean Validation (`@Valid`), and delegation to Services.
  - Controllers must NEVER execute business logic or interact with Repositories/EntityManager directly.
- **Service Layer:**
  - Encapsulates transaction boundaries (`@Transactional`).
  - Orchestrates business rules, validations, entity conversions, and repository invocations.
  - Service methods must return DTOs or domain models to controllers, preventing JPA entities from leaking into presentation layers.
- **Repository / Persistence Layer:**
  - Encapsulates data access mechanisms via Spring Data JPA and QueryDSL.
  - No HTTP or presentation logic should ever be present in repositories.

---

## 4. Persistence & Concurrent Updates (Section 9)

### 4.1 Concurrency Control & Entity Contracts
- **Optimistic Locking:**
  - Entities subject to concurrent modification must declare `@Version private Integer version;`.
  - Handle `ObjectOptimisticLockingFailureException` in `GlobalExceptionHandler` and return HTTP 409 Conflict.
- **Technical ID vs Business Key:**
  - Use surrogate primary keys (`@Id @GeneratedValue(strategy = GenerationType.SEQUENCE)`).
  - Business keys (`projectNumber`, `visa`) must have unique constraints (`@Column(unique = true)` or table-level `uniqueConstraints`).
- **Entity Equality (`equals` & `hashCode`):**
  - Implement Vlad Mihalcea's JPA equality pattern: compare immutable business keys if present, or fallback to class-level hashCode if ID is transient/null.
