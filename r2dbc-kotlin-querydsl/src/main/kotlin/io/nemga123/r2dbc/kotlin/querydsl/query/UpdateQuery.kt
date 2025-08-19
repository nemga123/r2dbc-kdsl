package io.nemga123.r2dbc.kotlin.querydsl.query

import io.nemga123.r2dbc.kotlin.querydsl.dsl.UpdateQueryDsl
import org.springframework.data.relational.core.sql.Update
import kotlin.reflect.KClass

interface UpdateQuery {
    suspend fun update(dsl: UpdateQueryDsl.() -> Update): Long
}