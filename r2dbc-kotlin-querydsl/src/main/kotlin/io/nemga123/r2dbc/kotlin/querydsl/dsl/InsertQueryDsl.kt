package io.nemga123.r2dbc.kotlin.querydsl.dsl

import io.nemga123.r2dbc.kotlin.querydsl.annotation.R2dbcDsl
import org.springframework.data.mapping.PersistentPropertyAccessor
import org.springframework.data.r2dbc.core.DefaultReactiveDataAccessStrategy
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity
import org.springframework.data.relational.core.sql.Insert
import org.springframework.data.relational.core.sql.SqlIdentifier
import java.util.*
import org.springframework.data.r2dbc.convert.R2dbcConverter
import org.springframework.data.relational.core.mapping.RelationalMappingContext
import org.springframework.data.relational.core.sql.Table
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.createInstance

@R2dbcDsl
class InsertQueryDsl<T: Any>(
    override val mappingContext: RelationalMappingContext,
    private val converter: R2dbcConverter
): DefaultExpressionDsl(mappingContext), InsertQueryDslBuilder<T>, InsertQueryDslBuilder.InsertValuesDslBuilder<T> {
    private lateinit var intoClazz: KClass<T>
    private lateinit var table: Table
    private val assignMap: MutableMap<SqlIdentifier, Any?> = TreeMap<SqlIdentifier, Any?>(Comparator.comparing { sqlIdentifier: SqlIdentifier -> sqlIdentifier.reference })
    lateinit var entity: T

    override fun into(intoClazz: KClass<T>): InsertQueryDslBuilder.InsertValuesDslBuilder<T> {
        this.intoClazz = intoClazz
        this.table = table(intoClazz)
        return this
    }

    override fun <V: Any?> set(col: KProperty1<T, V>, value: V): InsertQueryDslBuilder.InsertValuesDslBuilder<T> {
        val identifier = getColumnName(col)
        assignMap[identifier] = value
        return this
    }

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

    private fun <T: Any> setVersionIfNecessary(persistentEntity: RelationalPersistentEntity<T>) {
        val versionProperty = persistentEntity.versionProperty ?: return
        val versionPropertyType = versionProperty.type
        val version = if (versionPropertyType.isPrimitive) 1L else 0L
        assignMap[versionProperty.columnName] = version
    }

    internal fun buildEntity(): T {
        val entity: T = intoClazz.createInstance()
        val persistentEntity: RelationalPersistentEntity<T> =
            this.mappingContext.getRequiredPersistentEntity(intoClazz.java) as RelationalPersistentEntity<T>
        val propertyAccessor: PersistentPropertyAccessor<*> = persistentEntity.getPropertyAccessor(entity)
        for (property in persistentEntity) {
            if (assignMap.containsKey(property.columnName)) {
                propertyAccessor.setProperty(
                    property,
                    converter.conversionService.convert(assignMap[property.columnName], property.type)
                )
            }
        }

        return propertyAccessor.bean as T
    }
}
