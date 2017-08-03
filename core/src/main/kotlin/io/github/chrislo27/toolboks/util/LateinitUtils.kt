package io.github.chrislo27.toolboks.util

import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KProperty


fun Any.hasUninitializedLateinits(): Boolean {
    this::class.members.filter { it is KProperty && it.isLateinit }.forEach {
        it as KProperty
        try {
            it.call(this)
        } catch (e: InvocationTargetException) {
            return false
        }
    }

    return true
}

fun Any.getUninitializedLateinits(): List<KProperty<*>> {
    return this::class.members.filterIsInstance<KProperty<*>>().mapNotNull {
        if (!it.isLateinit)
            return@mapNotNull null
        try {
            it.call(this)
        } catch (e: InvocationTargetException) {
            return@mapNotNull it
        }
        return@mapNotNull null
    }
}
