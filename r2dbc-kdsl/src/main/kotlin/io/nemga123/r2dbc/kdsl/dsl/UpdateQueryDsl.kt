package io.nemga123.r2dbc.kdsl.dsl

import io.nemga123.r2dbc.kdsl.annotation.R2dbcDsl
import org.springframework.data.relational.core.mapping.RelationalMappingContext
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.sql.AssignValue
import org.springframework.data.relational.core.sql.Assignment
import org.springframework.data.relational.core.sql.Assignments
import org.springframework.data.relational.core.sql.Condition
import org.springframework.data.relational.core.sql.SqlIdentifier
import org.springframework.data.relational.core.sql.Table
import org.springframework.data.relational.core.sql.Update
import org.springframework.data.relational.core.sql.UpdateBuilder
import org.springframework.util.Assert
import kotlin.contracts.contract
import kotlin.reflect.KClass

@R2dbcDsl
class UpdateQueryDsl private constructor(
    override val mappingContext: RelationalMappingContext,
    private val assignments: List<Assignment>,
    private val where: Condition?,
    private val table: Table?
): DefaultExpressionDsl(mappingContext), UpdateQueryDslBuilder, UpdateQueryDslBuilder.UpdateBuild, UpdateQueryDslBuilder.UpdateAndWhereBuilder {
    constructor(
        context: RelationalMappingContext,
    ): this(
        context,
        emptyList(),
        null,
        null
    )

    override fun <T: Any> from(tableClazz: KClass<T>): UpdateQueryDslBuilder.UpdateAndValuesBuilder<T> {
        return UpdateQueryAndValuesDsl(mappingContext, tableClazz)
    }

    class UpdateQueryAndValuesDsl<T: Any>(
        override val mappingContext: RelationalMappingContext,
        private val tableClazz: KClass<T>,
    ) : DefaultExpressionDsl(mappingContext), UpdateQueryDslBuilder.UpdateAndValuesBuilder<T>{
        private val table: Table = table(tableClazz)
        private val assignment: AssignmentDsl<T> = AssignmentDsl(mappingContext, tableClazz)

        override fun assign(dsl: AssignmentDsl<T>.() -> Unit): UpdateQueryDslBuilder.UpdateAndWhereBuilder {
            assignment.apply(dsl)
            return UpdateQueryDsl(mappingContext, this.getAssignmentList(),null,  table(tableClazz))
        }

        private fun getAssignmentList(): List<Assignment> {
            val assignmentMap = assignment.build()
            return assignmentMap.map { AssignValue.create(table.column(it.key), exp(it.value)) }
        }
    }

    override fun where(dsl: DefaultExpressionDsl.() -> Condition): UpdateQueryDslBuilder.UpdateBuild {
        return UpdateQueryDsl(mappingContext, assignments, DefaultExpressionDsl(mappingContext).run(dsl), this.table)
    }

    override fun build(): Update {
        Assert.notNull(table, "From table is null")

        val builder: UpdateBuilder.UpdateWhere = Update.builder()
            .table(this.table!!)
            .set(assignments)

        where?.let { builder.where(it) }

        return builder.build()
    }

}
