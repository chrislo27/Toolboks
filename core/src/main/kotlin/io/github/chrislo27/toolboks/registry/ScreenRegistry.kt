package io.github.chrislo27.toolboks.registry

import com.badlogic.gdx.Screen
import com.badlogic.gdx.utils.Disposable
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.ToolboksGame
import io.github.chrislo27.toolboks.ToolboksScreen


object ScreenRegistry : Disposable {

    val screens: Map<String, ToolboksScreen<*, *>> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    inline operator fun <reified G : ToolboksGame> get(key: String): ToolboksScreen<G, *>? {
        return screens[key] as ToolboksScreen<G, *>?
    }

    inline fun <reified G : ToolboksGame> getNonNull(key: String): ToolboksScreen<*, *> =
            get<G>(key) ?: throw IllegalArgumentException("No screen found with key $key")

    operator fun plusAssign(pair: Pair<String, ToolboksScreen<*, *>>) {
        add(pair.first, pair.second)
    }

    fun add(key: String, screen: ToolboksScreen<*, *>) {
        if (key.startsWith( Toolboks.TOOLBOKS_ASSET_PREFIX)) {
            throw IllegalArgumentException("$key starts with Toolboks asset prefix, which is ${Toolboks.TOOLBOKS_ASSET_PREFIX}")
        }
        if (screens.containsKey(key)) {
            throw IllegalArgumentException("Already contains key $key")
        }
        (screens as MutableMap)[key] = screen
    }

    internal fun addToolboks(keyWithoutPrefix: String, screen: ToolboksScreen<*, *>) {
        if (screens.containsKey(Toolboks.TOOLBOKS_ASSET_PREFIX + keyWithoutPrefix)) {
            throw IllegalArgumentException("Already contains key " + Toolboks.TOOLBOKS_ASSET_PREFIX + keyWithoutPrefix)
        }
        screens as MutableMap
        screens[Toolboks.TOOLBOKS_ASSET_PREFIX + keyWithoutPrefix] = screen
    }

    override fun dispose() {
        screens.values.forEach(Screen::dispose)
    }

}