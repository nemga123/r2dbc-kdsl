package io.nemga123.r2dbc.kotlin.querydsl.query

import io.nemga123.r2dbc.kotlin.querydsl.dsl.InsertQueryDsl
import io.nemga123.r2dbc.kotlin.querydsl.dsl.InsertQueryDslBuilder
import kotlin.reflect.KClass

interface InsertQuery {
    suspend fun <T : Any> insert(dsl: InsertQueryDslBuilder<T>.() -> Unit): T
}