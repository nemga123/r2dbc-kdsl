package io.nemga123.r2dbc.kdsl.client

import io.nemga123.r2dbc.kdsl.dsl.CountQueryDsl
import io.nemga123.r2dbc.kdsl.dsl.CountQueryDslBuilder
import io.nemga123.r2dbc.kdsl.dsl.DeleteQueryDsl
import io.nemga123.r2dbc.kdsl.dsl.DeleteQueryDslBuilder
import io.nemga123.r2dbc.kdsl.dsl.InsertEntityQueryDsl
import io.nemga123.r2dbc.kdsl.dsl.InsertQueryDsl
import io.nemga123.r2dbc.kdsl.dsl.InsertQueryDslBuilder
import io.nemga123.r2dbc.kdsl.dsl.SelectQueryDsl
import io.nemga123.r2dbc.kdsl.dsl.SelectQueryDslBuilder
import io.nemga123.r2dbc.kdsl.dsl.UpdateQueryDsl
import io.nemga123.r2dbc.kdsl.dsl.UpdateQueryDslBuilder
import io.nemga123.r2dbc.kdsl.query.CountQuery
import io.nemga123.r2dbc.kdsl.query.DeleteQuery
import io.nemga123.r2dbc.kdsl.query.ExistQuery
import io.nemga123.r2dbc.kdsl.query.InsertByEntityQuery
import io.nemga123.r2dbc.kdsl.query.InsertQuery
import io.nemga123.r2dbc.kdsl.query.SelectQuery
import io.nemga123.r2dbc.kdsl.query.UpdateQuery
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
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
import org.springframework.data.relational.core.dialect.RenderContextFactory
import org.springframework.data.relational.core.mapping.RelationalMappingContext
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty
import org.springframework.data.relational.core.sql.Delete
import org.springframework.data.relational.core.sql.Insert
import org.springframework.data.relational.core.sql.Select
import org.springframework.data.relational.core.sql.SqlIdentifier
import org.springframework.data.relational.core.sql.Update
import org.springframework.data.relational.core.sql.render.SqlRenderer
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec
import org.springframework.r2dbc.core.RowsFetchSpec
import org.springframework.r2dbc.core.QueryOperation
import org.springframework.r2dbc.core.awaitRowsUpdated
import kotlin.jvm.optionals.getOrElse
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.jvmErasure

class R2dbcKdslClient (
    private val dialect: R2dbcDialect,
    private val databaseClient: DatabaseClient,
): InsertQuery, InsertByEntityQuery, SelectQuery, ExistQuery, CountQuery, UpdateQuery, DeleteQuery {
    private val converter: R2dbcConverter = createConverter()
    private val renderContextFactory: RenderContextFactory = RenderContextFactory(dialect)
    private val sqlRenderer: SqlRenderer = SqlRenderer.create(renderContextFactory.createRenderContext())
    private val mappingContext: RelationalMappingContext = converter.mappingContext as RelationalMappingContext

    private fun createConverter(): R2dbcConverter {
        val customConversions = R2dbcCustomConversions.of(dialect, emptyList<Any>())

        val context = R2dbcMappingContext()
        context.setSimpleTypeHolder(customConversions.simpleTypeHolder)

        return MappingR2dbcConverter(context, customConversions)
    }


    override suspend fun count(dsl: CountQueryDslBuilder.() -> Select): Long {
        val select = CountQueryDsl(mappingContext).run(dsl)
        val result = databaseClient.sql(QueryOperation { sqlRenderer.render(select) })
            .map { r, _ -> r.get(0, Long::class.java) ?: 0L }
            .first()
            .awaitSingle()

        return result
    }

    override suspend fun exist(dsl: SelectQueryDslBuilder.() -> Select): Boolean {
        val select = SelectQueryDsl(mappingContext).run(dsl)
        val result = databaseClient.sql(QueryOperation { sqlRenderer.render(select) })
            .map { r, _ -> r }
            .first()
            .hasElement()
            .awaitSingle()
        return result
    }

    override suspend fun <T : Any> selectAll(retType: KClass<T>, dsl: SelectQueryDsl.() -> Select): List<T> {
        val select = SelectQueryDsl(mappingContext).run(dsl)
        val result = resultProjection(this.getRowMapper(retType), databaseClient.sql(QueryOperation { sqlRenderer.render(select) }), RowsFetchSpec<T>::all)

        return result.asFlow().toList()
    }
    
    override suspend fun <T : Any> selectAll(rowMapper: (Row, RowMetadata) -> T, dsl: SelectQueryDsl.() -> Select): List<T> {
        val select = SelectQueryDsl(mappingContext).run(dsl)
        val result = resultProjection(rowMapper, databaseClient.sql(QueryOperation { sqlRenderer.render(select) }), RowsFetchSpec<T>::all)

        return result.asFlow().toList()
    }

    override suspend fun <T : Any> selectSingle(retType: KClass<T>, dsl: SelectQueryDsl.() -> Select): T? {
        val select = SelectQueryDsl(mappingContext).run(dsl)
        val result = resultProjection(this.getRowMapper(retType), databaseClient.sql(QueryOperation { sqlRenderer.render(select) }), RowsFetchSpec<T>::one)

        return result.awaitSingleOrNull()
    }

    override suspend fun <T : Any> selectSingle(rowMapper: BiFunction<Row, RowMetadata, T>, dsl: SelectQueryDsl.() -> Select): T? {
        val select = SelectQueryDsl(mappingContext).run(dsl)
        val result = resultProjection(rowMapper, databaseClient.sql(QueryOperation { sqlRenderer.render(select) }), RowsFetchSpec<T>::one)

        return result.awaitSingleOrNull()
    }

    private fun <T : Any, P: Publisher<T>> resultProjection(rowMapper: BiFunction<Row, RowMetadata, T>, executeSpec: GenericExecuteSpec, resultHandler: (RowsFetchSpec<T>) -> P): P {
        val result: P = resultHandler.invoke(getRowFetchSpec(rowMapper, executeSpec))
        return result
    }

    private fun <T : Any> getRowFetchSpec(rowMapper: BiFunction<Row, RowMetadata, T>, executeSpec: GenericExecuteSpec): RowsFetchSpec<T> {
        return executeSpec.map(rowMapper);
    }

    private fun <T : Any> getRowMapper(retType: KClass<T>): BiFunction<Row, RowMetadata, T> {
        val isSimpleType = converter.isSimpleType(retType.java)
        val rowMapper: BiFunction<Row, RowMetadata, T> = if (converter is AbstractRelationalConverter
            && converter.conversions.hasCustomReadTarget(Row::class.java, retType.java)
        ) {
            val conversionService = converter.getConversionService()
            BiFunction { row: Row, _: RowMetadata ->
                conversionService.convert(
                    row,
                    retType.java
                ) as T
            }
        } else if (isSimpleType) {
            BiFunction { row: Row, rowMetadata: RowMetadata ->
                if (rowMetadata.columnMetadatas.size > 1) {
                    throw RuntimeException("INVALID QUERY FOR SIMPLE TYPE")
                }

                return@BiFunction try {
                    retType.cast(row.get(0))
                } catch (ex: Exception) {
                    throw RuntimeException("CANNOT CAST ROW TYPE ${rowMetadata.getColumnMetadata(0).type} TO ${retType.createType()}")
                }
            }
        } else {
            BiFunction { row: Row, rowMetadata: RowMetadata ->
                val constructor = retType.constructors.stream()
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

    override suspend fun update(dsl: UpdateQueryDslBuilder.() -> Update): Long {
        val update: Update = UpdateQueryDsl(mappingContext).run(dsl)
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

    override suspend fun insert(dsl: InsertQueryDslBuilder.() -> Insert): Long {
        val insert = InsertQueryDsl(mappingContext).run(dsl)

        val result = databaseClient.sql(QueryOperation { sqlRenderer.render(insert) })
            .fetch()
            .awaitRowsUpdated()

        return result
    }

    override suspend fun delete(dsl: DeleteQueryDslBuilder.() -> Delete): Long {
        val delete =  DeleteQueryDsl(mappingContext).run(dsl)
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

