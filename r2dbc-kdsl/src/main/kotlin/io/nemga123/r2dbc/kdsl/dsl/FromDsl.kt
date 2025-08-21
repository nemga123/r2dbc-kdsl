package io.nemga123.r2dbc.kdsl.dsl

import io.nemga123.r2dbc.kdsl.annotation.R2dbcDsl
import org.springframework.data.relational.core.mapping.RelationalMappingContext
import org.springframework.data.relational.core.sql.Condition
import org.springframework.data.relational.core.sql.Join
import org.springframework.data.relational.core.sql.TableLike

@R2dbcDsl
class FromDsl(
    override val mappingContext: RelationalMappingContext,
): DefaultExpressionDsl(mappingContext), FromDslBuilder, FromDslBuilder.FromWithJoinBuilder, FromDslBuilder.FromBuild {
    private var from: TableLike? = null
    private val joins: MutableList<FromDslBuilder.DefaultJoin> = mutableListOf()

    override fun from(tableLike: TableLike): FromDslBuilder.FromWithJoinBuilder {
        this.from = tableLike
        return this
    }

    override fun leftJoin(table: TableLike, condition: Condition): FromDslBuilder.FromWithJoinBuilder {
        this.joins.add(
            DefaultJoinBuilder(table, Join.JoinType.LEFT_OUTER_JOIN).on(condition)
        )
        return this
    }

    override fun rightJoin(table: TableLike, condition: Condition): FromDslBuilder.FromWithJoinBuilder {
        this.joins.add(
            DefaultJoinBuilder(table, Join.JoinType.RIGHT_OUTER_JOIN).on(condition)
        )
        return this
    }

    override fun join(table: TableLike, condition: Condition): FromDslBuilder.FromWithJoinBuilder {
        this.joins.add(
            DefaultJoinBuilder(table, Join.JoinType.JOIN).on(condition)
        )
        return this
    }

    /**
     * Delegation builder to construct JOINs.
     */
    class DefaultJoinBuilder(
        private val table: TableLike,
        private val joinType: Join.JoinType,
    ) {
        fun on(condition: Condition): FromDslBuilder.DefaultJoin {
            return FromDslBuilder.DefaultJoin(table, joinType, condition)
        }
    }

    override fun buildFrom(): TableLike = from!!

    override fun buildJoin(): List<FromDslBuilder.DefaultJoin> = joins
}
