package io.github.chrislo27.toolboks.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


/**
 * A standard image label.
 */
open class ImageLabel<S : ToolboksScreen<*, *>>
    : Label<S>, Palettable {

    constructor(palette: UIPalette, parent: UIElement<S>, stage: Stage<S>)
            : super(palette, parent, stage)

    enum class ImageRendering {

        /**
         * Draws the region at the full bounds of this element.
         */
        RENDER_FULL,

        /**
         * Maintains the region's original aspect ratio, attempting to maximize space.
         */
        ASPECT_RATIO

    }

    override var background = false
    var image: TextureRegion? = null
    var tint: Color = Color(1f, 1f, 1f, 1f)
    var renderType: ImageRendering = ImageRendering.RENDER_FULL
    var rotation: Float = 0f
    var rotationPoint = Vector2(0.5f, 0.5f)

    override fun render(screen: S, batch: SpriteBatch,
                        shapeRenderer: ShapeRenderer) {
        if (background) {
            val old = batch.packedColor
            batch.color = palette.backColor
            batch.fillRect(location.realX, location.realY, location.realWidth, location.realHeight)
            batch.setColor(old)
        }

        val image = this.image ?: return
        val old = batch.packedColor
        batch.color = tint

        when (renderType) {
            ImageLabel.ImageRendering.RENDER_FULL -> {
                batch.draw(image,
                           location.realX, location.realY,
                           rotationPoint.x * location.realWidth, rotationPoint.y * location.realHeight,
                           location.realWidth, location.realHeight,
                           1f, 1f,
                           rotation)
            }
            ImageLabel.ImageRendering.ASPECT_RATIO -> {
                val x: Float
                val y: Float
                val w: Float
                val h: Float
                if (location.realWidth <= location.realHeight) { // wider than tall
                    w = location.realWidth
                    x = location.realWidth / 2 - w / 2
                    h = w * (image.regionHeight.toFloat() / image.regionWidth)
                    y = location.realHeight / 2 - (h / 2)
                } else {
                    h = location.realHeight
                    y = location.realHeight / 2 - h / 2
                    w = h * (image.regionWidth.toFloat() / image.regionHeight)
                    x = location.realWidth / 2 - (w / 2)
                }

                batch.draw(image, location.realX + x, location.realY + y,
                           rotationPoint.x * w, rotationPoint.y * h,
                           w, h,
                           1f, 1f,
                           rotation)
            }
        }

        batch.setColor(old)
    }

}
