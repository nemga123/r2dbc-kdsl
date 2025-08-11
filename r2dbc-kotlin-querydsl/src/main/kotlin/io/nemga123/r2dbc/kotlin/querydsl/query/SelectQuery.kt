package io.nemga123.r2dbc.kotlin.querydsl.query

import io.nemga123.r2dbc.kotlin.querydsl.dsl.SelectQueryDsl
import kotlin.reflect.KClass

interface SelectQuery {
    suspend fun <T : Any> selectAll(clazz: KClass<T>, dsl: SelectQueryDsl.() -> Unit): List<T>
    suspend fun <T : Any> selectSingle(clazz: KClass<T>, dsl: SelectQueryDsl.() -> Unit): T
    suspend fun <T : Any> selectSingleOrNull(retType: KClass<T>, dsl: SelectQueryDsl.() -> Unit): T?
}