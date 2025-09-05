package io.nemga123.r2dbc.kdsl.dsl

import io.nemga123.r2dbc.kdsl.annotation.R2dbcDsl
import org.springframework.data.mapping.PersistentPropertyAccessor
import org.springframework.data.r2dbc.convert.R2dbcConverter
import org.springframework.data.relational.core.mapping.RelationalMappingContext
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity
import org.springframework.data.relational.core.sql.Insert
import org.springframework.data.relational.core.sql.InsertBuilder

@R2dbcDsl
class InsertEntityQueryDsl<T: Any>(
    override val mappingContext: RelationalMappingContext,
    private val converter: R2dbcConverter,
    private val entity: T,
): DefaultExpressionDsl(mappingContext) {
    @Suppress("UNCHECKED_CAST")
    internal fun build(): Insert {
        val persistentEntity: RelationalPersistentEntity<T> = this.mappingContext.getRequiredPersistentEntity(entity::class.java) as RelationalPersistentEntity<T>
        val propertyAccessor: PersistentPropertyAccessor<T> = persistentEntity.getPropertyAccessor(entity)

        val initializedEntityPropertyAccessor: PersistentPropertyAccessor<T> = persistentEntity.getPropertyAccessor(entity)

        val table = table(entity::class)
        val insertBuilder: InsertBuilder.InsertIntoColumnsAndValuesWithBuild = Insert.builder()
            .into(table)

        for (column in persistentEntity) {
            if (column.isIdProperty && this.shouldSkipIdValue(propertyAccessor.getProperty(column))) {
                continue
            }

            insertBuilder.column(table.column(column.columnName))
                .value(exp(initializedEntityPropertyAccessor.getProperty(column)))
        }

        return insertBuilder.build()
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
}
