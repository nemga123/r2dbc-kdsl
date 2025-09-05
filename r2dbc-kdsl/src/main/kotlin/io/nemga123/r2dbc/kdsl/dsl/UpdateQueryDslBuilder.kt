package io.nemga123.r2dbc.kdsl.dsl

import org.springframework.data.relational.core.sql.Condition
import org.springframework.data.relational.core.sql.LockMode
import org.springframework.data.relational.core.sql.Select
import org.springframework.data.relational.core.sql.Update
import kotlin.reflect.KClass

interface UpdateQueryDslBuilder {
    fun <T: Any> from(tableClazz: KClass<T>): UpdateAndValuesBuilder<T>

    interface UpdateAndValuesBuilder<T: Any> {
        fun assign(dsl: AssignmentDsl<T>.() -> Unit): UpdateAndWhereBuilder
    }

    interface UpdateAndWhereBuilder: UpdateBuild {
        fun where(dsl: DefaultExpressionDsl.() -> Condition): UpdateBuild
    }

    interface UpdateBuild {
        fun build(): Update
    }
}