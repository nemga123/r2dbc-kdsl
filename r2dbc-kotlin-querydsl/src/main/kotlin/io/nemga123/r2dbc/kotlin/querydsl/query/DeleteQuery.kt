package io.nemga123.r2dbc.kotlin.querydsl.query

import io.nemga123.r2dbc.kotlin.querydsl.dsl.DeleteQueryDsl

interface DeleteQuery {
    suspend fun delete(dsl: DeleteQueryDsl.() -> Unit): Long
}