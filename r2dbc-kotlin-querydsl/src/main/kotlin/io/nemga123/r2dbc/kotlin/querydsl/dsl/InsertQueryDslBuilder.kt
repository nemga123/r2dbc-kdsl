package io.nemga123.r2dbc.kotlin.querydsl.dsl

import org.springframework.data.relational.core.sql.Insert
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface InsertQueryDslBuilder<T: Any> {
    fun into(intoClazz: KClass<T>): InsertValuesDslBuilder<T>
    fun build(): Insert

    interface InsertValuesDslBuilder<T: Any> {
        fun <V: Any?> set(col: KProperty1<T, V>, value: V): InsertValuesDslBuilder<T>
    }
}