package io.github.chrislo27.toolboks

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.toolboks.font.FontHandler
import io.github.chrislo27.toolboks.font.FreeTypeFont
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.logging.Logger
import io.github.chrislo27.toolboks.logging.SysOutPiper
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.tick.TickController
import io.github.chrislo27.toolboks.tick.TickHandler
import io.github.chrislo27.toolboks.util.MemoryUtils
import io.github.chrislo27.toolboks.util.gdxutils.drawCompressed
import io.github.chrislo27.toolboks.version.Version
import java.text.NumberFormat
import kotlin.system.measureNanoTime

abstract class ToolboksGame(val logger: Logger, val logToFile: Boolean,
                            val version: Version,
                            val emulatedSize: Pair<Int, Int>, val lockToEmulatedSize: Boolean)
    : Game(), TickHandler {

    val versionString: String = version.toString()
    protected val defaultFontKey: String = "${Toolboks.TOOLBOKS_ASSET_PREFIX}default_font"
    protected val defaultBorderedFontKey: String = "${Toolboks.TOOLBOKS_ASSET_PREFIX}default_bordered_font"

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

        // render update
        if (Gdx.input.isKeyJustPressed(Toolboks.DEBUG_KEY)) {
            Toolboks.debugMode = !Toolboks.debugMode
            Toolboks.LOGGER.debug("Switched debug mode to ${Toolboks.debugMode}")
        }
        if (Gdx.input.isKeyPressed(Toolboks.DEBUG_KEY)) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
                val nano = measureNanoTime(Localization::reloadAll)
                Toolboks.LOGGER.debug("Reloaded I18N from files in ${nano / 1_000_000.0} ms")
            }
        }
        if (screen != null) {
            (screen as? ToolboksScreen<*>)?.renderUpdate()
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    }

    open fun postRender() {
        if (Toolboks.debugMode) {
            val font = defaultBorderedFont
            batch.begin()
            font.data.setScale(0.75f)

            val fps = Gdx.graphics.framesPerSecond
            val string = """FPS: [${if (fps <= 10) "RED" else if (fps <= 30) "YELLOW" else "WHITE"}]$fps[]
Debug mode: ${Toolboks.DEBUG_KEY_NAME}
  While holding ${Toolboks.DEBUG_KEY_NAME}: I - Reload I18N
Version: $versionString
Memory usage: ${NumberFormat.getIntegerInstance().format(Gdx.app.nativeHeap / 1024)} / ${NumberFormat.getIntegerInstance().format(MemoryUtils.maxMemory)} KB
${if (screen is ToolboksScreen<*>) (screen as ToolboksScreen<*>).getDebugString() else ""}"""

            font.drawCompressed(batch, string, 8f, Gdx.graphics.height - 8f, Gdx.graphics.width - 16f, Align.left)

            font.data.setScale(1f)
            batch.end()
        }
    }

    override fun render() {
        val nano = measureNanoTime {
            preRender()
            super.render()
            postRender()
        }
    }

    override fun tickUpdate(tickController: TickController) {
        if (screen != null) {
            (screen as? ToolboksScreen<*>)?.tickUpdate()
        }
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
