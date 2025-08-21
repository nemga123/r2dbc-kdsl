package io.nemga123.r2dbc.kdsl.query

import io.nemga123.r2dbc.kotlin.querydsl.dsl.CountQueryDslBuilder

interface CountQuery {
    suspend fun count(dsl: CountQueryDslBuilder.() -> Unit): Long
}