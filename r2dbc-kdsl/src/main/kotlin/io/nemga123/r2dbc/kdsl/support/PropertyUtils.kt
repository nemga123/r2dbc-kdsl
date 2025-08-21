package io.nemga123.r2dbc.kdsl.support

import kotlin.jvm.internal.CallableReference
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField

object PropertyUtils {
    fun <T : Any> getOwner(property: KProperty1<T, *>): KClass<T> {
        @Suppress("UNCHECKED_CAST")
        return (property as CallableReference).owner as KClass<T>
    }
}
