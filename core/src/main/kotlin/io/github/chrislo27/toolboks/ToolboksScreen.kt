package io.github.chrislo27.toolboks

import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.Screen
import io.github.chrislo27.toolboks.ui.Stage

@Suppress("UNCHECKED_CAST")
public abstract class ToolboksScreen<G : ToolboksGame, SELF : ToolboksScreen<G, SELF>>(public val main: G) : Screen, InputProcessor {

    open val stage: Stage<SELF>? = null

    override fun render(delta: Float) {
        main.batch.begin()
        stage?.render(this as SELF, main.batch)
        main.batch.end()
    }

    open fun renderUpdate() {
        stage?.frameUpdate(this as SELF)
    }

    abstract fun tickUpdate()

    fun getDebugString(): String? {
        return null
    }

    protected open fun resizeStage() {
        stage?.onResize(stage!!.camera.viewportWidth, stage!!.camera.viewportHeight)
    }

    override fun resize(width: Int, height: Int) {
        resizeStage()
    }

    override fun show() {
        stage?.updatePositions()
        main.inputMultiplexer.removeProcessor(this)
        main.inputMultiplexer.addProcessor(this)
    }

    override fun hide() {
        main.inputMultiplexer.removeProcessor(this)
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return stage?.touchUp(screenX, screenY, pointer, button)?: false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return stage?.mouseMoved(screenX, screenY)?: false
    }

    override fun keyTyped(character: Char): Boolean {
        return stage?.keyTyped(character)?: false
    }

    override fun scrolled(amount: Int): Boolean {
        return stage?.scrolled(amount)?: false
    }

    override fun keyUp(keycode: Int): Boolean {
        return stage?.keyUp(keycode) ?: false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return stage?.touchDragged(screenX, screenY, pointer) ?: false
    }

    override fun keyDown(keycode: Int): Boolean {
        return stage?.keyDown(keycode) ?: false
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return stage?.touchDown(screenX, screenY, pointer, button) ?: false
    }
}