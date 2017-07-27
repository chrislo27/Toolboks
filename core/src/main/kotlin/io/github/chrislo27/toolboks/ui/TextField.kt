package io.github.chrislo27.toolboks.ui

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.util.gdxutils.*


open class TextField<S : ToolboksScreen<*, *>>(override var palette: UIPalette, parent: UIElement<S>,
                                               parameterStage: Stage<S>)
    : UIElement<S>(parent, parameterStage), Palettable, Backgrounded {

    override var background: Boolean = false
    var text: String = ""
        set(value) {
            field = value
            carat = Math.min(0, text.length)
        }
    var textAlign: Int = Align.left
    var multiline: Boolean = false
    /**
     * 0 = start, the number is the index and then behind that
     */
    var carat: Int = 0
        set(value) {
            field = value.coerceIn(0, text.length)
        }

    open fun getFont(): BitmapFont =
            palette.font

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
        val textHeightNoWrap = getFont().getTextHeight(text, labelWidth, false)
        val shouldWrap = textHeightWithWrap <= location.realHeight
        val textHeight = if (shouldWrap) textHeightWithWrap else textHeightNoWrap
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
        getFont().color = palette.textColor
        getFont().data.setScale(palette.fontScale)

        shapeRenderer.prepareStencilMask(batch) {
            this.projectionMatrix = batch.projectionMatrix
            this.setColor(1f, 1f, 1f, 1f)
            this.begin(ShapeRenderer.ShapeType.Filled)
            this.rect(location.realX, location.realY, location.realWidth, location.realHeight)
            this.end()
        }.useStencilMask {
            getFont().draw(batch, text, location.realX, y, labelWidth, textAlign, !multiline)
        }

        getFont().color = oldColor
        getFont().data.setScale(oldScale)
    }

}