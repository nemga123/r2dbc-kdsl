package io.nemga123.r2dbc.kotlin.querydsl.query

import io.nemga123.r2dbc.kotlin.querydsl.dsl.SelectQueryDsl

interface ExistQuery {
    suspend fun exist(dsl: SelectQueryDsl.() -> Unit): Boolean
}