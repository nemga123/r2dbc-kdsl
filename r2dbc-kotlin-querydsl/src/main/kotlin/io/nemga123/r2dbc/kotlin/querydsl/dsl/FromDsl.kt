package io.nemga123.r2dbc.kotlin.querydsl.dsl

import io.nemga123.r2dbc.kotlin.querydsl.annotation.R2dbcDsl
import org.springframework.data.relational.core.mapping.RelationalMappingContext
import org.springframework.data.relational.core.sql.Condition
import org.springframework.data.relational.core.sql.Join
import org.springframework.data.relational.core.sql.TableLike
import kotlin.reflect.KClass

@R2dbcDsl
class FromDsl(
    override val mappingContext: RelationalMappingContext,
): DefaultExpressionDsl(mappingContext) {
    private var from: TableLike? = null
    private val joins: MutableList<DefaultJoin> = mutableListOf()

    fun <T: Any> from(clazz: KClass<T>) {
        from(table(clazz))
    }

    fun from(tableLike: TableLike) {
        if (from != null) {
            throw RuntimeException("ALREADY_FROM")
        }
        this.from = tableLike
    }

    fun <T: Any> leftJoin(clazz: KClass<T>, condition: Condition){
        this.leftJoin(table(clazz), condition)
    }

    fun leftJoin(table: TableLike, condition: Condition){
        this.joins.add(
            DefaultJoinBuilder(table, Join.JoinType.LEFT_OUTER_JOIN).on(condition)
        )
    }

    fun <T: Any> rightJoin(clazz: KClass<T>, condition: Condition){
        this.rightJoin(table(clazz), condition)
    }

    fun rightJoin(table: TableLike, condition: Condition) {
        this.joins.add(
            DefaultJoinBuilder(table, Join.JoinType.RIGHT_OUTER_JOIN).on(condition)
        )
    }

    fun <T: Any> join(clazz: KClass<T>, condition: Condition){
        this.join(table(clazz), condition)
    }

    fun join(table: TableLike, condition: Condition) {
        this.joins.add(
            DefaultJoinBuilder(table, Join.JoinType.JOIN).on(condition)
        )
    }

    /**
     * Delegation builder to construct JOINs.
     */
    class DefaultJoinBuilder(
        private val table: TableLike,
        private val joinType: Join.JoinType,
    ) {
        fun on(condition: Condition): DefaultJoin {
            return DefaultJoin(table, joinType, condition)
        }
    }

    data class DefaultJoin(
        val table: TableLike,
        val joinType: Join.JoinType,
        val on: Condition,
    )

    internal fun buildFrom(): TableLike? = from

    internal fun buildJoin(): List<DefaultJoin> = joins
}
