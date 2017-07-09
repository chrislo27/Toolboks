package io.github.chrislo27.toolboks.font

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.utils.Disposable


class FreeTypeFont(val file: FileHandle, val defaultWindowSize: Pair<Int, Int>,
                   val fontSize: Int, val borderSize: Float,
                   val parameter: FreeTypeFontGenerator.FreeTypeFontParameter) : Disposable {

    constructor(file: FileHandle, defaultWindowSize: Pair<Int, Int>,
                parameter: FreeTypeFontGenerator.FreeTypeFontParameter):
            this(file, defaultWindowSize, parameter.size, parameter.borderWidth, parameter)

    private var generator: FreeTypeFontGenerator? = null
    var font: BitmapFont? = null
        private set
    private var afterLoad: FreeTypeFont.() -> Unit = {}

    fun setAfterLoad(func: FreeTypeFont.() -> Unit): FreeTypeFont {
        afterLoad = func
        return this
    }

    fun isLoaded(): Boolean = font != null

    fun load() {
        dispose()

        val scale: Float = run {
            if (Gdx.graphics.width <= Gdx.graphics.height) {
                return@run Gdx.graphics.width.toFloat() / defaultWindowSize.first
            } else {
                return@run Gdx.graphics.height.toFloat() / defaultWindowSize.second
            }
        }
        parameter.size = Math.round(fontSize * scale)
        parameter.borderWidth = borderSize * scale

        generator = FreeTypeFontGenerator(file)
        font = generator!!.generateFont(parameter)
    }

    override fun dispose() {
        font?.dispose()
        (font?.data as? Disposable)?.dispose()
        generator?.dispose()
    }
}