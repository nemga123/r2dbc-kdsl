package io.nemga123.r2dbc.kotlin.querydsl.dsl

import io.nemga123.r2dbc.kotlin.querydsl.annotation.R2dbcDsl
import org.springframework.data.relational.core.mapping.RelationalMappingContext
import org.springframework.data.relational.core.sql.*

@R2dbcDsl
class ProjectionDsl(
    override val mappingContext: RelationalMappingContext,
): DefaultExpressionDsl(mappingContext) {
    private val selectList: MutableList<Expression> = mutableListOf()

    fun select(vararg exp: Expression) {
        this.selectList.addAll(exp)
    }

    internal fun build(): List<Expression> = selectList
}
