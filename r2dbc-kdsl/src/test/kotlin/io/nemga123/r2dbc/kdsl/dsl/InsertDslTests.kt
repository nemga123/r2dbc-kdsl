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
import org.springframework.data.relational.core.sql.Insert
import org.springframework.data.relational.core.sql.render.SqlRenderer

import org.assertj.core.api.Assertions.*
import org.springframework.data.r2dbc.dialect.MySqlDialect
import org.springframework.data.r2dbc.dialect.PostgresDialect

/**
 * Unit tests for {@link InsertQueryDsl, @link InsertEntityQueryDsl}.
 */
class InsertDslTests {
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
    fun `simple_assignment_insert_test`() {
        val insert: Insert = InsertQueryDsl(mappingContext)
            .run {
                into(Person::class)
                    .set<Long>(Person::id, 10)
                    .set<String>(Person::name, "James")
                    .build()
            }

        val query = sqlRenderer.render(insert)
        val sql = "INSERT INTO person (id, name) VALUES(10, 'James')"

        assertThat(query).isEqualToIgnoringWhitespace(sql)
    }

    @Test
    fun `simple_entity_insert_test`() {
        val entity = Person(id = 10L, name = "James")
        val insert: Insert = InsertEntityQueryDsl<Person>(mappingContext, converter, entity).build()

        val query = sqlRenderer.render(insert)
        val sql = "INSERT INTO person (id, name) VALUES(10, 'James')"

        assertThat(query).isEqualToIgnoringWhitespace(sql)
    }

    /**
     * When id column is set 0 or null, generate SQL without id column assignment.
     */
    @Test
    fun `simple_entity_insert_with_auto_increment_key_test`() {
        val entity = Person(id = 0L, name = "James")
        val insert: Insert = InsertEntityQueryDsl<Person>(mappingContext, converter, entity).build()

        val query = sqlRenderer.render(insert)
        val sql = "INSERT INTO person (name) VALUES('James')"

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