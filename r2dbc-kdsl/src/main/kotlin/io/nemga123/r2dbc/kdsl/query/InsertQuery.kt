package io.nemga123.r2dbc.kdsl.query

import io.nemga123.r2dbc.kdsl.dsl.InsertQueryDslBuilder
import org.springframework.data.relational.core.sql.Insert

interface InsertQuery {
    suspend fun insert(dsl: InsertQueryDslBuilder.() -> Insert): Long
}