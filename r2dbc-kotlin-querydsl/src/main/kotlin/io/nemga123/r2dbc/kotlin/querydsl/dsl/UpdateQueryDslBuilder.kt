package io.nemga123.r2dbc.kotlin.querydsl.dsl

import org.springframework.data.relational.core.sql.LockMode
import org.springframework.data.relational.core.sql.Select
import org.springframework.data.relational.core.sql.Update
import kotlin.reflect.KClass

interface UpdateQueryDslBuilder<T: Any> {
    fun table(tableClazz: KClass<T>): UpdateAndValuesBuilder<T>

    interface UpdateAndValuesBuilder<T: Any> {
        fun set(dsl: AssignmentDsl<T>.() -> Unit): UpdateAndWhereBuilder
    }

    interface UpdateAndWhereBuilder: BuildUpdate {
        fun where(dsl: CriteriaDsl.() -> Unit): BuildUpdate
    }

    interface BuildUpdate {
        fun build(): Update
    }
}