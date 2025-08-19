package io.nemga123.r2dbc.kotlin.querydsl.query

import io.nemga123.r2dbc.kotlin.querydsl.dsl.DeleteQueryDslBuilder
import org.springframework.data.relational.core.sql.Delete

interface DeleteQuery {
    suspend fun delete(dsl: DeleteQueryDslBuilder.() -> Delete): Long
}