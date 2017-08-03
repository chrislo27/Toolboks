package io.github.chrislo27.toolboks.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
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
    override var background = false
    var fontScaleMultiplier: Float = 1f
    var textColor: Color? = null

    open fun getRealText(): String =
            if (isLocalizationKey)
                Localization[text]
            else
                text
    
    open fun getFont(): BitmapFont =
            palette.font

    fun setText(text: String, align: Int = this.textAlign, wrapping: Boolean = textWrapping,
                isLocalization: Boolean = isLocalizationKey): TextLabel<S> {
        this.text = text
        this.textAlign = align
        this.textWrapping = wrapping
        isLocalizationKey = isLocalization
        return this
    }

    override fun render(screen: S, batch: SpriteBatch,
                        shapeRenderer: ShapeRenderer) {
        if (background) {
            val old = batch.packedColor
            batch.color = palette.backColor
            batch.fillRect(location.realX, location.realY, location.realWidth, location.realHeight)
            batch.setColor(old)
        }

        val labelWidth = location.realWidth
        val labelHeight = location.realHeight

        val textHeightWithWrap = getFont().getTextHeight(text, labelWidth, true)
        val shouldWrap = textHeightWithWrap <= location.realHeight
        val textHeight = getFont().getTextHeight(text, labelWidth, shouldWrap)
        val textWidth = getFont().getTextWidth(text, labelWidth, shouldWrap)

        val y: Float
        if ((textAlign and Align.top) == Align.top) {
            y = location.realY + location.realHeight
        } else if ((textAlign and Align.bottom) == Align.bottom) {
            y = location.realY + textHeight
        } else {
            y = location.realY + location.realHeight / 2 + textHeight / 2
        }

        val oldColor = getFont().color
        val oldScale = getFont().scaleX
        getFont().color = if (textColor != null) textColor else palette.textColor
        getFont().data.setScale(palette.fontScale * fontScaleMultiplier)
        if (textWrapping) {
            getFont().draw(batch, getRealText(), location.realX, y, labelWidth, textAlign, true)
        } else {
            getFont().drawCompressed(batch, getRealText(), location.realX, y, labelWidth, textAlign)
        }
        getFont().color = oldColor
        getFont().data.setScale(oldScale)
    }

}