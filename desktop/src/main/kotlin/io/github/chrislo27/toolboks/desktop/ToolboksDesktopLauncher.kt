package io.github.chrislo27.toolboks.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.ToolboksGame
import io.github.chrislo27.toolboks.logging.Logger
import io.github.chrislo27.toolboks.logging.SysOutPiper


class ToolboksDesktopLauncher(val game: ToolboksGame) {

    val config = LwjglApplicationConfiguration()
    var logToFile: Boolean = true
    var logger: Logger? = null

    init {
        System.setProperty("file.encoding", "UTF-8")
    }

    fun disableLoggingToFile(): ToolboksDesktopLauncher {
        logToFile = false
        return this
    }

    inline fun editConfig(func: LwjglApplicationConfiguration.() -> Unit): ToolboksDesktopLauncher {
        config.func()
        return this
    }

    fun setLogger(logger: Logger?): ToolboksDesktopLauncher {
        this.logger = logger
        return this
    }

    fun launch(): LwjglApplication {
        val logger: Logger = logger ?: Logger()

        return object : LwjglApplication(game, config) {
            init {
                Toolboks.LOGGER = logger
                if (logToFile) {
                    SysOutPiper.pipe(game)
                }
            }
        }
    }

}