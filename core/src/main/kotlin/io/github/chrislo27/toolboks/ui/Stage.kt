package io.github.chrislo27.toolboks.ui

import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.toolboks.ToolboksScreen


open class Stage<S : ToolboksScreen<*, *>>
    : UIElement<S>, InputProcessor {

    val camera: OrthographicCamera

    override val stage: Stage<S>
        get() = this
    open val elements: MutableList<UIElement<S>> = mutableListOf()

    private var calledFromUpdatePositions: Boolean = false

    constructor(parent: UIElement<S>?, camera: OrthographicCamera) : super(parent, null) {
        this.camera = camera
        this.location.set(screenWidth = 1f, screenHeight = 1f)
        this.updatePositions()
    }

    override fun removeChild(element: UIElement<S>): Boolean {
        return elements.remove(element)
    }

    override fun render(screen: S, batch: SpriteBatch,
                        shapeRenderer: ShapeRenderer) {
        camera.update()
        val oldProj = batch.projectionMatrix
        batch.projectionMatrix = camera.combined
        elements.filter(UIElement<S>::visible).forEach {
            it.render(screen, batch, shapeRenderer)
        }
        batch.projectionMatrix = oldProj
    }

    override fun drawOutline(batch: SpriteBatch, camera: OrthographicCamera, lineThickness: Float) {
        if (camera !== this.camera)
            error("Camera passed in wasn't the stage's camera")
        val old = batch.packedColor
        batch.color = Color.ORANGE
        super.drawOutline(batch, camera, lineThickness)
        batch.setColor(old)
        elements.forEach {
            it.drawOutline(batch, this.camera, lineThickness)
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
        if (calledFromUpdatePositions) {
            error("Unthread-safe use of updatePositions")
        }
        calledFromUpdatePositions = true
        if (parent == null) {
            onResize(camera.viewportWidth, camera.viewportHeight)
        } else {
            onResize(parent.location.realWidth, parent.location.realHeight)
        }
    }

    override fun onResize(width: Float, height: Float) {
        val calledFromUpdatePositions = calledFromUpdatePositions
        this.calledFromUpdatePositions = false
        if (parent == null && !calledFromUpdatePositions) {
            error("onResize cannot be called without a parent. You're dumb, and should use updatePositions instead.")
        }
        super.onResize(width, height)
        if (elements.any {it.parent !== this}) {
            error("Elements ${elements.filter {it.parent !== this}.map {"[$it, parent=${it.parent}]"}} do not have this as their parent")
        }
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