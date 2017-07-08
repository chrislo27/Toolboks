package io.github.chrislo27.toolboks.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import io.github.chrislo27.toolboks.ToolboksGame


class ToolboksDesktopLauncher(val game: ToolboksGame) {

    val config = LwjglApplicationConfiguration()

    fun setSystemProperties(): ToolboksDesktopLauncher {
        System.setProperty("file.encoding", "UTF-8")
        return this
    }

    inline fun editConfig(func: LwjglApplicationConfiguration.() -> Unit): ToolboksDesktopLauncher {
        config.func()
        return this
    }

    fun launch(): LwjglApplication {
        return LwjglApplication(game, config)
    }

}