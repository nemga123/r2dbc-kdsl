package io.nemga123.r2dbc.kdsl.dsl

import org.springframework.data.relational.core.sql.Insert
import kotlin.reflect.KClass

interface InsertQueryDslBuilder {
    fun <T: Any> into(intoClazz: KClass<T>): InsertValuesDslBuilder<T>

    interface InsertValuesDslBuilder<T: Any> {
        fun assign(dsl: AssignmentDsl<T>.() -> Unit): InsertBuild
    }

    interface InsertBuild {
        fun build(): Insert
    }
}