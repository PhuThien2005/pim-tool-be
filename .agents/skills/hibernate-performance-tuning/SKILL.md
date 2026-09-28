---
name: hibernate-performance-tuning
description: Guidelines and patterns for optimizing Hibernate & JPA performance, eliminating N+1 queries, profiling SQL via P6Spy, and tuning batching and fetch joins.
---

# Hibernate & JPA Performance Tuning Skill

This skill provides practical methodologies to diagnose, profile, and optimize Hibernate ORM persistence logic in Spring Boot applications, ensuring high throughput and zero N+1 query overhead.

---

## 1. Eliminating the N+1 Query Problem

### 1.1 Detection
- **Symptom:** Querying $N$ parent entities results in $1$ query for parents, followed by $N$ individual queries for child entities/associations (`1 + N`).
- **Detection via P6Spy:** Look for repeating query patterns in console logs or execution reports:
  ```sql
  select group0_.ID from "GROUP" group0_ where group0_.ID = ?
  select group0_.ID from "GROUP" group0_ where group0_.ID = ?
  ...
  ```

### 1.2 Resolution Strategies
- **QueryDSL / JPQL Fetch Join:**
  - Explicitly fetch associations in one SQL JOIN query:
  ```java
  query.select(project)
       .from(project)
       .leftJoin(project.group, group).fetchJoin()
       .leftJoin(project.employees, employee).fetchJoin();
  ```
- **Hibernate Batch Fetching:**
  - Configure `spring.jpa.properties.hibernate.default_batch_fetch_size: 25` in `application.properties`.
  - Alternatively annotate collections with `@BatchSize(size = 25)` on `@OneToMany` or `@ManyToMany`.
- **JPA EntityGraph:**
  - Use `@EntityGraph(attributePaths = {"group", "employees"})` on repository query methods.

---

## 2. P6Spy SQL Query Profiling & Analysis

### 2.1 Configuration
- Include `p6spy-spring-boot-starter` or `spy.properties`.
- Format SQL with custom formatters (e.g. `P6SpySqlFormatter`) showing formatted SQL, execution duration in milliseconds, and parameter bindings.

### 2.2 Benchmarking Protocol
1. Perform operation (e.g., search with pagination, create project, update project).
2. Count the number of `[SQL EXECUTE]` log lines generated.
3. If search returns 10 projects and triggers > 3 SQL queries, investigate un-fetched lazy relationships.

---

## 3. Read vs Write Optimization

### 3.1 Read-Only Transactions
- Annotate read-only service methods with `@Transactional(readOnly = true)`.
- Hibernate sets JDBC connection to read-only mode and disables dirty checking snapshots for loaded entities, reducing memory consumption and CPU cycles.

### 3.2 DTO Projections
- When only a subset of fields is required, avoid loading full managed entities.
- Use Spring Data interface projections or QueryDSL `Projections.constructor()` / `Projections.bean()` to select only required database columns directly into lightweight DTOs.
