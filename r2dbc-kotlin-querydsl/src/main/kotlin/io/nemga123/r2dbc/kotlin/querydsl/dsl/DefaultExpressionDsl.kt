package io.nemga123.r2dbc.kotlin.querydsl.dsl

import io.nemga123.r2dbc.kotlin.querydsl.support.PropertyUtils
import org.springframework.data.relational.core.mapping.RelationalMappingContext
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity
import org.springframework.data.relational.core.sql.BooleanLiteral
import org.springframework.data.relational.core.sql.Column
import org.springframework.data.relational.core.sql.Expression
import org.springframework.data.relational.core.sql.Functions
import org.springframework.data.relational.core.sql.InlineQuery
import org.springframework.data.relational.core.sql.NumericLiteral
import org.springframework.data.relational.core.sql.SQL
import org.springframework.data.relational.core.sql.Select
import org.springframework.data.relational.core.sql.SimpleFunction
import org.springframework.data.relational.core.sql.SqlIdentifier
import org.springframework.data.relational.core.sql.StringLiteral
import org.springframework.data.relational.core.sql.Table
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

open class DefaultExpressionDsl(
    open val mappingContext: RelationalMappingContext,
) {
    fun <T: Any, V: Any?> Table.path(column: KProperty1<T, V>): Column {
        return this.column(getColumnName(column))
    }


    fun count(vararg expression: Expression): SimpleFunction {
        return Functions.count(*expression)
    }

    fun greatest(vararg expression: Expression): SimpleFunction {
        return Functions.greatest(*expression)
    }

    fun least(vararg expression: Expression): SimpleFunction {
        return Functions.least(*expression)
    }

    fun lower(expression: Expression): SimpleFunction {
        return Functions.lower(expression)
    }

    fun upper(expression: Expression): SimpleFunction {
        return Functions.upper(expression)
    }

    fun exp(value: Any?): Expression {
        return when(value) {
            is Number -> number(value)
            is String -> string(value)
            is Boolean -> bool(value)
            else -> SQL.literalOf(value)
        }
    }

    fun number(number: Number): NumericLiteral {
        return SQL.literalOf(number)
    }

    fun string(string: String): StringLiteral {
        return SQL.literalOf(string)
    }

    fun bool(boolean: Boolean): BooleanLiteral {
        return SQL.literalOf(boolean)
    }

    open fun <T: Any> table(clazz: KClass<T>): Table {
        val tableName = getTableName(clazz)
        return Table.create(tableName)
    }

    fun subquery(alias: String, dsl: SelectQueryDslBuilder.() -> Select): InlineQuery {
        val select = SelectQueryDsl(mappingContext).run(dsl)
        return InlineQuery.create(select, alias)
    }


    fun <T : Any, V: Any?> getColumnName(property: KProperty1<T, V>): SqlIdentifier {
        val entity: RelationalPersistentEntity<*> = getRequiredEntity(PropertyUtils.getOwner(property))

        return entity.getPersistentProperty(property.name)!!.columnName
    }

    private fun <T : Any> getTableName(entityClass: KClass<T>): SqlIdentifier {
        return getRequiredEntity(entityClass).qualifiedTableName
    }

    private fun <T: Any> getTableNameOrEmpty(entityClass: KClass<T>): SqlIdentifier {
        val entity = this.mappingContext.getPersistentEntity(entityClass.java)
        return entity?.qualifiedTableName ?: SqlIdentifier.EMPTY
    }

    private fun <T: Any> getRequiredEntity(entityClass: KClass<T>): RelationalPersistentEntity<*> {
        return this.mappingContext.getRequiredPersistentEntity(entityClass.java)
    }

    protected fun <T: Any> getRequiredEntity(entity: T): RelationalPersistentEntity<T> {
        return this.mappingContext.getRequiredPersistentEntity(entity::class.java) as RelationalPersistentEntity<T>
    }
}
