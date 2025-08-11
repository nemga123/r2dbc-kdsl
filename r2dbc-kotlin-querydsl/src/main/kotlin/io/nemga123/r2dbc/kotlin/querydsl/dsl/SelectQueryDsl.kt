package io.nemga123.r2dbc.kotlin.querydsl.dsl

import io.nemga123.r2dbc.kotlin.querydsl.annotation.R2dbcDsl
import io.nemga123.r2dbc.kotlin.querydsl.dsl.SelectQueryDslBuilder.SelectAndLockModeBuilder
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.DefaultReactiveDataAccessStrategy
import org.springframework.data.relational.core.mapping.RelationalMappingContext
import org.springframework.data.relational.core.sql.Expressions
import org.springframework.data.relational.core.sql.LockMode
import org.springframework.data.relational.core.sql.Select
import org.springframework.data.relational.core.sql.SelectBuilder
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoin

@R2dbcDsl
class SelectQueryDsl (
    override val mappingContext: RelationalMappingContext,
): DefaultExpressionDsl(mappingContext),
    SelectQueryDslBuilder,
    SelectQueryDslBuilder.SelectAndFromBuilder,
    SelectQueryDslBuilder.SelectWhereBuilder,
    SelectQueryDslBuilder.SelectWhereAndOrderByBuilder,
    SelectQueryDslBuilder.SelectAndLimitOffsetBuilder,
    SelectQueryDslBuilder.SelectAndLockModeBuilder,
    SelectQueryDslBuilder.BuildSelect
{
    private val from: FromDsl = FromDsl(mappingContext)
    private var limit: Long = -1L
    private var offset: Long = -1L
    private val select: ProjectionDsl = ProjectionDsl(mappingContext)
    private val where: CriteriaDsl = CriteriaDsl(mappingContext)
    private val orderBy: OrderByDsl = OrderByDsl(mappingContext)
    private var lockMode: LockMode? = null

    override fun select(dsl: ProjectionDsl.() -> Unit): SelectQueryDslBuilder.SelectAndFromBuilder {
        this.select.apply(dsl)
        return this
    }

    override fun from(dsl: FromDsl.() -> Unit): SelectQueryDslBuilder.SelectWhereBuilder {
        this.from.apply(dsl)
        return this
    }


    override fun where(dsl: CriteriaDsl.() -> Unit): SelectQueryDslBuilder.SelectWhereAndOrderByBuilder {
        where.apply(dsl)
        return this
    }

    override fun limit(limit: Long): SelectQueryDslBuilder.SelectAndLimitOffsetBuilder {
        this.limit = limit
        return this
    }

    override fun offset(offset: Long): SelectQueryDslBuilder.SelectAndLimitOffsetBuilder {
        this.offset = offset
        return this
    }

    override fun page(page: Pageable): SelectQueryDslBuilder.SelectAndLimitOffsetBuilder {
        if (page.isPaged) {
            this.limit = page.pageSize.toLong()
            this.offset = page.offset
            val sort : Sort= page.sort
            if (sort.isSorted) {
                this.orderBy.apply {
                    sort.forEach { s ->
                        from(Expressions.just(s.property), s.direction)
                    }
                }
            }
        }
        return this
    }

    override fun orderBy(dsl: OrderByDsl.() -> Unit): SelectQueryDslBuilder.SelectAndLimitOffsetBuilder {
        this.orderBy.apply(dsl)
        return this
    }


    override fun lockMode(lockMode: LockMode): SelectAndLockModeBuilder {
        this.lockMode = lockMode
        return this
    }

    override fun build(): Select {
        var builder: SelectBuilder.SelectAndFrom = Select.builder()
            .select(select.build())

        if (select.isDistinct()) {
            builder = builder.distinct()
        }

        val selectFromAndJoinBuilder: SelectFromAndJoin = builder.from(from.buildFrom())

        for (join in from.buildJoin()) {
            selectFromAndJoinBuilder.join(join.table, join.joinType).on(join.on)
        }

        where.build()?.let { selectFromAndJoinBuilder.where(it) }

        lockMode?.let { selectFromAndJoinBuilder.lock(lockMode!!) }

        if (limit > 0) {
            selectFromAndJoinBuilder.limit(limit)
        }

        if (offset >= 0) {
            selectFromAndJoinBuilder.offset(offset)
        }

        if (!orderBy.isEmpty()) {
            selectFromAndJoinBuilder.orderBy(orderBy.build())
        }

        return selectFromAndJoinBuilder.build()
    }


}
