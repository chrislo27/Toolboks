package io.github.chrislo27.toolboks

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
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
                            val emulatedSize: Pair<Int, Int>, val resizeAction: ResizeAction,
                            val minimumSize: Pair<Int, Int>)
    : Game(), TickHandler {

    companion object {

        lateinit var smallTexture: Texture
            private set

    }

    val versionString: String = version.toString()
    val defaultFontKey: String = "${Toolboks.TOOLBOKS_ASSET_PREFIX}default_font"
    val defaultBorderedFontKey: String = "${Toolboks.TOOLBOKS_ASSET_PREFIX}default_bordered_font"

    lateinit var originalResolution: Pair<Int, Int>
        private set
    val tickController: TickController = TickController()
    val defaultCamera: OrthographicCamera = OrthographicCamera()
    lateinit var fonts: FontHandler
        private set
    lateinit var batch: SpriteBatch
        private set
    lateinit var shapeRenderer: ShapeRenderer
        private set

    val defaultFont: BitmapFont
        get() = fonts[defaultFontKey].font!!
    val defaultBorderedFont: BitmapFont
        get() = fonts[defaultBorderedFontKey].font!!

    open val inputMultiplexer = InputMultiplexer()

    private var memoryDeltaTime: Float = 0f
    private var lastMemory: Long = 0L
    var memoryDelta: Long = 0L

    /**
     * Should include the version
     */
    abstract fun getTitle(): String

    abstract val programLaunchArguments: List<String>

    override fun create() {
        if (logToFile) {
            SysOutPiper.pipe(programLaunchArguments, this)
        }
        Toolboks.LOGGER = logger

        originalResolution = Pair(Gdx.graphics.width, Gdx.graphics.height)
        resetCamera()
        tickController.init(this)

        val pixmap: Pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(1f, 1f, 1f, 1f)
        pixmap.fill()
        smallTexture = Texture(pixmap)
        pixmap.dispose()

        batch = SpriteBatch()
        shapeRenderer = ShapeRenderer()
        fonts = FontHandler(this)
        fonts[defaultFontKey] = createDefaultFont()
        fonts[defaultBorderedFontKey] = createDefaultBorderedFont()
        fonts.loadAll(defaultCamera.viewportWidth, defaultCamera.viewportHeight)

        Gdx.input.inputProcessor = inputMultiplexer
    }

    open fun preRender() {
        defaultCamera.update()
        batch.projectionMatrix = defaultCamera.combined
        shapeRenderer.projectionMatrix = defaultCamera.combined
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
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                Toolboks.stageOutlines = !Toolboks.stageOutlines
                Toolboks.LOGGER.debug("Toggled stage outlines to ${Toolboks.stageOutlines}")
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
                System.gc()
            }
        }
        if (screen != null) {
            (screen as? ToolboksScreen<*, *>)?.renderUpdate()
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        Gdx.gl.glClearDepthf(1f)
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT)
    }

    open fun postRender() {

    }

    override fun render() {
        val nano = measureNanoTime {
            preRender()
            super.render()
            postRender()

            memoryDeltaTime += Gdx.graphics.deltaTime
            if (memoryDeltaTime >= 1f) {
                memoryDeltaTime = 0f
                val heap = Gdx.app.nativeHeap
                memoryDelta = heap - lastMemory
                lastMemory = heap
            }

            if (Toolboks.debugMode) {
                val font = defaultBorderedFont
                batch.begin()
                font.data.setScale(0.75f)

                val fps = Gdx.graphics.framesPerSecond
                val string =
                        """FPS: [${if (fps <= 10) "RED" else if (fps < 30) "YELLOW" else "WHITE"}]$fps[]
Debug mode: ${Toolboks.DEBUG_KEY_NAME}
  While holding ${Toolboks.DEBUG_KEY_NAME}: I - Reload I18N | S - Toggle stage outlines | G - Garbage collect
Version: $versionString
Memory usage: ${NumberFormat.getIntegerInstance().format(Gdx.app.nativeHeap / 1024)} / ${NumberFormat.getIntegerInstance().format(
                                MemoryUtils.maxMemory)} KB (${NumberFormat.getIntegerInstance().format(memoryDelta / 1024)} KB/s)

Screen: ${screen?.javaClass?.canonicalName}
${getDebugString()}
${(screen as? ToolboksScreen<*, *>)?.getDebugString() ?: ""}"""

                font.drawCompressed(batch, string, 8f, Gdx.graphics.height - 8f, Gdx.graphics.width - 16f, Align.left)

                font.data.setScale(1f)
                batch.end()
            }
        }
    }

    open fun getDebugString(): String {
        return ""
    }

    override fun tickUpdate(tickController: TickController) {
        if (screen != null) {
            (screen as? ToolboksScreen<*, *>)?.tickUpdate()
        }
    }

    override fun resize(width: Int, height: Int) {
        resetCamera()
        if (resizeAction != ResizeAction.ANY_SIZE) {
            val nano = measureNanoTime {
                fonts.loadAll(defaultCamera.viewportWidth, defaultCamera.viewportHeight)
            }
            Toolboks.LOGGER.info("Reloaded all ${fonts.fonts.size} fonts in ${nano / 1_000_000.0} ms")
        }
        super.resize(width, height)
    }

    override fun dispose() {
        super.dispose()

        batch.dispose()
        shapeRenderer.dispose()
        fonts.dispose()
        smallTexture.dispose()

        ScreenRegistry.dispose()
        AssetRegistry.dispose()
    }

    abstract fun createDefaultFont(): FreeTypeFont

    abstract fun createDefaultBorderedFont(): FreeTypeFont

    fun resetCamera() {
        when (resizeAction) {
            ResizeAction.ANY_SIZE -> defaultCamera.setToOrtho(false, Gdx.graphics.width.toFloat(),
                                                              Gdx.graphics.height.toFloat())
            ResizeAction.LOCKED -> defaultCamera.setToOrtho(false, emulatedSize.first.toFloat(),
                                                            emulatedSize.second.toFloat())
            ResizeAction.KEEP_ASPECT_RATIO -> {
                val width: Float
                val height: Float

                if (Gdx.graphics.width < Gdx.graphics.height) {
                    width = Gdx.graphics.width.toFloat()
                    height = (emulatedSize.second.toFloat() / emulatedSize.first) * width
                } else {
                    height = Gdx.graphics.height.toFloat()
                    width = (emulatedSize.first.toFloat() / emulatedSize.second) * height
                }

                defaultCamera.setToOrtho(false, width, height)
            }
        }
        if (defaultCamera.viewportWidth < minimumSize.first || defaultCamera.viewportHeight < minimumSize.second) {
            Toolboks.LOGGER.info("Camera too small, forcing it at minimum")
            defaultCamera.setToOrtho(false, minimumSize.first.toFloat(), minimumSize.second.toFloat())
        }
        defaultCamera.update()
        Toolboks.LOGGER.info(
                "Resizing camera as $resizeAction, window is ${Gdx.graphics.width} x ${Gdx.graphics.height}, camera is ${defaultCamera.viewportWidth} x ${defaultCamera.viewportHeight}")
    }
}
