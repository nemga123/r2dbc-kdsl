package io.nemga123.r2dbc.kotlin.querydsl.dsl

import org.springframework.data.domain.Pageable
import org.springframework.data.relational.core.sql.Condition
import org.springframework.data.relational.core.sql.LockMode
import org.springframework.data.relational.core.sql.Select

interface CountQueryDslBuilder {
    fun build(): Select
    fun from(dsl: FromDsl.() -> Unit): CountWhereBuilder

    interface CountWhereBuilder: SelectAndLockModeBuilder {
        fun where(dsl: CriteriaDsl.() -> Condition): SelectAndLockModeBuilder
    }

    interface SelectAndLockModeBuilder {
        fun lockMode(lockMode: LockMode): SelectAndLockModeBuilder
    }
}