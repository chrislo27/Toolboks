package io.github.chrislo27.toolboks.ui

import io.github.chrislo27.toolboks.ToolboksScreen


/**
 * A stage that disappears if you click and none of its children respond.
 */
open class ContextMenu<S : ToolboksScreen<*, *>>(parent: UIElement<S>) : Stage<S>(parent.stage, parent.stage.camera) {

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val result = super.touchUp(screenX, screenY, pointer, button)

        if (!result) {
            parent!!.removeChild(this)
        }

        return result
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val result = super.touchDown(screenX, screenY, pointer, button)

        if (!result) {
            parent!!.removeChild(this)
        }

        return result
    }

}