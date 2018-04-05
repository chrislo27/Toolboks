package io.github.chrislo27.toolboks.util.gdxutils

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align


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

/**
 * Multiplies the current scale by x and y
 */
fun BitmapFont.scaleMul(x: Float, y: Float) {
    this.data.setScale(this.scaleX * x, this.scaleY * y)
}

fun BitmapFont.scaleMul(coefficient: Float) {
    this.scaleMul(coefficient, coefficient)
}

fun BitmapFont.drawCompressed(batch: SpriteBatch, text: String, x: Float, y: Float, width: Float,
                              align: Int): GlyphLayout {
    val font = this
    val textWidth = this.getTextWidth(text)
    val oldScaleX = font.data.scaleX

    if (textWidth > width) {
        font.data.scaleX = (width / textWidth) * oldScaleX
    }

    val layout = font.draw(batch, text, x, y, width, align, false)

    font.data.scaleX = oldScaleX

    return layout
}

fun BitmapFont.drawConstrained(batch: SpriteBatch, text: String, x: Float, y: Float, width: Float, height: Float,
                               align: Int): GlyphLayout {
    val font = this
    val textWidth = this.getTextWidth(text)
    val textHeight = this.getTextHeight(text, width, true)
    val oldScaleX = font.data.scaleX
    val oldScaleY = font.data.scaleY
    val oldLineHeight = font.data.down

    if (textWidth > width) {
        font.data.scaleX = (width / textWidth) * oldScaleX
    }

    if (textHeight > height) {
        font.data.scaleY = (height / textHeight) * oldScaleY
        font.data.down = (height / textHeight) * oldLineHeight
    }

    val layout = font.draw(batch, text, x, y, width, align, true)

    font.data.scaleX = oldScaleX
    font.data.scaleY = oldScaleY
    font.data.down = oldLineHeight

    return layout
}
