package io.nemga123.r2dbc.kdsl.dsl

import io.nemga123.r2dbc.kdsl.annotation.R2dbcDsl
import org.springframework.data.r2dbc.core.DefaultReactiveDataAccessStrategy
import org.springframework.data.relational.core.mapping.RelationalMappingContext
import org.springframework.data.relational.core.sql.Condition
import org.springframework.data.relational.core.sql.Expressions
import org.springframework.data.relational.core.sql.LockMode
import org.springframework.data.relational.core.sql.Select
import org.springframework.data.relational.core.sql.SelectBuilder
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoin

@io.nemga123.r2dbc.kdsl.annotation.R2dbcDsl
class CountQueryDsl (
    override val mappingContext: RelationalMappingContext,
): io.nemga123.r2dbc.kdsl.dsl.DefaultExpressionDsl(mappingContext), io.nemga123.r2dbc.kdsl.dsl.CountQueryDslBuilder,
    io.nemga123.r2dbc.kdsl.dsl.CountQueryDslBuilder.CountWhereBuilder,
    _root_ide_package_.io.nemga123.r2dbc.kdsl.dsl.CountQueryDslBuilder.SelectAndLockModeBuilder,
    _root_ide_package_.io.nemga123.r2dbc.kdsl.dsl.CountQueryDslBuilder.CountBuild {
    private val from: _root_ide_package_.io.nemga123.r2dbc.kdsl.dsl.FromDsl =
        _root_ide_package_.io.nemga123.r2dbc.kdsl.dsl.FromDsl(mappingContext)
    private var where: Condition? = null
    private var lockMode: LockMode? = null

    override fun from(dsl: _root_ide_package_.io.nemga123.r2dbc.kdsl.dsl.FromDsl.() -> Unit): _root_ide_package_.io.nemga123.r2dbc.kdsl.dsl.CountQueryDslBuilder.CountWhereBuilder {
        this.from.apply(dsl)
        return this
    }


    override fun where(dsl: _root_ide_package_.io.nemga123.r2dbc.kdsl.dsl.CriteriaDsl.() -> Condition): _root_ide_package_.io.nemga123.r2dbc.kdsl.dsl.CountQueryDslBuilder.SelectAndLockModeBuilder {
        this.where = _root_ide_package_.io.nemga123.r2dbc.kdsl.dsl.CriteriaDsl(mappingContext).run(dsl)
        return this
    }


    override fun lockMode(lockMode: LockMode): _root_ide_package_.io.nemga123.r2dbc.kdsl.dsl.CountQueryDslBuilder.SelectAndLockModeBuilder {
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
