package io.nemga123.r2dbc.kotlin.querydsl.dsl

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

/**
 * Unit tests for {@link UpdateQueryDsl}.
 */
class UpdateDslTests {
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
    fun `simple_update_test`() {
        val updateQueryDslBuilder: UpdateQueryDslBuilder = UpdateQueryDsl(mappingContext)

        val update = updateQueryDslBuilder.run {
            from(Person::class)
            .assign {
                set(Person::id, 1)
                set(Person::name, "James")
            }.build()
        }
        val query = sqlRenderer.render(update)
        val sql = "UPDATE person SET id = 1, name = 'James'"

        assertThat(query).isEqualToIgnoringWhitespace(sql)
    }

    @Test
    fun `simple_update_with_or_criteria_test`() {
        val updateQueryDslBuilder: UpdateQueryDslBuilder = UpdateQueryDsl(mappingContext)

        val update = updateQueryDslBuilder.run {
            from(Person::class)
                .assign {
                    set(Person::name, "James")
                }
                .where {
                    or(table(Person::class).path(Person::name).isEqualTo(string("Nick")), table(Person::class).path(Person::name).isEqualTo(string("Michael")))
                }.build()
        }
        val query = sqlRenderer.render(update)
        val sql = "UPDATE person SET name = 'James' WHERE (person.name = 'Nick' OR person.name = 'Michael')"

        assertThat(query).isEqualToIgnoringWhitespace(sql)
    }

    @Test
    fun `simple_update_with_and_criteria_test`() {
        val updateQueryDslBuilder: UpdateQueryDslBuilder = UpdateQueryDsl(mappingContext)

        val update = updateQueryDslBuilder.run {
            from(Person::class)
                .assign {
                    set(Person::name, "James")
                }
                .where {
                    and(table(Person::class).path(Person::name).isEqualTo(string("Nick")), table(Person::class).path(Person::name).isEqualTo(string("Michael")))
                }.build()
        }
        val query = sqlRenderer.render(update)
        val sql = "UPDATE person SET name = 'James' WHERE (person.name = 'Nick' AND person.name = 'Michael')"

        assertThat(query).isEqualToIgnoringWhitespace(sql)
    }

    @Test
    fun `simple_update_with_complicated_criteria_test`() {
        val updateQueryDslBuilder: UpdateQueryDslBuilder = UpdateQueryDsl(mappingContext)

        val update = updateQueryDslBuilder.run {
            from(Person::class)
                .assign {
                    set(Person::name, "James")
                }
                .where {
                    or(
                        and(
                            table(Person::class).path(Person::name).isEqualTo(string("Nick")),
                            table(Person::class).path(Person::name).isEqualTo(string("Michael"))
                        ),
                        and(
                            table(Person::class).path(Person::name).isEqualTo(string("Ben")),
                            table(Person::class).path(Person::name).isEqualTo(string("Ann"))
                        ),
                    )
                }.build()
        }
        val query = sqlRenderer.render(update)
        val sql = "UPDATE person SET name = 'James' WHERE ((person.name = 'Nick' AND person.name = 'Michael') OR (person.name = 'Ben' AND person.name = 'Ann'))"

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