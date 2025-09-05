package io.nemga123.r2dbc.kdsl.dsl

import org.springframework.data.relational.core.sql.Condition
import org.springframework.data.relational.core.sql.Delete
import org.springframework.data.relational.core.sql.Table

interface DeleteQueryDslBuilder {
    fun from(table: Table): DeleteFromAndWhereBuilder

    interface DeleteFromAndWhereBuilder: DeleteBuild {
        fun where(dsl: DefaultExpressionDsl.() -> Condition): DeleteFromAndWhereBuilder
    }

    interface DeleteBuild {
        fun build(): Delete
    }
}