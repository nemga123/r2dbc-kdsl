package io.nemga123.r2dbc.kdsl.query

import io.nemga123.r2dbc.kdsl.dsl.SelectQueryDslBuilder
import org.springframework.data.relational.core.sql.Select
import kotlin.reflect.KClass

interface SelectQuery {
    suspend fun <T : Any> selectAll(clazz: KClass<T>, dsl: SelectQueryDslBuilder.() -> Select): List<T>
    suspend fun <T : Any> selectSingle(clazz: KClass<T>, dsl: SelectQueryDslBuilder.() -> Select): T
    suspend fun <T : Any> selectSingleOrNull(retType: KClass<T>, dsl: SelectQueryDslBuilder.() -> Select): T?
}