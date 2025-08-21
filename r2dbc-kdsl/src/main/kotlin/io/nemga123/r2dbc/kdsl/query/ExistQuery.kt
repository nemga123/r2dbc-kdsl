package io.nemga123.r2dbc.kdsl.query

import io.nemga123.r2dbc.kdsl.dsl.SelectQueryDslBuilder
import org.springframework.data.relational.core.sql.Select

interface ExistQuery {
    suspend fun exist(dsl: SelectQueryDslBuilder.() -> Select): Boolean
}