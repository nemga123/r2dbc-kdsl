package io.nemga123.r2dbc.kdsl.dsl

import io.nemga123.r2dbc.kdsl.annotation.R2dbcDsl
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction
import org.springframework.data.relational.core.mapping.RelationalMappingContext
import org.springframework.data.relational.core.sql.Expression
import org.springframework.data.relational.core.sql.OrderByField

@io.nemga123.r2dbc.kdsl.annotation.R2dbcDsl
class OrderByDsl(
    override val mappingContext: RelationalMappingContext,
): DefaultExpressionDsl(mappingContext) {
    private val targetExpression: MutableList<OrderByField> = mutableListOf()

    fun asc(expression: Expression) {
        targetExpression.add(OrderByField.from(expression, Sort.Direction.ASC))
    }

    fun desc(expression: Expression) {
        targetExpression.add(OrderByField.from(expression, Sort.Direction.DESC))
    }

    fun from(expression: Expression, direction: Direction) {
        targetExpression.add(OrderByField.from(expression, direction))
    }

    internal fun build(): List<OrderByField> = targetExpression
    internal fun isEmpty(): Boolean = targetExpression.isEmpty()
}
