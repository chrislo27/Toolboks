package io.github.chrislo27.toolboks.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.badlogic.gdx.backends.lwjgl.LwjglInput
import io.github.chrislo27.toolboks.ToolboksGame
import io.github.chrislo27.toolboks.desktop.util.DesktopMusicUtils
import io.github.chrislo27.toolboks.util.gdxutils.MusicUtils


class ToolboksDesktopLauncher(val game: ToolboksGame) {

    val config = LwjglApplicationConfiguration()

    init {
        System.setProperty("file.encoding", "UTF-8")
        LwjglInput.keyRepeatTime = 0.05f

        MusicUtils.instance = DesktopMusicUtils()
    }

    inline fun editConfig(func: LwjglApplicationConfiguration.() -> Unit): ToolboksDesktopLauncher {
        config.func()
        return this
    }

    fun launch(): LwjglApplication {
        val app = LwjglApplication(game, config)
        return app
    }

}