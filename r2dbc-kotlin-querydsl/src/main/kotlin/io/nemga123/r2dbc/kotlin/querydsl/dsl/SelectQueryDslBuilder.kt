package io.nemga123.r2dbc.kotlin.querydsl.dsl

import org.springframework.data.domain.Pageable
import org.springframework.data.relational.core.sql.LockMode
import org.springframework.data.relational.core.sql.Select

interface SelectQueryDslBuilder {
    fun select(dsl: ProjectionDsl.() -> Unit): SelectAndFromBuilder

    interface SelectAndFromBuilder {
        fun from(dsl: FromDsl.() -> Unit): SelectWhereBuilder
    }

    interface SelectWhereBuilder: BuildSelect, SelectAndLimitOffsetBuilder, SelectWhereAndOrderByBuilder, SelectAndLockModeBuilder {
        fun where(dsl: CriteriaDsl.() -> Unit): SelectWhereAndOrderByBuilder
    }

    interface SelectWhereAndOrderByBuilder: SelectAndLockModeBuilder, BuildSelect {
        fun orderBy(dsl: OrderByDsl.() -> Unit): SelectAndLimitOffsetBuilder
        fun page(page: Pageable): SelectAndLockModeBuilder
    }

    interface SelectAndLimitOffsetBuilder: BuildSelect, SelectAndLockModeBuilder {
        fun limit(limit: Long): SelectAndLimitOffsetBuilder
        fun offset(offset: Long): SelectAndLimitOffsetBuilder
    }

    interface SelectAndLockModeBuilder: BuildSelect {
        fun lockMode(lockMode: LockMode): SelectAndLockModeBuilder
    }

    interface CountAndFromBuilder {
        fun from(dsl: FromDsl.() -> Unit): CountWhereBuilder
    }

    interface CountWhereBuilder: BuildSelect, SelectAndLockModeBuilder {
        fun where(dsl: CriteriaDsl.() -> Unit): SelectAndLockModeBuilder
    }


    interface BuildSelect {
        fun build(): Select
    }
}