package io.nemga123.r2dbc.kdsl.query

import io.nemga123.r2dbc.kdsl.dsl.DeleteQueryDslBuilder
import org.springframework.data.relational.core.sql.Delete

interface DeleteQuery {
    suspend fun delete(dsl: DeleteQueryDslBuilder.() -> Delete): Long
}