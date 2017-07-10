package io.github.chrislo27.toolboks.i18n

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.I18NBundle
import io.github.chrislo27.toolboks.Toolboks
import java.util.*


object Localization {

    val baseHandle: FileHandle by lazy {
        Gdx.files.internal("localization/default")
    }

    var currentIndex: Int = 0
    var currentBundle: ToolboksBundle
        get() {
            return bundles[currentIndex]
        }
        set(value) {
            val index = bundles.indexOf(value)
            if (index != -1) {
                currentIndex = index
            }
        }
    val bundles: MutableList<ToolboksBundle> = mutableListOf()

    @JvmStatic
    fun createBundle(locale: NamedLocale): ToolboksBundle {
        return ToolboksBundle(locale, I18NBundle.createBundle(baseHandle, locale.locale, "UTF-8"))
    }

    fun reloadAll() {
        val old = bundles.toList()

        bundles.clear()
        old.mapTo(bundles) {
            createBundle(it.locale)
        }
    }

    fun cycle(direction: Int) {
        if (bundles.isEmpty()) {
            error("No bundles found")
        }

        currentIndex += direction
        if (currentIndex < 0) {
            currentIndex = bundles.size - 1
        } else if (currentIndex >= bundles.size) {
            currentIndex = 0
        }
    }

    private fun checkMissing(key: String): Boolean {
        if (currentBundle.missing[key] != null) {
            return true
        }
        try {
            currentBundle.bundle[key]
        } catch (e: MissingResourceException) {
            currentBundle.missing[key] = true
            Toolboks.LOGGER.warn("Missing content for I18N key $key in bundle ${currentBundle.locale}")

            return true
        }

        return false
    }

    operator fun get(key: String): String {
        if (checkMissing(key))
            return key

        return currentBundle.bundle[key]
    }

    operator fun get(key: String, vararg args: Any): String {
        if (checkMissing(key))
            return key

        return currentBundle.bundle.format(key, args)
    }

}