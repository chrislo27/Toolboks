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
