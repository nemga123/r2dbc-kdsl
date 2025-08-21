package io.nemga123.r2dbc.kotlin.querydsl.dsl

import org.springframework.data.relational.core.sql.Condition
import org.springframework.data.relational.core.sql.Delete
import org.springframework.data.relational.core.sql.Table
import kotlin.reflect.KClass

interface DeleteQueryDslBuilder {
    fun from(table: Table): DeleteFromAndWhereBuilder

    interface DeleteFromAndWhereBuilder: DeleteBuild {
        fun where(dsl: CriteriaDsl.() -> Condition): DeleteFromAndWhereBuilder
    }

    interface DeleteBuild {
        fun build(): Delete
    }
}