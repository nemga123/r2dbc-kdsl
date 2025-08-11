package io.nemga123.r2dbc.kotlin.querydsl.query

import io.nemga123.r2dbc.kotlin.querydsl.dsl.CountQueryDsl
import io.nemga123.r2dbc.kotlin.querydsl.dsl.DeleteQueryDsl
import io.nemga123.r2dbc.kotlin.querydsl.dsl.InsertEntityQueryDsl
import io.nemga123.r2dbc.kotlin.querydsl.dsl.InsertQueryDsl
import io.nemga123.r2dbc.kotlin.querydsl.dsl.SelectQueryDsl
import io.nemga123.r2dbc.kotlin.querydsl.dsl.UpdateQueryDsl
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import java.util.*
import java.util.function.BiFunction
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.reactivestreams.Publisher
import org.springframework.data.r2dbc.convert.MappingR2dbcConverter
import org.springframework.data.r2dbc.convert.R2dbcConverter
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.dialect.R2dbcDialect
import org.springframework.data.r2dbc.mapping.R2dbcMappingContext
import org.springframework.data.relational.core.conversion.AbstractRelationalConverter
import org.springframework.data.relational.core.mapping.RelationalMappingContext
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty
import org.springframework.data.relational.core.sql.SqlIdentifier
import org.springframework.data.relational.core.sql.render.SqlRenderer
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec
import org.springframework.r2dbc.core.QueryOperation
import org.springframework.r2dbc.core.RowsFetchSpec
import org.springframework.r2dbc.core.awaitRowsUpdated
import org.springframework.util.Assert
import kotlin.jvm.optionals.getOrElse
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.jvmErasure

class R2dbcKotlinQueryDsl(
    private val dialect: R2dbcDialect,
    private val databaseClient: DatabaseClient,
    private val sqlRenderer: SqlRenderer,
    private val mappingContext: RelationalMappingContext,
): InsertQuery, InsertByEntityQuery, SelectQuery, ExistQuery, CountQuery, UpdateQuery, DeleteQuery {
    private val converter: R2dbcConverter = createConverter()

    private fun createConverter(): R2dbcConverter {
        val customConversions = R2dbcCustomConversions.of(dialect, emptyList<Any>())

        val context = R2dbcMappingContext()
        context.setSimpleTypeHolder(customConversions.simpleTypeHolder)

        return MappingR2dbcConverter(context, customConversions)
    }


    override suspend fun count(dsl: CountQueryDsl.() -> Unit): Long {
        val selectBuilder = CountQueryDsl(mappingContext).apply(dsl)
        val select = selectBuilder.build()
        val result = databaseClient.sql(QueryOperation { sqlRenderer.render(select) })
            .map { r, _ -> r.get(0, Long::class.java) ?: 0L }
            .first()
            .awaitSingle()

        return result
    }

    override suspend fun exist(dsl: SelectQueryDsl.() -> Unit): Boolean {
        val selectBuilder = SelectQueryDsl(mappingContext).apply(dsl)
        val select = selectBuilder.build()
        val result = databaseClient.sql(QueryOperation { sqlRenderer.render(select) })
            .map { r, _ -> r }
            .first()
            .hasElement()
            .awaitSingle()
        return result
    }

    override suspend fun <T : Any> selectAll(retType: KClass<T>, dsl: SelectQueryDsl.() -> Unit): List<T> {
        Assert.notNull(retType, "Entity class must not be null")

        val selectBuilder = SelectQueryDsl(mappingContext).apply(dsl)
        val select = selectBuilder.build()
        val result = resultProjection(retType, databaseClient.sql(QueryOperation { sqlRenderer.render(select) }), RowsFetchSpec<T>::all)

        return result.asFlow().toList()
    }

    override suspend fun <T : Any> selectSingle(retType: KClass<T>, dsl: SelectQueryDsl.() -> Unit): T {
        Assert.notNull(retType, "Entity class must not be null")

        val selectBuilder = SelectQueryDsl(mappingContext).apply(dsl)
        val select = selectBuilder.build()
        val result = resultProjection(retType, databaseClient.sql(QueryOperation { sqlRenderer.render(select) }), RowsFetchSpec<T>::one)

        return result.awaitSingle()
    }

    override suspend fun <T : Any> selectSingleOrNull(retType: KClass<T>, dsl: SelectQueryDsl.() -> Unit): T? {
        Assert.notNull(retType, "Entity class must not be null")

        val selectBuilder = SelectQueryDsl(mappingContext).apply(dsl)
        val select = selectBuilder.build()
        val result = resultProjection(retType, databaseClient.sql(QueryOperation { sqlRenderer.render(select) }), RowsFetchSpec<T>::one)

        return result.awaitSingleOrNull()
    }

    private fun <T : Any, P: Publisher<T>> resultProjection(clazz: KClass<T>, executeSpec: GenericExecuteSpec, resultHandler: (RowsFetchSpec<T>) -> P): P {
        val result: P = resultHandler.invoke(getRowFetchSpec(clazz, executeSpec))
        return result
    }

    private fun <T : Any> getRowFetchSpec(returnClass: KClass<T>, executeSpec: GenericExecuteSpec): RowsFetchSpec<T> {
        return executeSpec.map(getRowMapper(returnClass));
    }

    private fun <T : Any> getRowMapper(returnClass: KClass<T>): BiFunction<Row, RowMetadata, T> {
        val isSimpleType = converter.isSimpleType(returnClass.java)
        val rowMapper: BiFunction<Row, RowMetadata, T> = if (converter is AbstractRelationalConverter
            && converter.conversions.hasCustomReadTarget(Row::class.java, returnClass.java)
        ) {
            val conversionService = converter.getConversionService()
            BiFunction { row: Row, _: RowMetadata ->
                conversionService.convert(
                    row,
                    returnClass.java
                ) as T
            }
        } else if (isSimpleType) {
            BiFunction { row: Row, rowMetadata: RowMetadata ->
                if (rowMetadata.columnMetadatas.size > 1) {
                    throw RuntimeException("INVALID QUERY FOR SIMPLE TYPE")
                }

                return@BiFunction try {
                    returnClass.cast(row.get(0))
                } catch (ex: Exception) {
                    throw RuntimeException("CANNOT CAST ROW TYPE ${rowMetadata.getColumnMetadata(0).type} TO ${returnClass.createType()}")
                }
            }
        } else {
            BiFunction { row: Row, rowMetadata: RowMetadata ->
                val constructor = returnClass.constructors.stream()
                    .filter { it.parameters.size == rowMetadata.columnMetadatas.size }
                    .findFirst().getOrElse { throw RuntimeException("INVALID CONSTRUCTOR") }

                val parameterArray = Array<Any?>(constructor.parameters.size) { null }
                for(i in parameterArray.indices) {
                    parameterArray[i] = row.get(i, constructor.parameters[i].type.jvmErasure.java)
                }
                constructor.call(*parameterArray)
            }
        }

        return rowMapper
    }

    override suspend fun <T: Any> update(dsl: UpdateQueryDsl<T>.() -> Unit): Long {
        val updateBuilder = UpdateQueryDsl<T>(mappingContext).apply(dsl)
        val update = updateBuilder.build()
        val result = databaseClient.sql(QueryOperation { sqlRenderer.render(update) })
            .fetch()
            .awaitRowsUpdated()
        return result
    }

    override suspend fun <T: Any> insert(entity: T): T {
        val insertEntityQuery = InsertEntityQueryDsl(mappingContext, converter, entity)
        val persistentEntity: RelationalPersistentEntity<*> = this.mappingContext.getRequiredPersistentEntity(entity.javaClass)
        val result = databaseClient.sql(QueryOperation { sqlRenderer.render(insertEntityQuery.build()) })
            .filter { statement ->
                val identifierColumns = this.getIdentifierColumns(persistentEntity)
                if(identifierColumns.isEmpty()) {
                    statement.returnGeneratedValues()
                }

                statement.returnGeneratedValues(this.dialect.renderForGeneratedValues(identifierColumns[0]))
            }
            .map(converter.populateIdIfNecessary(entity))
            .all()
            .last(entity)
            .awaitSingle()

        return result
    }

    override suspend fun <T : Any> insert(dsl: InsertQueryDsl<T>.() -> Unit): T {
        val insertQueryBuilder = InsertQueryDsl<T>(mappingContext, converter).apply(dsl)
        val insert = insertQueryBuilder.build()
        val insertedEntity: T = insertQueryBuilder.buildEntity()
        val persistentEntity: RelationalPersistentEntity<*> = this.mappingContext.getRequiredPersistentEntity(insertedEntity.javaClass)

        val result = databaseClient.sql(QueryOperation { sqlRenderer.render(insert) })
            .filter { statement ->
                val identifierColumns = this.getIdentifierColumns(persistentEntity)
                if(identifierColumns.isEmpty()) {
                    statement.returnGeneratedValues()
                }

                statement.returnGeneratedValues(dialect.renderForGeneratedValues(identifierColumns[0]))
            }
            .map(converter.populateIdIfNecessary(insertedEntity))
            .all()
            .last(insertedEntity)
            .awaitSingle()

        return result
    }

    override suspend fun delete(dsl: DeleteQueryDsl.() -> Unit): Long {
        val deleteBuilder =  DeleteQueryDsl(mappingContext).apply(dsl)
        val delete = deleteBuilder.build()
        val result = databaseClient.sql(QueryOperation { sqlRenderer.render(delete) })
            .fetch()
            .rowsUpdated()
            .defaultIfEmpty(0L)
            .awaitSingle()

        return result
    }

    private fun getIdentifierColumns(entity: RelationalPersistentEntity<*>): List<SqlIdentifier> {
        val columnNames: MutableList<SqlIdentifier> = ArrayList()
        for (property in entity) {
            if (property.isIdProperty) {
                columnNames.add(property.columnName)
            }
        }

        return entity.filter(RelationalPersistentProperty::isIdProperty).map { it.columnName }.toList()
    }
}

