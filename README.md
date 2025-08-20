# R2DBC Kotlin Querydsl

---

R2DBC Kotlin Querydsl provides extensions of [spring-data-r2dbc](https://github.com/spring-projects/spring-data-relational)
that enable to build R2DBC queries by kotlin dsl and coroutines.

## Motivation

---

The spring-data-r2dbc module provides `R2dbcEntityTemplate` class for entity-based-query.
However, the R2dbcEntityTemplate is inadequate to build complicated query and use in kotlin.
1. All columns in query are referred by their names as string. Therefore, we cannot know that a field name in query really exists in entity until query is executed.
2. The `R2dbcEntityTemplate` doesn't provide JOIN and subqueries feature.
3. It is impossible to project result to other dto.
4. The `R2dbcEntityTemplate` doesn't provide kotlin coroutine feature.

The goal: make the kotlin user build more complicated R2DBC query by the dsl way.

## Feature

---
This modules..

1. uses Kotlin reflection to build table and column, so it does not need additional build time to create table or column objects.
2. uses KClass, KProperty instead of the name of them to reflect table and column name, so queries are type referenced 
3. provides query results by kotlin coroutines, not a reactor.
4. uses core spring-data-relational query, so you can build more complex query

## Getting Started

---

### Configuration

By default, this module needs R2DBC-spi [ConnectionFactory](https://r2dbc.io/spec/0.8.0.RELEASE/api/io/r2dbc/spi/ConnectionFactories.html) configuration.
`ConnectionFactory` differs by R2DBC support DBMS drivers dependency. Show supported [drivers](https://r2dbc.io/drivers/).

---

```kotlin
@Configuration
class R2dbcConfig {
    @Bean
    fun r2dbcKotlinQueryDsl(connectionFactory: ConnectionFactory): R2dbcKotlinQueryDsl {
        val r2dbcDatabaseClient = DatabaseClient.create(connectionFactory)
        val dialect = DialectResolver.getDialect(connectionFactory)
        return R2dbcKotlinQueryDsl(
            dialect = dialect,
            databaseClient = r2dbcDatabaseClient,
        )
    }
}
```

### Query

---

#### Table

You can create table object by `table(KClass)` function.
For using an entity as a table, the entity should have `@Table(:table_name)` on class.
If you want to alias table, you can use `as(:alias_name)` function.

#### Column

You can create column object by `Table.path(KProperty)` function. A column object need table object. 
For using a field as a column, the entity should have `@Column(:column_name)` on fields.
If you want to alias column, you can use `as(:alias_name)` function.

```kotlin
/**
 * Entity
 */
@Table("person")
data class Person(
    @Id
    @Column("id")
    val id: Long = 0,
    @Column("name")
    val name: String,
)

/**
 * "SELECT p.id FROM person p"
 */
suspend fun getAllIds(): List<PersonIdDto> {
    return databaseClient.selectAll(PersonIdDto::class) {
        val sourceTable = table(Person::class).`as`("p")   // Use table aliasing
        select {
            select(
                sourceTable.path(Person::id).`as`("id1"), // Use column aliasing
            )
        }
        .from { from(sourceTable) }
        .build()
    }
}
```

#### Projection

Projection provides by using constructor. If you need to select only one field and the field is Java primitive type, you can use primitive type projection.

```kotlin
/**
 * Projection Dto
 */
data class PersonIdDto(
    val id: Long
)

/**
 * @return Projected Dto List
 */
suspend fun getAllIds(): List<PersonIdDto> {
    return databaseClient.selectAll(PersonIdDto::class) {
        val sourceTable = table(Person::class).`as`("p")
        select {
            select(
                sourceTable.path(Person::id),
            )
        }
        .from { from(sourceTable) }
        .build()
    }
}

/**
 * This also works
 * @return Long List
 */
suspend fun getAllIds(): List<Long> {
    return databaseClient.selectAll(Long::class) {
        val sourceTable = table(Person::class).`as`("p")
        select {
            select(
                sourceTable.path(Person::id),
            )
        }
            .from { from(sourceTable) }
            .build()
    }
}
```

#### Join

You can use join by `join(:table, :condition)` in From dsl.

```kotlin
/**
 * "SELECT p1.id FROM person p1 JOIN person p2 ON p1.id > p2.id"
 */
suspend fun joinSubquery(): List<PersonIdDto> {
    return databaseClient.selectAll(PersonIdDto::class) {
        val sourceTable = table(Person::class).`as`("p1")
        val joinedTable = table(Person::class).`as`("p2")
        select {
            select(
                sourceTable.path(Person::id),
            )
        }
        .from { from(sourceTable).join(joinedTable, sourceTable.column("id").isGreater(joinedTable.column("id"))) }
        .build()
    }
}
```

#### Subqueries

You can use subqueries by `subquery(:alias)` and select dsl.

```kotlin
/**
 * "SELECT p.id FROM person p JOIN (SELECT p.id FROM person p WHERE p.id = 1) sub1 ON sub1.id = p.id"
 */
suspend fun joinSubquery(): List<PersonIdDto> {
    return databaseClient.selectAll(PersonIdDto::class) {
        val sourceTable = table(Person::class).`as`("p")
        val subquery = subquery("sub1") {
            select {
                select(
                    sourceTable.path(Person::id),
                )
            }
            .from { from(sourceTable) }
            .where {
                sourceTable.path(Person::id).isEqualTo(number(1))
            }
            .build()
        }

        select {
            select(
                sourceTable.path(Person::id),
            )
        }
        .from { from(sourceTable).join(subquery, subquery.column("id").isEqualTo(sourceTable.column("id"))) }
        .build()
    }
}
```