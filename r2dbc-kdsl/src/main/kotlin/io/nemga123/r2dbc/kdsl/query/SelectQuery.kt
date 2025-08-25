package io.nemga123.r2dbc.kdsl.query

import io.nemga123.r2dbc.kdsl.dsl.SelectQueryDsl
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import java.util.function.BiFunction
import org.springframework.data.relational.core.sql.Select
import kotlin.reflect.KClass

interface SelectQuery {
    suspend fun <T : Any> selectAll(retType: KClass<T>, dsl: SelectQueryDsl.() -> Select): List<T>
    suspend fun <T : Any> selectAll(rowMapper: (Row, RowMetadata) -> T, dsl: SelectQueryDsl.() -> Select): List<T>
    suspend fun <T : Any> selectSingle(retType: KClass<T>, dsl: SelectQueryDsl.() -> Select): T?
    suspend fun <T : Any> selectSingle(rowMapper: BiFunction<Row, RowMetadata, T>, dsl: SelectQueryDsl.() -> Select): T?
}