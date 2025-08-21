package io.nemga123.r2dbc.kdsl.dsl

import org.springframework.data.domain.Pageable
import org.springframework.data.relational.core.sql.Condition
import org.springframework.data.relational.core.sql.LockMode
import org.springframework.data.relational.core.sql.Select

interface CountQueryDslBuilder {
    fun from(dsl: FromDsl.() -> Unit): CountWhereBuilder

    interface CountWhereBuilder: SelectAndLockModeBuilder, CountBuild {
        fun where(dsl: CriteriaDsl.() -> Condition): SelectAndLockModeBuilder
    }

    interface SelectAndLockModeBuilder: CountBuild {
        fun lockMode(lockMode: LockMode): SelectAndLockModeBuilder
    }

    interface CountBuild {
        fun build(): Select
    }
}