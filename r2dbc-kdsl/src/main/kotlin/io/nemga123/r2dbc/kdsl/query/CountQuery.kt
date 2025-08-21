package io.nemga123.r2dbc.kdsl.query

import io.nemga123.r2dbc.kdsl.dsl.CountQueryDslBuilder
import org.springframework.data.relational.core.sql.Select

interface CountQuery {
    suspend fun count(dsl: CountQueryDslBuilder.() -> Select): Long
}