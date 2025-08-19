package io.nemga123.r2dbc.kotlin.querydsl.dsl

import org.springframework.data.relational.core.sql.Condition
import org.springframework.data.relational.core.sql.Join
import org.springframework.data.relational.core.sql.TableLike
import kotlin.reflect.KClass

interface FromDslBuilder {
    fun from(tableLike: TableLike): FromWithJoinBuilder

    interface FromWithJoinBuilder : FromBuild {
        fun leftJoin(table: TableLike, condition: Condition): FromWithJoinBuilder
        fun rightJoin(table: TableLike, condition: Condition): FromWithJoinBuilder
        fun join(table: TableLike, condition: Condition): FromWithJoinBuilder
    }

    interface FromBuild {
        fun buildFrom(): TableLike
        fun buildJoin(): List<DefaultJoin>
    }

    data class DefaultJoin(
        val table: TableLike,
        val joinType: Join.JoinType,
        val on: Condition,
    )
}