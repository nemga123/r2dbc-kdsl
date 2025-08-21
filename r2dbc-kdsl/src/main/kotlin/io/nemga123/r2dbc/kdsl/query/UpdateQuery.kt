package io.nemga123.r2dbc.kdsl.query

import io.nemga123.r2dbc.kotlin.querydsl.dsl.UpdateQueryDsl
import io.nemga123.r2dbc.kotlin.querydsl.dsl.UpdateQueryDslBuilder
import org.springframework.data.relational.core.sql.Update
import kotlin.reflect.KClass

interface UpdateQuery {
    suspend fun update(dsl: UpdateQueryDslBuilder.() -> Update): Long
}