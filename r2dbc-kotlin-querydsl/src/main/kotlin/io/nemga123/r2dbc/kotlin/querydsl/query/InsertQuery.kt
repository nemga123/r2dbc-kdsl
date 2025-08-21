package io.nemga123.r2dbc.kotlin.querydsl.query

import io.nemga123.r2dbc.kotlin.querydsl.dsl.InsertQueryDslBuilder
import org.springframework.data.relational.core.sql.Insert

interface InsertQuery {
    suspend fun <T : Any> insert(dsl: InsertQueryDslBuilder.() -> Insert): Long
}