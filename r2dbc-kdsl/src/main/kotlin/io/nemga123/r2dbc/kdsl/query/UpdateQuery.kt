package io.nemga123.r2dbc.kdsl.query

import io.nemga123.r2dbc.kdsl.dsl.UpdateQueryDslBuilder
import org.springframework.data.relational.core.sql.Update

interface UpdateQuery {
    suspend fun update(dsl: UpdateQueryDslBuilder.() -> Update): Long
}