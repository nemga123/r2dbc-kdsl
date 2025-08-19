package io.nemga123.r2dbc.kotlin.querydsl.dsl

import org.springframework.data.domain.Pageable
import org.springframework.data.relational.core.sql.Condition
import org.springframework.data.relational.core.sql.LockMode
import org.springframework.data.relational.core.sql.Select

interface SelectQueryDslBuilder {
    fun select(dsl: ProjectionDsl.() -> Unit): SelectAndFromBuilder
    fun build(): Select

    interface SelectAndFromBuilder {
        fun from(dsl: FromDsl.() -> Unit): SelectWhereBuilder
    }

    interface SelectWhereBuilder: SelectAndLimitOffsetBuilder, SelectWhereAndOrderByBuilder, SelectAndLockModeBuilder {
        fun where(dsl: CriteriaDsl.() -> Condition): SelectWhereAndOrderByBuilder
    }

    interface SelectWhereAndOrderByBuilder: SelectAndLockModeBuilder {
        fun orderBy(dsl: OrderByDsl.() -> Unit): SelectAndLimitOffsetBuilder
        fun page(page: Pageable): SelectAndLockModeBuilder
    }

    interface SelectAndLimitOffsetBuilder: SelectAndLockModeBuilder {
        fun limit(limit: Long): SelectAndLimitOffsetBuilder
        fun offset(offset: Long): SelectAndLimitOffsetBuilder
    }

    interface SelectAndLockModeBuilder {
        fun lockMode(lockMode: LockMode): SelectAndLockModeBuilder
    }
}