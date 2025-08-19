package io.nemga123.r2dbc.kotlin.querydsl.dsl

import org.springframework.data.relational.core.sql.Condition
import org.springframework.data.relational.core.sql.Delete
import kotlin.reflect.KClass

interface DeleteQueryDslBuilder {
    fun <T: Any> from(intoClazz: KClass<T>): DeleteFromAndWhereBuilder
    fun build(): Delete

    interface DeleteFromAndWhereBuilder {
        fun where(dsl: CriteriaDsl.() -> Condition): DeleteFromAndWhereBuilder
    }
}