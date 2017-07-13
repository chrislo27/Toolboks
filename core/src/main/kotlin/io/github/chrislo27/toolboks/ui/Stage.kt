package io.github.chrislo27.toolboks.ui

import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import io.github.chrislo27.toolboks.ToolboksScreen


open class Stage<S : ToolboksScreen<*, *>>(parent: UIElement<S>?, val camera: OrthographicCamera)
    : UIElement<S>(parent, null), InputProcessor {

    override val stage: Stage<S>
        get() = this
    open val elements: MutableList<UIElement<S>> = mutableListOf()

    init {
        this.location.set(screenWidth = 1f, screenHeight = 1f)
        this.updatePositions()
    }

    override fun removeChild(element: UIElement<S>): Boolean {
        return elements.remove(element)
    }

    override fun render(screen: S, batch: SpriteBatch) {
        elements.filter(UIElement<S>::visible).forEach {
            it.render(screen, batch)
        }
    }

    override fun frameUpdate(screen: S) {
        elements.forEach {
            it.frameUpdate(screen)
        }
    }

    override fun tickUpdate(screen: S) {
        super.tickUpdate(screen)
        elements.forEach {
            it.tickUpdate(screen)
        }
    }

    fun updatePositions() {
        onResize(camera.viewportWidth, camera.viewportHeight)
    }

    override fun onResize(width: Float, height: Float) {
        super.onResize(width, height)
        elements.forEach { it.onResize(this.location.realWidth, this.location.realHeight) }
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return elements.filter { it.touchUp(screenX, screenY, pointer, button) }.any()
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return elements.filter { it.mouseMoved(screenX, screenY) }.any()
    }

    override fun keyTyped(character: Char): Boolean {
        return elements.filter { it.keyTyped(character) }.any()
    }

    override fun scrolled(amount: Int): Boolean {
        return elements.filter { it.scrolled(amount) }.any()
    }

    override fun keyUp(keycode: Int): Boolean {
        return elements.filter { it.keyUp(keycode) }.any()
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return elements.filter { it.touchDragged(screenX, screenY, pointer) }.any()
    }

    override fun keyDown(keycode: Int): Boolean {
        return elements.filter { it.keyDown(keycode) }.any()
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return elements.filter { it.touchDown(screenX, screenY, pointer, button) }.any()
    }
}