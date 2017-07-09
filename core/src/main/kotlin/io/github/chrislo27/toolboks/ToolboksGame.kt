package io.github.chrislo27.toolboks

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import io.github.chrislo27.toolboks.font.FontHandler
import io.github.chrislo27.toolboks.font.FreeTypeFont
import io.github.chrislo27.toolboks.logging.Logger
import io.github.chrislo27.toolboks.logging.SysOutPiper
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.tick.TickController
import io.github.chrislo27.toolboks.tick.TickHandler
import io.github.chrislo27.toolboks.version.Version
import kotlin.system.measureNanoTime

abstract class ToolboksGame(val logger: Logger, val logToFile: Boolean,
                                   val version: Version,
                                   val emulatedSize: Pair<Int, Int>, val lockToEmulatedSize: Boolean)
    : Game(), TickHandler {

    protected val defaultFontKey: String = "toolboks_default_font"
    protected val defaultBorderedFontKey: String = "toolboks_default_bordered_font"

    lateinit var originalResolution: Pair<Int, Int>
        private set
    val tickController: TickController = TickController()
    val defaultCamera: OrthographicCamera = OrthographicCamera()
    lateinit var fonts: FontHandler
        private set
    lateinit var batch: SpriteBatch
        private set

    val defaultFont: BitmapFont
        get() = fonts[defaultFontKey].font!!
    val defaultBorderedFont: BitmapFont
        get() = fonts[defaultBorderedFontKey].font!!

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

        batch = SpriteBatch()
        fonts = FontHandler(this)
        fonts[defaultFontKey] = createDefaultFont()
        fonts[defaultBorderedFontKey] = createDefaultBorderedFont()
        fonts.loadAll()
    }

    open fun preRender() {
        defaultCamera.update()
        batch.projectionMatrix = defaultCamera.combined
        tickController.update()

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    }

    open fun postRender() {

    }

    override fun render() {
        preRender()
        super.render()
        postRender()
    }

    override fun tickUpdate(tickController: TickController) {
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        resetCamera()
        if (!lockToEmulatedSize) {
            val nano = measureNanoTime(fonts::loadAll)
            Toolboks.LOGGER.info("Reloaded all ${fonts.fonts.size} fonts in ${nano / 1_000_000.0} ms")
        }
    }

    override fun dispose() {
        super.dispose()

        batch.dispose()
        fonts.dispose()

        ScreenRegistry.dispose()
        AssetRegistry.dispose()
    }

    abstract fun createDefaultFont(): FreeTypeFont

    abstract fun createDefaultBorderedFont(): FreeTypeFont

    fun resetCamera() {
        if (lockToEmulatedSize) {
            defaultCamera.setToOrtho(false, emulatedSize.first.toFloat(), emulatedSize.second.toFloat())
        } else {
            defaultCamera.setToOrtho(false, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        }
    }
}
