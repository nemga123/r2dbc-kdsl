package io.nemga123.r2dbc.kotlin.querydsl.query

interface InsertByEntityQuery {
    suspend fun <T : Any> insert(entity: T): T
}