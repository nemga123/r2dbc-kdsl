package io.nemga123.r2dbc.kdsl.dsl

import io.nemga123.r2dbc.kdsl.annotation.R2dbcDsl
import java.util.*
import org.springframework.data.relational.core.mapping.RelationalMappingContext
import org.springframework.data.relational.core.sql.AssignValue
import org.springframework.data.relational.core.sql.Assignment
import org.springframework.data.relational.core.sql.SqlIdentifier
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

@R2dbcDsl
class AssignmentDsl<T: Any>(
    override val mappingContext: RelationalMappingContext,
    private val clazz: KClass<T>,
): DefaultExpressionDsl(mappingContext) {
    private val table = table(clazz)
    private val assignMap: MutableMap<SqlIdentifier, Any?> = TreeMap<SqlIdentifier, Any?>(Comparator.comparing { sqlIdentifier: SqlIdentifier -> sqlIdentifier.reference })
    fun <V: Any?> set(col: KProperty1<T, V>, value: V) {
        val column = table.path(col)
        val exp = exp(value)

        this.assignMap[column.referenceName] = exp
    }

    internal fun build(): Map<SqlIdentifier, Any?> = assignMap
}
