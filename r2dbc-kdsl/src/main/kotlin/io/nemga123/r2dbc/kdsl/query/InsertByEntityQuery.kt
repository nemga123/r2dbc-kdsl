package io.nemga123.r2dbc.kdsl.query

interface InsertByEntityQuery {
    suspend fun <T : Any> insert(entity: T): T
}