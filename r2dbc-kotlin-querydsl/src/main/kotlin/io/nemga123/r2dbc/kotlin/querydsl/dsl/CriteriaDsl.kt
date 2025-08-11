package io.nemga123.r2dbc.kotlin.querydsl.dsl

import io.nemga123.r2dbc.kotlin.querydsl.annotation.R2dbcDsl
import org.springframework.data.r2dbc.core.DefaultReactiveDataAccessStrategy
import org.springframework.data.relational.core.mapping.RelationalMappingContext
import org.springframework.data.relational.core.sql.Condition

@R2dbcDsl
class CriteriaDsl(
    override val mappingContext: RelationalMappingContext,
): DefaultExpressionDsl(mappingContext) {
    private var condition: Condition? = null

    fun and(vararg conditions: Condition?) {
        val mergedCondition = conditions.filterNotNull().reduce { acc, condition -> acc.and(condition) }
        mergeCondition(mergedCondition)
    }

    fun or(vararg conditions: Condition?) {
        val mergedCondition = conditions.filterNotNull().reduce { acc, condition -> acc.or(condition) }
        mergeCondition(mergedCondition)
    }

    private fun mergeCondition(condition: Condition) {
        if (this.condition == null) {
            this.condition = condition
        } else {
            this.condition!!.and(condition)
        }
    }

    internal fun build(): Condition? = condition
}
