package io.github.chrislo27.toolboks.i18n

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.ObjectMap
import io.github.chrislo27.toolboks.Toolboks
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.properties.Delegates


/**
 * Pretties up the libGDX localization system.
 */
object Localization {

    val baseHandle: FileHandle by lazy {
        Gdx.files.internal("localization/default")
    }

    var currentIndex: Int by Delegates.observable(0) { _, old, new ->
        listeners.forEach {
            it.invoke(bundles[old])
        }
    }
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
    val listeners: MutableList<(oldBundle: ToolboksBundle) -> Unit> = CopyOnWriteArrayList()

    @JvmStatic
    fun createBundle(locale: NamedLocale): ToolboksBundle {
        return ToolboksBundle(locale, I18NBundle.createBundle(baseHandle, locale.locale, "UTF-8"))
    }

    @Suppress("UNCHECKED_CAST")
    fun logMissingLocalizations() {
        val keys: List<String> = bundles.firstOrNull()?.bundle?.let { bundle ->
            val field = bundle::class.java.getDeclaredField("properties")
            field.isAccessible = true
            val map = field.get(bundle) as ObjectMap<String, String>

            map.keys().toList()
        } ?: return
        val missing: List<Pair<ToolboksBundle, List<String>>> = bundles.drop(1).map { tbundle ->
            val bundle = tbundle.bundle
            val field = bundle::class.java.getDeclaredField("properties")
            field.isAccessible = true
            val map = field.get(bundle) as ObjectMap<String, String>

            tbundle to (keys.map { key ->
                if (!map.containsKey(key)) {
                    key
                } else {
                    ""
                }
            }.filter { !it.isBlank() }).sorted()
        }

        missing.filter { it.second.isNotEmpty() }.forEach {
            Toolboks.LOGGER.warn("Missing keys for bundle ${it.first.locale}:${it.second.joinToString(
                    separator = "") { "\n  * $it" }}")
        }
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

        currentIndex = (currentIndex + direction).let {
            (if (it < 0) {
                bundles.size - 1
            } else if (it >= bundles.size) {
                0
            } else {
                it
            })
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

    operator fun get(key: String, vararg args: Any?): String {
        if (checkMissing(key))
            return key

        return currentBundle.bundle.format(key, *args)
    }

}