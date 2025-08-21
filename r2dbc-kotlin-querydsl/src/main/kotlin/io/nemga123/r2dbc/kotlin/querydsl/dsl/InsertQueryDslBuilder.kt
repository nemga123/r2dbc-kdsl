package io.nemga123.r2dbc.kotlin.querydsl.dsl

import org.springframework.data.relational.core.sql.Insert
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface InsertQueryDslBuilder {
    fun <T: Any> into(intoClazz: KClass<T>): InsertValuesDslBuilder<T>

    interface InsertValuesDslBuilder<T: Any>: InsertBuild {
        fun <V: Any?> set(col: KProperty1<T, V>, value: V): InsertValuesDslBuilder<T>
    }

    interface InsertBuild {
        fun build(): Insert
    }
}