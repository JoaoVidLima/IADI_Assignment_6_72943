# Spring Data JPA — Query Cheat Sheet

> **Key rule:** Method names use your **Kotlin/Java field names**, not `@Column(name = ...)` values.
> Spring reads left to right, chaining through your object graph.

---

## Anatomy of a Derived Query

```
findBy     Club        Id         And    Name       IgnoreCase
  ↑          ↑          ↑           ↑      ↑            ↑
subject   field on   field on   keyword  field on   condition
          Entity     Club type           Entity     modifier
```

---

## Subjects (what operation to perform)

| Method Prefix   | SQL Equivalent         | Return Type       |
|-----------------|------------------------|-------------------|
| `findBy...`     | `SELECT * FROM ...`    | `T` or `List<T>`  |
| `existsBy...`   | `SELECT EXISTS(...)`   | `Boolean`         |
| `countBy...`    | `SELECT COUNT(*) ...`  | `Long`            |
| `deleteBy...`   | `DELETE FROM ...`      | `Unit` / `Long`   |

---

## Condition Keywords (after `By`)

| Spring Keyword              | SQL Equivalent                        | Example Method                              |
|-----------------------------|---------------------------------------|---------------------------------------------|
| `And`                       | `AND`                                 | `findByNameAndCategory`                     |
| `Or`                        | `OR`                                  | `findByNameOrCategory`                      |
| `Not`                       | `!= ?`                                | `findByIdNot(id)`                           |
| `IgnoreCase`                | `LOWER(x) = LOWER(?)`                 | `findByNameIgnoreCase(name)`                |
| `LessThan`                  | `< ?`                                 | `findByDateLessThan(date)`                  |
| `LessThanEqual`             | `<= ?`                                | `findByDateLessThanEqual(date)`             |
| `GreaterThan`               | `> ?`                                 | `findByDateGreaterThan(date)`               |
| `GreaterThanEqual`          | `>= ?`                                | `findByDateGreaterThanEqual(date)`          |
| `Between`                   | `BETWEEN ? AND ?`                     | `findByDateBetween(from, to)`               |
| `Like`                      | `LIKE ?` (you add `%`)               | `findByNameLike("%nova%")`                  |
| `Containing`                | `LIKE %?%` (auto wraps `%`)           | `findByNameContaining("nova")`              |
| `StartingWith`              | `LIKE ?%`                             | `findByNameStartingWith("nova")`            |
| `EndingWith`                | `LIKE %?`                             | `findByNameEndingWith("events")`            |
| `In`                        | `IN (?)`                              | `findByIdIn(listOf(1L, 2L))`               |
| `NotIn`                     | `NOT IN (?)`                          | `findByIdNotIn(listOf(1L, 2L))`            |
| `IsNull`                    | `IS NULL`                             | `findByLocationIsNull()`                    |
| `IsNotNull`                 | `IS NOT NULL`                         | `findByLocationIsNotNull()`                 |
| `True` / `False`            | `= true` / `= false`                  | `findByActiveTrue()`                        |
| `OrderBy...Asc`             | `ORDER BY x ASC`                      | `findByClubIdOrderByDateAsc(id)`            |
| `OrderBy...Desc`            | `ORDER BY x DESC`                     | `findByClubIdOrderByDateDesc(id)`           |

---

## Traversing Relationships

If your entity has a field `club: Club` and `Club` has a field `id: Long`:

```kotlin
// Spring resolves: event.club.id
fun findByClubId(clubId: Long): List<Event>

// Spring resolves: event.eventType.id
fun findByEventTypeId(typeId: Long): List<Event>

// Combining both
fun findByClubIdAndEventTypeId(clubId: Long, typeId: Long): List<Event>
```

> The parameter **name** doesn't matter to Spring — parameters are bound **left to right by position**.

---

## Real Examples (with SQL equivalent)

```kotlin
// Does an event exist with this name (case-insensitive)?
fun existsByNameIgnoreCase(name: String): Boolean
// SQL: SELECT EXISTS(SELECT 1 FROM event WHERE LOWER(name) = LOWER(?))

// Same, but exclude a specific ID (for edit duplicate check)
fun existsByNameIgnoreCaseAndIdNot(name: String, id: Long): Boolean
// SQL: SELECT EXISTS(... WHERE LOWER(name) = LOWER(?) AND id != ?)

// All events for a club
fun findByClubId(clubId: Long): List<Event>
// SQL: SELECT * FROM event WHERE club_id = ?

// All events of a given type
fun findByEventTypeId(typeId: Long): List<Event>
// SQL: SELECT * FROM event WHERE event_type_id = ?

// Events in a date range, ordered newest first
fun findByDateBetweenOrderByDateDesc(from: LocalDate, to: LocalDate): List<Event>
// SQL: SELECT * FROM event WHERE date BETWEEN ? AND ? ORDER BY date DESC

// Find EventType by name (exact match)
fun findByName(name: String): EventType?
// SQL: SELECT * FROM event_type WHERE name = ?
```

---

## `@Query` — When Derived Queries Aren't Enough

Use `@Query` for complex logic: optional filters, explicit JOINs, aggregations, or anything that would make the method name unreadable.

`@Query` uses **JPQL** — SQL-like, but with **entity class names** and **field names**, not table/column names.

```kotlin
// Basic JPQL query
@Query("SELECT e FROM Event e WHERE e.club.id = :clubId")
fun findByClub(@Param("clubId") clubId: Long): List<Event>

// With JOIN (explicit, instead of relying on lazy loading)
@Query("SELECT e FROM Event e JOIN e.club c WHERE c.name = :name")
fun findByClubName(@Param("name") name: String): List<Event>

// Optional filter — only apply condition if param is not null
@Query("SELECT e FROM Event e WHERE (:clubId IS NULL OR e.club.id = :clubId) AND (:typeId IS NULL OR e.eventType.id = :typeId)")
fun findFiltered(
    @Param("clubId") clubId: Long?,
    @Param("typeId") typeId: Long?
): List<Event>

// COUNT per group — e.g. number of events per club
@Query("SELECT e.club.id, COUNT(e) FROM Event e GROUP BY e.club.id")
fun countEventsPerClub(): List<Array<Any>>

// Native SQL (escape hatch — use sparingly)
@Query(value = "SELECT * FROM event WHERE YEAR(date) = ?1", nativeQuery = true)
fun findByYear(year: Int): List<Event>
```

### JPQL vs SQL — Quick Reference

| Concept        | SQL                        | JPQL                          |
|----------------|----------------------------|-------------------------------|
| Table name     | `event`                    | `Event` (entity class name)   |
| Column name    | `club_id`                  | `e.club.id` (field path)      |
| Join           | `JOIN event ON ...`        | `JOIN e.club c` (follow field)|
| Parameter      | `?` or `:name`             | `:name` with `@Param`         |
| Alias          | `e AS event`               | `Event e`                     |

---

## Projections — Fetching Only Specific Fields

Instead of returning full entity objects, you can return only the fields you need.
Spring generates a dynamic proxy at runtime — your entity class needs no changes.

**Key rule:** Getter names in the interface must match your **Kotlin field names**, not `@Column(name = ...)` values.

### 1. Define a projection interface (no `@Entity`, no repository of its own)

```kotlin
// Just a plain interface — put it in your dto or projection package
interface EventTypeNameOnly {
    fun getId(): Long    // matches field 'id' on EventType
    fun getName(): String // matches field 'name' on EventType
}
```

### 2. Use it in your existing repository

```kotlin
@Repository
interface EventTypeRepository : JpaRepository<EventType, Long> {
    fun findAllProjectedBy(): List<EventTypeNameOnly>  // ProjectedBy = use a projection
    //                              ↑
    //              Spring infers WHICH projection from the return type
}
```

### 3. Use it in your code like any interface

```kotlin
val types = eventTypeRepository.findAllProjectedBy()
types.forEach { println(it.getName()) }  // Spring proxy handles it
```

### How Spring knows which projection to use

`ProjectedBy` is the keyword that signals *"return a subset of fields"*.
The **return type** tells Spring which interface to map to — not the method name.

```kotlin
fun findAllProjectedBy(): List<EventTypeNameOnly>     // → uses EventTypeNameOnly
fun findAllProjectedBy(): List<EventTypeFullSummary>  // → uses EventTypeFullSummary
```

### Performance benefit

Spring only fetches the columns declared in the interface:

```sql
-- findAll()
SELECT id, name FROM event_type

-- findAllProjectedBy(): List<EventTypeNameOnly> with only getName()
SELECT name FROM event_type
```

### When to use projections

```
Full entity   →  you need the whole object, or will call save() on it later
Projection    →  read-only, display purposes, dropdowns, API responses
```

> **Important:** Never call `save()` on a projection — it's not a managed entity,
> just a view of the data. Fetch the full entity if you need to modify it.

---

## Summary

```
Derived query  →  simple conditions, readable name is feasible
@Query JPQL    →  complex logic, optional params, aggregations
@Query native  →  last resort — loses type safety, breaks refactoring
Projection     →  read-only, fetch only the fields you need
```