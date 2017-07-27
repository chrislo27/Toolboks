package io.github.chrislo27.toolboks.util.gdxutils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

/**
 * Call this while the batch isn't running, then call batch.begin(). Finish with [resetStencilMask()]
 */
inline fun ShapeRenderer.useStencilMask(drawing: ShapeRenderer.() -> Unit) {
    Gdx.gl.glDepthFunc(GL20.GL_LESS)
    Gdx.gl.glEnable(GL20.GL_DEPTH_TEST)
    Gdx.gl.glDepthMask(true)
    Gdx.gl.glColorMask(false, false, false, false)

    this.drawing()

    Gdx.gl.glDepthMask(false)
    Gdx.gl.glColorMask(true, true, true, true)
    Gdx.gl.glEnable(GL20.GL_DEPTH_TEST)
    Gdx.gl.glDepthFunc(GL20.GL_EQUAL)
}

fun SpriteBatch.resetStencilMask() {
    Gdx.gl.glDisable(GL20.GL_DEPTH_TEST)
    this.flush()
}
