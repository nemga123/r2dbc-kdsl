package io.nemga123.r2dbc.kdsl.dsl

import io.nemga123.r2dbc.kdsl.annotation.R2dbcDsl
import org.springframework.data.relational.core.mapping.RelationalMappingContext
import org.springframework.data.relational.core.sql.Condition
import org.springframework.data.relational.core.sql.Conditions

@R2dbcDsl
class CriteriaDsl(
    override val mappingContext: RelationalMappingContext,
): DefaultExpressionDsl(mappingContext) {
    fun and(vararg conditions: Condition): Condition {
        return conditions.reduce { acc, condition -> acc.and(condition) }.let { Conditions.nest(it) }
    }

    fun or(vararg conditions: Condition): Condition {
        return conditions.reduce { acc, condition -> acc.or(condition) }.let { Conditions.nest(it) }
    }
}
