package io.nemga123.r2dbc.kdsl.query

import io.nemga123.r2dbc.kotlin.querydsl.dsl.SelectQueryDslBuilder
import kotlin.reflect.KClass

interface SelectQuery {
    suspend fun <T : Any> selectAll(clazz: KClass<T>, dsl: SelectQueryDslBuilder.() -> Unit): List<T>
    suspend fun <T : Any> selectSingle(clazz: KClass<T>, dsl: SelectQueryDslBuilder.() -> Unit): T
    suspend fun <T : Any> selectSingleOrNull(retType: KClass<T>, dsl: SelectQueryDslBuilder.() -> Unit): T?
}