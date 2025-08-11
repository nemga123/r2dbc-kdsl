package io.nemga123.r2dbc.kotlin.querydsl.dsl

import io.nemga123.r2dbc.kotlin.querydsl.annotation.R2dbcDsl
import org.springframework.data.relational.core.mapping.RelationalMappingContext
import org.springframework.data.relational.core.sql.*

@R2dbcDsl
class ProjectionDsl(
    override val mappingContext: RelationalMappingContext,
): DefaultExpressionDsl(mappingContext) {
    private val selectList: MutableList<Expression> = mutableListOf()
    private var distinct: Boolean = false

    fun select(exp: Expression) {
        this.selectList.add(exp)
    }

    fun distinct(distinct: Boolean = true) {
        this.distinct = distinct
    }

    internal fun isDistinct(): Boolean = distinct
    internal fun build(): List<Expression> = selectList
}
