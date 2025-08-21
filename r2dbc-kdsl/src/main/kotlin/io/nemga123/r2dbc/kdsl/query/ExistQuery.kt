package io.nemga123.r2dbc.kdsl.query

import io.nemga123.r2dbc.kotlin.querydsl.dsl.SelectQueryDslBuilder

interface ExistQuery {
    suspend fun exist(dsl: SelectQueryDslBuilder.() -> Unit): Boolean
}