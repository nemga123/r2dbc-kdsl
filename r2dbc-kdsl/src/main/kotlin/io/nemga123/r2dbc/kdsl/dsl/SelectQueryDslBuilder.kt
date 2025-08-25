package io.nemga123.r2dbc.kdsl.dsl

import org.springframework.data.domain.Pageable
import org.springframework.data.relational.core.sql.Condition
import org.springframework.data.relational.core.sql.Expression
import org.springframework.data.relational.core.sql.LockMode
import org.springframework.data.relational.core.sql.Select

interface SelectQueryDslBuilder {
    fun select(vararg select: Expression): SelectAndFromBuilder

    interface SelectAndFromBuilder {
        fun distinct(distinct: Boolean): SelectAndFromBuilder
        fun from(dsl: FromDslBuilder.() -> FromDslBuilder.FromBuild): SelectWhereBuilder
    }

    interface SelectWhereBuilder: SelectAndLimitOffsetBuilder, SelectWhereAndOrderByBuilder, SelectAndLockModeBuilder, SelectBuild {
        fun where(dsl: CriteriaDsl.() -> Condition): SelectWhereAndOrderByBuilder
    }

    interface SelectWhereAndOrderByBuilder: SelectAndLockModeBuilder, SelectBuild {
        fun orderBy(dsl: OrderByDsl.() -> Unit): SelectAndLimitOffsetBuilder
        fun page(page: Pageable): SelectAndLockModeBuilder
    }

    interface SelectAndLimitOffsetBuilder: SelectAndLockModeBuilder, SelectBuild {
        fun limit(limit: Long): SelectAndLimitOffsetBuilder
        fun offset(offset: Long): SelectAndLimitOffsetBuilder
    }

    interface SelectAndLockModeBuilder: SelectBuild {
        fun lockMode(lockMode: LockMode): SelectAndLockModeBuilder
    }

    interface SelectBuild {
        fun build(): Select
    }
}