package io.github.chrislo27.toolboks.util

object MemoryUtils {

    val usedMemory: Int
        get() = (Runtime.getRuntime().totalMemory() / 1024).toInt()

    val maxMemory: Int
        get() = (Runtime.getRuntime().maxMemory() / 1024).toInt()

    val freeMemory: Int
        get() = (Runtime.getRuntime().freeMemory() / 1024).toInt()

    val cores: Int
        get() = Runtime.getRuntime().availableProcessors()
}