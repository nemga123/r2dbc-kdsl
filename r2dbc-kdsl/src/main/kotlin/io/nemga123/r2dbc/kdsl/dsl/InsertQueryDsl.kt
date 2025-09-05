package io.nemga123.r2dbc.kdsl.dsl

import io.nemga123.r2dbc.kdsl.annotation.R2dbcDsl
import org.springframework.data.relational.core.sql.Insert
import org.springframework.data.relational.core.sql.SqlIdentifier
import org.springframework.data.relational.core.mapping.RelationalMappingContext
import org.springframework.data.relational.core.sql.Table
import kotlin.reflect.KClass

@R2dbcDsl
open class InsertQueryDsl(
    override val mappingContext: RelationalMappingContext,
): DefaultExpressionDsl(mappingContext), InsertQueryDslBuilder {
    override fun <T: Any> into(intoClazz: KClass<T>): InsertQueryDslBuilder.InsertValuesDslBuilder<T> {
        return InsertValueDsl(mappingContext, intoClazz)
    }

    private class InsertValueDsl<T: Any>(
        override val mappingContext: RelationalMappingContext,
        private val intoClazz: KClass<T>,
    ): InsertQueryDsl(mappingContext), InsertQueryDslBuilder.InsertValuesDslBuilder<T> {
        private val table: Table = table(intoClazz)
        private val assignment: AssignmentDsl<T> = AssignmentDsl(mappingContext, intoClazz)

        override fun assign(dsl: AssignmentDsl<T>.() -> Unit): InsertQueryDslBuilder.InsertBuild {
            assignment.apply(dsl)
            return InsertBuildDsl(mappingContext, this.assignment.build().toMutableMap(), table)
        }

        private class InsertBuildDsl(
            override val mappingContext: RelationalMappingContext,
            private val assignMap: MutableMap<SqlIdentifier, Any?>,
            private val table: Table,
        ) : DefaultExpressionDsl(mappingContext), InsertQueryDslBuilder.InsertBuild {
            override fun build(): Insert {
                val builder = Insert.builder()
                    .into(this.table)

                for (entry in assignMap) {
                    val exp = exp(entry.value)
                    builder.column(table.column(entry.key))
                        .value(exp)
                }

                return builder.build()
            }
        }
    }
}
