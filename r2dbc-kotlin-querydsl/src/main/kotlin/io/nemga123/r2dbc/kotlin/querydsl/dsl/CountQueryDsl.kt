package io.nemga123.r2dbc.kotlin.querydsl.dsl

import io.nemga123.r2dbc.kotlin.querydsl.annotation.R2dbcDsl
import org.springframework.data.r2dbc.core.DefaultReactiveDataAccessStrategy
import org.springframework.data.relational.core.mapping.RelationalMappingContext
import org.springframework.data.relational.core.sql.Expressions
import org.springframework.data.relational.core.sql.LockMode
import org.springframework.data.relational.core.sql.Select
import org.springframework.data.relational.core.sql.SelectBuilder
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoin

@R2dbcDsl
class CountQueryDsl (
    override val mappingContext: RelationalMappingContext,
): DefaultExpressionDsl(mappingContext), SelectQueryDslBuilder.CountAndFromBuilder,
    SelectQueryDslBuilder.CountWhereBuilder, SelectQueryDslBuilder.SelectAndLockModeBuilder, SelectQueryDslBuilder.BuildSelect {
    private val from: FromDsl = FromDsl(mappingContext)
    private val where: CriteriaDsl = CriteriaDsl(mappingContext)
    private var lockMode: LockMode? = null

    override fun from(dsl: FromDsl.() -> Unit): SelectQueryDslBuilder.CountWhereBuilder {
        this.from.apply(dsl)
        return this
    }


    override fun where(dsl: CriteriaDsl.() -> Unit): SelectQueryDslBuilder.SelectAndLockModeBuilder {
        where.apply(dsl)
        return this
    }


    override fun lockMode(lockMode: LockMode): SelectQueryDslBuilder.SelectAndLockModeBuilder {
        this.lockMode = lockMode
        return this
    }

    override fun build(): Select {
        var builder: SelectBuilder.SelectAndFrom = Select.builder()
            .select(count(Expressions.asterisk()))

        val selectFromAndJoinBuilder: SelectFromAndJoin = builder.from(from.buildFrom())

        for (join in from.buildJoin()) {
            selectFromAndJoinBuilder.join(join.table, join.joinType).on(join.on)
        }

        where.build()?.let { selectFromAndJoinBuilder.where(it) }

        lockMode?.let { selectFromAndJoinBuilder.lock(lockMode!!) }

        return selectFromAndJoinBuilder.build()
    }
}
