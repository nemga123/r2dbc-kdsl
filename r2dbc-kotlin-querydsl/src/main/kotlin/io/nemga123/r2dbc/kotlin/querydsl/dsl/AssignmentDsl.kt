package io.nemga123.r2dbc.kotlin.querydsl.dsl

import io.nemga123.r2dbc.kotlin.querydsl.annotation.R2dbcDsl
import org.springframework.data.r2dbc.core.DefaultReactiveDataAccessStrategy
import org.springframework.data.r2dbc.core.ReactiveDataAccessStrategy
import org.springframework.data.relational.core.mapping.RelationalMappingContext
import org.springframework.data.relational.core.sql.AssignValue
import org.springframework.data.relational.core.sql.Assignment
import org.springframework.data.relational.core.sql.Expression
import org.springframework.data.relational.core.sql.SQL
import kotlin.reflect.KProperty1

@R2dbcDsl
class AssignmentDsl<T: Any>(
    override val mappingContext: RelationalMappingContext,
): DefaultExpressionDsl(mappingContext) {
    private val assignmentList: MutableList<Assignment> = mutableListOf()

    fun <V: Any?> set(col: KProperty1<T, V>, value: V) {
        val column = path(col)
        val exp = exp(value)

        this.assignmentList.add(AssignValue.create(column, exp))
    }

    fun setExp(col: KProperty1<T, *>, exp: Expression) {
        val column = path(col)
        this.assignmentList.add(AssignValue.create(column, exp))
    }

    internal fun build(): List<Assignment> = assignmentList
}
