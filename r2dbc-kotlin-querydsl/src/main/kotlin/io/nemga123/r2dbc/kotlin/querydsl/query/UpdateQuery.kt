package io.nemga123.r2dbc.kotlin.querydsl.query

import io.nemga123.r2dbc.kotlin.querydsl.dsl.UpdateQueryDsl
import kotlin.reflect.KClass

interface UpdateQuery {
    suspend fun <T: Any> update(dsl: UpdateQueryDsl<T>.() -> Unit): Long
}