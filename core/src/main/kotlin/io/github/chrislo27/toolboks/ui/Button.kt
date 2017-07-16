package io.github.chrislo27.toolboks.ui

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


open class Button<S : ToolboksScreen<*, *>>
    : UIElement<S>, Palettable {

    override var palette: UIPalette

    constructor(palette: UIPalette, parent: UIElement<S>, stage: Stage<S>) : super(parent, stage) {
        this.palette = palette
        this.labels = mutableListOf()
    }

    val labels: List<Label<S>>
    var enabled = true

    fun addLabel(l: Label<S>) {
        if (l.parent !== this) {
            throw IllegalArgumentException("Label parent must be this")
        }
        labels as MutableList
        labels.add(l)
    }

    fun removeLabel(l: Label<S>) {
        if (l.parent !== this) {
            throw IllegalArgumentException("Label parent must be this")
        }
        labels as MutableList
        labels.remove(l)
    }

    override fun canBeClickedOn(): Boolean {
        return enabled
    }

    override fun render(screen: S, batch: SpriteBatch) {
        val oldBatchColor = batch.color

        if (wasClickedOn) {
            batch.color = palette.clickedBackColor
        } else if (isMouseOver()) {
            batch.color = palette.highlightedBackColor
        } else {
            batch.color = palette.backColor
        }

        batch.fillRect(location.realX, location.realY, location.realWidth, location.realHeight)

        batch.color = oldBatchColor

        labels.forEach {
            it.render(screen, batch)
        }
    }

    override fun onResize(width: Float, height: Float) {
        super.onResize(width, height)
        labels.forEach {
            it.onResize(this.location.realWidth, this.location.realHeight)
        }
    }
}