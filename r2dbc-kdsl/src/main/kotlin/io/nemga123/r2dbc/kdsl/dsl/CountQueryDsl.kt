package io.nemga123.r2dbc.kdsl.dsl

import io.nemga123.r2dbc.kdsl.annotation.R2dbcDsl
import org.springframework.data.relational.core.mapping.RelationalMappingContext
import org.springframework.data.relational.core.sql.Condition
import org.springframework.data.relational.core.sql.Expressions
import org.springframework.data.relational.core.sql.LockMode
import org.springframework.data.relational.core.sql.Select
import org.springframework.data.relational.core.sql.SelectBuilder
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoin

@R2dbcDsl
class CountQueryDsl (
    override val mappingContext: RelationalMappingContext,
): DefaultExpressionDsl(mappingContext),
    CountQueryDslBuilder,
    CountQueryDslBuilder.CountWhereBuilder,
    CountQueryDslBuilder.SelectAndLockModeBuilder,
    CountQueryDslBuilder.CountBuild {
    private val from: FromDsl =
        FromDsl(mappingContext)
    private var where: Condition? = null
    private var lockMode: LockMode? = null

    override fun from(dsl: FromDsl.() -> Unit): CountQueryDslBuilder.CountWhereBuilder {
        this.from.apply(dsl)
        return this
    }


    override fun where(dsl: DefaultExpressionDsl.() -> Condition): CountQueryDslBuilder.SelectAndLockModeBuilder {
        this.where = DefaultExpressionDsl(mappingContext).run(dsl)
        return this
    }


    override fun lockMode(lockMode: LockMode): CountQueryDslBuilder.SelectAndLockModeBuilder {
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

        where?.let { selectFromAndJoinBuilder.where(it) }

        lockMode?.let { selectFromAndJoinBuilder.lock(lockMode!!) }

        return selectFromAndJoinBuilder.build()
    }
}
