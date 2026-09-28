---
name: full-coverage-testing
description: Standard operating procedure and automation patterns for full test coverage engineering, branch & line coverage optimization, and self-verifying test execution in Spring Boot and Java projects.
---

# Full Coverage Testing Skill (Java / Spring Boot)

This skill provides an automated, systematic methodology for achieving and verifying high-fidelity unit and integration test coverage across Spring Boot enterprise applications.

## 1. Core Testing Philosophy
- **Pyramid Distribution:**
  - Fast, isolated Unit Tests (Mockito) for Services and business rules.
  - Web Slice Tests (MockMvc) for Controllers, Bean Validation, and Exception Handlers.
  - Slice Data Tests (H2 `@SpringBootTest` / `@DataJpaTest`) for QueryDSL and Custom Repositories.
- **Branch-Centric Coverage:**
  - Every `if / else`, ternary expression, switch branch, and null/empty check must have at least one dedicated test assertion.
- **Self-Verification Loop:**
  - Automatically run test suites using Maven (`mvn clean test jacoco:report`).
  - Inspect Jacoco metrics or coverage reports to ensure zero regression and high coverage.

## 2. Layer-by-Layer Test Recipe

### A. Controllers (`@WebMvcTest` or `@AutoConfigureMockMvc`)
- Verify HTTP Status codes (200 OK, 201 Created, 204 No Content, 400 Bad Request, 404 Not Found, 409 Conflict, 500 Error).
- Verify JSON payload structure (`jsonPath("$.errorCode")`, `jsonPath("$.message")`, arrays, objects).
- Verify Bean Validation error triggers (`@Valid`, constraints, custom annotations).

### B. Services (`MockitoJUnitRunner` or `@ExtendWith(MockitoExtension.class)`)
- Mock all repository and helper dependencies.
- Test Happy Paths (valid inputs, proper entity mapping and save).
- Test Business Exceptions (duplicate check, not found, invalid status, optimistic locking failure).
- Test Collections/Edge Cases (null lists, empty sets, whitespace trimming).

### C. Repositories & QueryDSL
- Test with populated in-memory data (H2).
- Test null criteria, null pageable, unpaged queries, and custom sorting.
- Assert exact query count and verify absence of N+1 side-effects.

### D. Exceptions & DTOs
- Test constructors, builder methods, getter/setter contracts, and ErrorResponse builders.

## 3. Verification Protocol
1. Execute: `mvn clean test jacoco:report`
2. Parse `target/site/jacoco/jacoco.csv` to calculate Instruction, Branch, and Line coverage.
3. Validate that 100% of test cases pass with zero failures or errors.
