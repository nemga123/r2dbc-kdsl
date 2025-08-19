package io.nemga123.r2dbc.kotlin.querydsl.dsl

import org.springframework.data.r2dbc.core.DefaultReactiveDataAccessStrategy
import org.springframework.data.relational.core.mapping.RelationalMappingContext
import org.springframework.data.relational.core.sql.Condition
import org.springframework.data.relational.core.sql.Delete
import org.springframework.data.relational.core.sql.DeleteBuilder
import org.springframework.data.relational.core.sql.Table
import org.springframework.util.Assert
import kotlin.reflect.KClass

class DeleteQueryDsl(
    override val mappingContext: RelationalMappingContext,
): DefaultExpressionDsl(mappingContext), DeleteQueryDslBuilder, DeleteQueryDslBuilder.DeleteFromAndWhereBuilder {
    private lateinit var table: Table
    private var where: Condition? = null

    override fun <T: Any> from(clazz: KClass<T>): DeleteQueryDslBuilder.DeleteFromAndWhereBuilder {
        table = table(clazz)
        return this
    }

    override fun where(dsl: CriteriaDsl.() -> Condition): DeleteQueryDslBuilder.DeleteFromAndWhereBuilder {
        this.where = CriteriaDsl(mappingContext).run(dsl)
        return this
    }

    override fun build(): Delete {
        val builder: DeleteBuilder.DeleteWhere = Delete.builder()
            .from(table)

        where?.let { builder.where(it) }

        return builder.build()
    }

}