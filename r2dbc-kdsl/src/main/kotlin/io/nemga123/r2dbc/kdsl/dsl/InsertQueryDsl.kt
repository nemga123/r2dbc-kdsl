package io.nemga123.r2dbc.kdsl.dsl

import io.nemga123.r2dbc.kdsl.annotation.R2dbcDsl
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity
import org.springframework.data.relational.core.sql.Insert
import org.springframework.data.relational.core.sql.SqlIdentifier
import java.util.*
import org.springframework.data.relational.core.mapping.RelationalMappingContext
import org.springframework.data.relational.core.sql.Table
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

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
        private val assignMap: MutableMap<SqlIdentifier, Any?> = TreeMap<SqlIdentifier, Any?>(Comparator.comparing { sqlIdentifier: SqlIdentifier -> sqlIdentifier.reference })

        override fun <V> set(col: KProperty1<T, V>, value: V): InsertQueryDslBuilder.InsertValuesDslBuilder<T> {
            val identifier = getColumnName(col)
            assignMap[identifier] = value
            return this
        }

        @Suppress("UNCHECKED_CAST")
        override fun build() : Insert {
            val persistentEntity: RelationalPersistentEntity<T> = this.mappingContext.getRequiredPersistentEntity(intoClazz.java) as RelationalPersistentEntity<T>
            this.setVersionIfNecessary(persistentEntity)
            this.potentiallyRemoveId(persistentEntity)

            val builder = Insert.builder()
                .into(this.table)

            for (entry in assignMap) {
                val exp = exp(entry.value)
                builder.column(table.column(entry.key))
                    .value(exp)
            }

            return builder.build()
        }

        private fun potentiallyRemoveId(persistentEntity: RelationalPersistentEntity<T>) {
            val idProperty = persistentEntity.idProperty
            if (idProperty != null) {
                val columnName = idProperty.columnName
                if (assignMap.containsKey(columnName) && this.shouldSkipIdValue(assignMap[columnName])) {
                    assignMap.remove(columnName)
                }
            }
        }

        private fun shouldSkipIdValue(idValue: Any?): Boolean {
            return if (idValue != null) {
                if (idValue is Number) {
                    idValue.toLong() == 0L
                } else {
                    false
                }
            } else {
                true
            }
        }

        fun <T: Any> setVersionIfNecessary(persistentEntity: RelationalPersistentEntity<T>) {
            val versionProperty = persistentEntity.versionProperty ?: return
            val versionPropertyType = versionProperty.type
            val version = if (versionPropertyType.isPrimitive) 1L else 0L
            assignMap[versionProperty.columnName] = version
        }
    }
}
