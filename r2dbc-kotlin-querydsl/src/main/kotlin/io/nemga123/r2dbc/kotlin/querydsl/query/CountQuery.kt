package io.nemga123.r2dbc.kotlin.querydsl.query

import io.nemga123.r2dbc.kotlin.querydsl.dsl.CountQueryDsl

interface CountQuery {
    suspend fun count(dsl: CountQueryDsl.() -> Unit): Long
}