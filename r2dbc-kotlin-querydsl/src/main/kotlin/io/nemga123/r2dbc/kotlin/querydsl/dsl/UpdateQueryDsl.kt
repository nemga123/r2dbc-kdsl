package io.nemga123.r2dbc.kotlin.querydsl.dsl

import io.nemga123.r2dbc.kotlin.querydsl.annotation.R2dbcDsl
import org.springframework.data.r2dbc.core.DefaultReactiveDataAccessStrategy
import org.springframework.data.relational.core.mapping.RelationalMappingContext
import org.springframework.data.relational.core.sql.Update
import org.springframework.data.relational.core.sql.UpdateBuilder
import kotlin.reflect.KClass

@R2dbcDsl
class UpdateQueryDsl<T : Any>(
    override val mappingContext: RelationalMappingContext,
): DefaultExpressionDsl(mappingContext), UpdateQueryDslBuilder<T>, UpdateQueryDslBuilder.UpdateAndValuesBuilder<T>, UpdateQueryDslBuilder.UpdateAndWhereBuilder, UpdateQueryDslBuilder.BuildUpdate {
    private lateinit var tableClass: KClass<T>
    private val assignment: AssignmentDsl<T> = AssignmentDsl(mappingContext)
    private val where: CriteriaDsl = CriteriaDsl(mappingContext)

    override fun table(tableClazz: KClass<T>): UpdateQueryDslBuilder.UpdateAndValuesBuilder<T> {
        this.tableClass = tableClazz
        return this
    }

    override fun set(dsl: AssignmentDsl<T>.() -> Unit): UpdateQueryDslBuilder.UpdateAndWhereBuilder {
        assignment.apply(dsl)
        return this
    }

    override fun where(dsl: CriteriaDsl.() -> Unit): UpdateQueryDslBuilder.BuildUpdate {
        where.apply(dsl)
        return this
    }

    override fun build(): Update {
        val builder: UpdateBuilder.UpdateWhere = Update.builder()
            .table(super.table(tableClass))
            .set(assignment.build())

        where.build()?.let { builder.where(it) }

        return builder.build()
    }
}
