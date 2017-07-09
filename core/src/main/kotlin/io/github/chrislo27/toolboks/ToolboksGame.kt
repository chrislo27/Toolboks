package io.github.chrislo27.toolboks

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import io.github.chrislo27.toolboks.logging.Logger
import io.github.chrislo27.toolboks.logging.SysOutPiper
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.tick.TickController
import io.github.chrislo27.toolboks.tick.TickHandler
import io.github.chrislo27.toolboks.version.Version

public abstract class ToolboksGame(val logger: Logger, val logToFile: Boolean,
                                   val version: Version,
                                   val emulatedSize: Pair<Int, Int>, val lockToEmulatedSize: Boolean)
    : Game(), TickHandler {

    lateinit var originalResolution: Pair<Int, Int>
        private set
    val defaultCamera: OrthographicCamera = OrthographicCamera()
    val tickController: TickController = TickController()

    /**
     * Should include the version
     */
    abstract fun getTitle(): String

    override fun create() {
        if (logToFile) {
            SysOutPiper.pipe(this)
        }
        Toolboks.LOGGER = logger

        originalResolution = Pair(Gdx.graphics.width, Gdx.graphics.height)
        resetCamera()
        tickController.init(this)
    }

    override fun render() {
        defaultCamera.update()
        tickController.update()
        super.render()


    }

    override fun tickUpdate(tickController: TickController) {
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        resetCamera()
        if (!lockToEmulatedSize) {
            // reload fonts
        }
    }

    override fun dispose() {
        super.dispose()
        ScreenRegistry.dispose()
        AssetRegistry.dispose()
    }

    fun resetCamera() {
        if (lockToEmulatedSize) {
            defaultCamera.setToOrtho(false, emulatedSize.first.toFloat(), emulatedSize.second.toFloat())
        } else {
            defaultCamera.setToOrtho(false, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        }
    }
}
