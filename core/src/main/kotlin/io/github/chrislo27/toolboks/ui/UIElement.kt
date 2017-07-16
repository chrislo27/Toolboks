package io.github.chrislo27.toolboks.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.drawRect
import io.github.chrislo27.toolboks.util.gdxutils.getInputX
import io.github.chrislo27.toolboks.util.gdxutils.getInputY


abstract class UIElement<S : ToolboksScreen<*, *>>
    : InputProcessor {

    val parent: UIElement<S>?
    private val parameterStage: Stage<S>?

    constructor(parent: UIElement<S>?, parameterStage: Stage<S>?) {
        this.parent = parent
        this.parameterStage = parameterStage
        this.alignment = Align.bottomLeft
        this.location = UIRectangle(0f, 0f, 1f, 1f, 0f, 0f, 0f, 0f)
        if (parent != null) {
        }
    }

    open val stage: Stage<S>
        get() = parameterStage ?: error("Stage is null")

    var alignment: Int
    var location: UIRectangle
    var visible: Boolean = true
    var wasClickedOn = false
        private set

    open fun removeChild(element: UIElement<S>): Boolean {
        return false
    }

    fun isMouseOver(): Boolean {
        return MathHelper.isPointIn(stage.camera.getInputX(), stage.camera.getInputY(), location.realX, location.realY,
                                    location.realWidth, location.realHeight)
    }

    open fun onLeftClick(xPercent: Float, yPercent: Float) {

    }

    open fun onRightClick(xPercent: Float, yPercent: Float) {

    }

    /**
     * Called each frame unless not visible
     */
    abstract fun render(screen: S, batch: SpriteBatch)

    /**
     * Called each frame when the parent screen is the current screen, regardless of visibility
     */
    abstract fun frameUpdate(screen: S)

    /**
     * Called each tick when the parent screen is the current screen, regardless of visibility
     */
    open fun tickUpdate(screen: S) {

    }

    open fun canBeClickedOn(): Boolean = true

    open fun onResize(width: Float, height: Float) {
        location.onResize(width, height)
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (canBeClickedOn() && visible && isMouseOver()) {
            wasClickedOn = true
        }

        return false
    }

    open fun drawOutline(batch: SpriteBatch, camera: OrthographicCamera, lineThickness: Float = 1f) {
        batch.drawRect(location.realX, location.realY, location.realWidth, location.realHeight, (camera.viewportWidth / Gdx.graphics.width) * lineThickness)
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val wasClickedOn = wasClickedOn
        this.wasClickedOn = false
        if (wasClickedOn && isMouseOver()) {
            val percentX: Float = (stage.camera.getInputX() - location.realX) / location.realWidth
            val percentY: Float = (stage.camera.getInputY() - location.realY) / location.realHeight
            when (button) {
                Input.Buttons.LEFT -> onLeftClick(percentX, percentY)
                Input.Buttons.RIGHT -> onRightClick(percentX, percentY)
                else -> return false
            }
            return true
        }
        return false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        return false
    }

    override fun scrolled(amount: Int): Boolean {
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return false
    }

    override fun keyDown(keycode: Int): Boolean {
        return false
    }

    inner class UIRectangle(var screenX: Float, var screenY: Float, var screenWidth: Float, var screenHeight: Float,
                            var pixelX: Float, var pixelY: Float, var pixelWidth: Float, var pixelHeight: Float) {

        fun set(screenX: Float = this.screenX, screenY: Float = this.screenY,
                screenWidth: Float = this.screenWidth, screenHeight: Float = this.screenHeight,
                pixelX: Float = this.pixelX, pixelY: Float = this.pixelY,
                pixelWidth: Float = this.pixelWidth, pixelHeight: Float = this.pixelHeight): UIRectangle {

            this.screenX = screenX
            this.screenY = screenY
            this.screenWidth = screenWidth
            this.screenHeight = screenHeight
            this.pixelX = pixelX
            this.pixelY = pixelY
            this.pixelWidth = pixelWidth
            this.pixelHeight = pixelHeight

            return this
        }

        var realX: Float = 0f
            private set
        var realY: Float = 0f
            private set

        var realWidth: Float = 0f
            private set
        var realHeight: Float = 0f
            private set

        fun onResize(width: Float, height: Float) {
            realWidth = screenWidth * width + pixelWidth
            realHeight = screenHeight * height + pixelHeight

            val parentX: Float = parent?.location?.realX ?: 0f
            val parentY: Float = parent?.location?.realY ?: 0f

            if ((alignment and Align.top) == Align.top) {
                realY = ((parentY) + (height)) - screenY * height - pixelY
            } else if ((alignment and Align.bottom) == Align.bottom) {
                realY = (parentY) + screenY * height + pixelY
            } else {
                realY = (parentY) + (height / 2) + screenY * height + pixelY
            }

            if ((alignment and Align.left) == Align.left) {
                realX = (parentX) + screenX * width + pixelX
            } else if ((alignment and Align.right) == Align.right) {
                realX = ((parentX) + width) - screenX * width - pixelX
            } else {
                realX = (parentX) + (width / 2) + screenX * width + pixelX
            }
        }

        override fun toString(): String {
            return "UIRectangle(screenX=$screenX, screenY=$screenY, screenWidth=$screenWidth, screenHeight=$screenHeight, pixelX=$pixelX, pixelY=$pixelY, pixelWidth=$pixelWidth, pixelHeight=$pixelHeight, realX=$realX, realY=$realY, realWidth=$realWidth, realHeight=$realHeight)"
        }

    }
}