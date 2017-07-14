package io.github.chrislo27.toolboks.ui

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.util.gdxutils.drawCompressed
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import io.github.chrislo27.toolboks.util.gdxutils.getTextHeight
import io.github.chrislo27.toolboks.util.gdxutils.getTextWidth


open class TextLabel<S : ToolboksScreen<*, *>>
    : Label<S>, Palettable {

    constructor(palette: UIPalette, parent: UIElement<S>, stage: Stage<S>) : super(palette, parent, stage) {
        this.textAlign = Align.center
    }

    var text: String = ""
    var textAlign: Int
    var isLocalizationKey = true
    var textWrapping = true
    var background = false

    open fun getRealText(): String =
            if (isLocalizationKey)
                Localization[text]
            else
                text

    fun setText(text: String, align: Int = this.textAlign, wrapping: Boolean = true,
                isLocalization: Boolean = isLocalizationKey): TextLabel<S> {
        this.text = text
        this.textAlign = align
        this.textWrapping = wrapping
        isLocalizationKey = isLocalization
        return this
    }

    override fun render(screen: S, batch: SpriteBatch) {
        if (background) {
            val old = batch.color
            batch.color = palette.backColor
            batch.fillRect(location.realX, location.realY, location.realWidth, location.realHeight)
            batch.color = old
        }

        val labelWidth = location.realWidth
        val labelHeight = location.realHeight

        val textHeightWithWrap = palette.font.getTextHeight(text, labelWidth, true)
        val textHeightNoWrap = palette.font.getTextHeight(text, labelWidth, false)
        val shouldWrap = textHeightWithWrap <= location.realHeight
        val textHeight = if (shouldWrap) textHeightWithWrap else textHeightNoWrap
        val textWidth = palette.font.getTextWidth(text, labelWidth, shouldWrap)

        val y: Float
        if ((textAlign and Align.top) == Align.top) {
            y = location.realY + location.realHeight
        } else if ((textAlign and Align.bottom) == Align.bottom) {
            y = location.realY + textHeight
        } else {
            y = location.realY + location.realHeight / 2 + textHeight / 2
        }

        val oldColor = palette.font.color
        val oldScale = palette.font.scaleX
        palette.font.color = palette.textColor
        palette.font.data.setScale(palette.fontScale)
        palette.font.drawCompressed(batch, getRealText(), location.realX, y, labelWidth, textAlign)
        palette.font.color = oldColor
        palette.font.data.setScale(oldScale)
    }

    override fun frameUpdate(screen: S) {
    }
}