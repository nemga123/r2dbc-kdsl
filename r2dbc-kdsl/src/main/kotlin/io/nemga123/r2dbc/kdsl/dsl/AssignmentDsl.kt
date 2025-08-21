package io.nemga123.r2dbc.kdsl.dsl

import io.nemga123.r2dbc.kdsl.annotation.R2dbcDsl
import org.springframework.data.r2dbc.core.DefaultReactiveDataAccessStrategy
import org.springframework.data.r2dbc.core.ReactiveDataAccessStrategy
import org.springframework.data.relational.core.mapping.RelationalMappingContext
import org.springframework.data.relational.core.sql.AssignValue
import org.springframework.data.relational.core.sql.Assignment
import org.springframework.data.relational.core.sql.Expression
import org.springframework.data.relational.core.sql.SQL
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

@io.nemga123.r2dbc.kdsl.annotation.R2dbcDsl
class AssignmentDsl<T: Any>(
    override val mappingContext: RelationalMappingContext,
    private val clazz: KClass<T>,
): io.nemga123.r2dbc.kdsl.dsl.DefaultExpressionDsl(mappingContext) {
    private val assignmentList: MutableList<Assignment> = mutableListOf()
    private val table = table(clazz)
    fun <V: Any?> set(col: KProperty1<T, V>, value: V) {
        val column = table.path(col)
        val exp = exp(value)

        this.assignmentList.add(AssignValue.create(column, exp))
    }

    fun setExp(col: KProperty1<T, *>, exp: Expression) {
        val column = table.path(col)
        this.assignmentList.add(AssignValue.create(column, exp))
    }

    internal fun build(): List<Assignment> = assignmentList
}
