package io.nemga123.r2dbc.kdsl.dsl

import io.nemga123.r2dbc.kdsl.annotation.R2dbcDsl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.relational.core.mapping.RelationalMappingContext
import org.springframework.data.relational.core.sql.Condition
import org.springframework.data.relational.core.sql.Expressions
import org.springframework.data.relational.core.sql.LockMode
import org.springframework.data.relational.core.sql.Select
import org.springframework.data.relational.core.sql.SelectBuilder

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
    SelectQueryDslBuilder.SelectBuild
{
    private val from: FromDsl = FromDsl(mappingContext)
    private var limit: Long = -1L
    private var offset: Long = -1L
    private val select: ProjectionDsl = ProjectionDsl(mappingContext)
    private var distinct: Boolean = false
    private var where: Condition? = null
    private val orderBy: OrderByDsl = OrderByDsl(mappingContext)
    private var lockMode: LockMode? = null

    override fun select(dsl: ProjectionDsl.() -> Unit): SelectQueryDslBuilder.SelectAndFromBuilder {
        this.select.apply(dsl)
        return this
    }

    override fun distinct(distinct: Boolean): SelectQueryDsl {
        this.distinct = distinct
        return this
    }

    override fun from(dsl: FromDsl.() -> Unit): SelectQueryDslBuilder.SelectWhereBuilder {
        this.from.apply(dsl)
        return this
    }


    override fun where(dsl: CriteriaDsl.() -> Condition): SelectQueryDslBuilder.SelectWhereAndOrderByBuilder {
        this.where = CriteriaDsl(mappingContext).run(dsl)
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


    override fun lockMode(lockMode: LockMode): SelectQueryDslBuilder.SelectAndLockModeBuilder {
        this.lockMode = lockMode
        return this
    }

    override fun build(): Select {
        var builder: SelectBuilder.SelectAndFrom = Select.builder()
            .select(select.build())

        if (distinct) {
            builder = builder.distinct()
        }

        var selectFromAndJoinBuilder: SelectBuilder.SelectJoin = builder.from(from.buildFrom())

        for (join in from.buildJoin()) {
            selectFromAndJoinBuilder = selectFromAndJoinBuilder.join(join.table, join.joinType).on(join.on)
        }

        where?.let { (selectFromAndJoinBuilder as SelectBuilder.SelectWhere).where(it) }

        lockMode?.let { (selectFromAndJoinBuilder as SelectBuilder.SelectLock).lock(lockMode!!) }

        if (limit > 0) {
            (selectFromAndJoinBuilder as SelectBuilder.SelectLimitOffset).limit(limit)
        }

        if (offset >= 0) {
            (selectFromAndJoinBuilder as SelectBuilder.SelectLimitOffset).offset(offset)
        }

        if (!orderBy.isEmpty()) {
            (selectFromAndJoinBuilder as SelectBuilder.SelectOrdered).orderBy(orderBy.build())
        }

        return selectFromAndJoinBuilder.build()
    }


}
