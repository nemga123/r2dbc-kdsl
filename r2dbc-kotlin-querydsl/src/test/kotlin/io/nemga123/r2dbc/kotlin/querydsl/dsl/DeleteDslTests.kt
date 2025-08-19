package io.nemga123.r2dbc.kotlin.querydsl.dsl

import io.nemga123.r2dbc.kotlin.querydsl.dsl.UpdateDslTests.Person
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
import org.springframework.data.relational.core.sql.Insert
import org.springframework.data.relational.core.sql.render.SqlRenderer

import org.assertj.core.api.Assertions.*
import org.springframework.data.r2dbc.dialect.MySqlDialect
import org.springframework.data.r2dbc.dialect.PostgresDialect
import org.springframework.data.relational.core.sql.Delete

/**
 * Unit tests for {@link DeleteQueryDsl}.
 */
class DeleteDslTests {
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
    fun `simple_delete_test`() {
        val delete: Delete = DeleteQueryDsl(mappingContext)
            .run {
                from(Person::class)
                    .build()
            }

        val query = sqlRenderer.render(delete)
        val sql = "DELETE FROM person"

        assertThat(query).isEqualToIgnoringWhitespace(sql)
    }

    @Test
    fun `simple_delete_with_condition_test`() {
        val delete: Delete = DeleteQueryDsl(mappingContext).run {
            from(Person::class)
                .where {
                    table(Person::class).path(Person::id).isEqualTo(number(1))
                }
                .build()
        }

        val query = sqlRenderer.render(delete)
        val sql = "DELETE FROM person WHERE person.id = 1"

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