package io.github.chrislo27.toolboks.util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array

fun <T> Array<T>.toList(): List<T> =
        this.map { it }

fun <T> Array<T>.toMutableList(): MutableList<T> =
        this.map { it }.toMutableList()

object InputUtils {

    private val inputMap: MutableMap<Int, Boolean> = mutableMapOf()
    private val buttonMap: MutableMap<Int, Boolean> = mutableMapOf()

    fun Input.isKeyJustReleased(key: Int): Boolean {
        if (inputMap[key] == null)
            inputMap[key] = false

        val old = inputMap[key]
        val state = Gdx.input.isKeyPressed(key)

        inputMap[key] = state

        return !state && old == true
    }

    fun Input.isButtonJustReleased(button: Int): Boolean {
        if (buttonMap[button] == null)
            buttonMap[button] = false

        val old = buttonMap[button]
        val state = Gdx.input.isButtonPressed(button)

        inputMap[button] = state

        return state && old == false
    }

    fun Input.isButtonJustPressed(button: Int): Boolean {
        if (buttonMap[button] == null)
            buttonMap[button] = false

        val old = buttonMap[button]
        val state = Gdx.input.isButtonPressed(button)

        inputMap[button] = state

        return !state && old == true
    }

}

object FontUtils {

    private val glyphLayout: GlyphLayout = GlyphLayout()

    fun BitmapFont.getTextWidth(text: String): Float {
        glyphLayout.setText(this, text)
        return glyphLayout.width
    }

    fun BitmapFont.getTextHeight(text: String): Float {
        glyphLayout.setText(this, text)
        return glyphLayout.height
    }

    fun BitmapFont.getTextWidth(text: String, width: Float, wrap: Boolean): Float {
        glyphLayout.setText(this, text, Color.WHITE, width, Align.left, wrap)
        return glyphLayout.width
    }

    fun BitmapFont.getTextHeight(text: String, width: Float, wrap: Boolean): Float {
        glyphLayout.setText(this, text, Color.WHITE, width, Align.left, wrap)
        return glyphLayout.height
    }

    fun BitmapFont.drawCompressed(batch: SpriteBatch, text: String, x: Float, y: Float, width: Float,
                       align: Int) {
        val font = this
        val textWidth = this.getTextWidth(text)
        val oldScaleX = font.data.scaleX

        if (textWidth > width) {
            font.data.scaleX = (width / textWidth) * oldScaleX
        }

        font.draw(batch, text, x, y, width, align, false)

        font.data.scaleX = oldScaleX
    }

}
