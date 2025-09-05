package io.nemga123.r2dbc.kdsl.dsl

import org.junit.jupiter.api.Test
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.convert.MappingR2dbcConverter
import org.springframework.data.r2dbc.convert.R2dbcConverter
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.dialect.R2dbcDialect
import org.springframework.data.r2dbc.mapping.R2dbcMappingContext
import org.springframework.data.relational.core.dialect.RenderContextFactory
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.RelationalMappingContext
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.relational.core.sql.render.SqlRenderer

import org.assertj.core.api.Assertions.*
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.dialect.MySqlDialect
import org.springframework.data.relational.core.sql.Select
import org.springframework.r2dbc.core.DatabaseClient

/**
 * Unit tests for {@link SelectQueryDsl}.
 */
class SelectDslTests {
    private val dialect: R2dbcDialect = MySqlDialect()
    private val converter = this.createConverter(dialect)
    private val mappingContext: RelationalMappingContext = converter.mappingContext as RelationalMappingContext
    private val renderContextFactory: RenderContextFactory = RenderContextFactory(dialect)
    private val sqlRenderer: SqlRenderer = SqlRenderer.create(renderContextFactory.createRenderContext())

    private fun createConverter(dialect: R2dbcDialect): R2dbcConverter {
        val customConversions = R2dbcCustomConversions.of(dialect, emptyList<Any>())

        val context = R2dbcMappingContext()
        context.setSimpleTypeHolder(customConversions.simpleTypeHolder)

        return MappingR2dbcConverter(context, customConversions)
    }

    @Test
    fun `simple_select_test`() {

        val select: Select = SelectQueryDsl(mappingContext).run {
            val sourceTable = table(Person::class).`as`("p")
            select(
                sourceTable.path(Person::id),
            )
                .distinct(true)
                .from { from(sourceTable) }
                .build()
        }

        val query = sqlRenderer.render(select)
        val sql = "SELECT DISTINCT p.id FROM person p"

        assertThat(query).isEqualToIgnoringWhitespace(sql)
    }

    @Test
    fun `simple_select_with_condition_test`() {
        val select: Select = SelectQueryDsl(mappingContext).run {
            val sourceTable = table(Person::class).`as`("p")
            select(
                sourceTable.path(Person::id),
            )
            .distinct(true)
            .from { from(sourceTable) }
            .where {
                sourceTable.path(Person::id).isEqualTo(number(1))
            }
            .build()
        }

        val query = sqlRenderer.render(select)
        val sql = "SELECT DISTINCT p.id FROM person p WHERE p.id = 1"

        assertThat(query).isEqualToIgnoringWhitespace(sql)
    }

    @Test
    fun `select_with_simple_function_test`() {
        val select: Select = SelectQueryDsl(mappingContext).run {
            val sourceTable = table(Person::class).`as`("p")
            select(
                greatest(sourceTable.path(Person::id)),
                least(sourceTable.path(Person::id)),
                lower(sourceTable.path(Person::id)),
                upper(sourceTable.path(Person::id)),
            )
            .from { from(sourceTable) }
            .build()
        }

        val query = sqlRenderer.render(select)
        val sql = "SELECT GREATEST(p.id), LEAST(p.id), LOWER(p.id), UPPER(p.id) FROM person p"

        assertThat(query).isEqualToIgnoringWhitespace(sql)
    }

    @Test
    fun `select_from_subquery_test`() {
        val select: Select = SelectQueryDsl(mappingContext).run {
            val sourceTable = table(Person::class).`as`("p")
            val subquery = subquery("sub1") {
                select(
                    sourceTable.path(Person::id),
                )
                .from { from(sourceTable) }
                .where {
                    sourceTable.path(Person::id).isEqualTo(number(1))
                }
                .build()
            }
            val subquery2 = subquery("sub2") {
                select(
                    sourceTable.path(Person::id),
                )
                .from { from(sourceTable) }
                .where {
                    sourceTable.path(Person::id).isEqualTo(number(1))
                }
                .build()
            }
            select(
                subquery.column("id")
            )
            .from { from(subquery).join(subquery2, subquery.column("id").isEqualTo(subquery2.column("id"))) }
            .build()
        }

        val query = sqlRenderer.render(select)
        val sql = "SELECT sub1.id FROM (SELECT p.id FROM person p WHERE p.id = 1) sub1 JOIN (SELECT p.id FROM person p WHERE p.id = 1) sub2 ON sub1.id = sub2.id"

        assertThat(query).isEqualToIgnoringWhitespace(sql)
    }

    @Test
    fun `select_with_join_test`() {
        val select: Select = SelectQueryDsl(mappingContext).run {
            val sourceTable = table(Person::class).`as`("p1")
            val joinTable = table(Person::class).`as`("p2")
            select(
                sourceTable.path(Person::id),
                sourceTable.path(Person::name)
            )
            .from {
                from(sourceTable)
                    .join(joinTable, sourceTable.path(Person::id).isEqualTo(joinTable.path(Person::id)))
            }
            .build()
        }

        val query = sqlRenderer.render(select)
        val sql = "SELECT p1.id, p1.name FROM person p1 JOIN person p2 ON p1.id = p2.id"

        assertThat(query).isEqualToIgnoringWhitespace(sql)
    }

    @Table("person")
    data class Person(
        @Id
        @Column("id")
        val id: Long = 0,
        @Column("name")
        val name: String,
    )
}