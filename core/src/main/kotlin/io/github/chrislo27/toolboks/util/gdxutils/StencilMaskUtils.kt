package io.github.chrislo27.toolboks.util.gdxutils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

/**
 * Call this with the function to draw primitives, then draw sprites with [useStencilMask]
 */
inline fun ShapeRenderer.prepareStencilMask(batch: SpriteBatch, drawing: ShapeRenderer.() -> Unit): SpriteBatch {
    if (batch.isDrawing)
        batch.end()

    Gdx.gl.glDepthFunc(GL20.GL_LESS)
    Gdx.gl.glEnable(GL20.GL_DEPTH_TEST)
    Gdx.gl.glDepthMask(true)
    Gdx.gl.glColorMask(false, false, false, false)

    this.drawing()

    batch.begin()
    Gdx.gl.glDepthMask(false)
    Gdx.gl.glColorMask(true, true, true, true)
    Gdx.gl.glEnable(GL20.GL_DEPTH_TEST)
    Gdx.gl.glDepthFunc(GL20.GL_EQUAL)

    return batch
}

inline fun SpriteBatch.useStencilMask(drawing: () -> Unit) {
    drawing()

    this.flush()
    Gdx.gl.glDisable(GL20.GL_DEPTH_TEST)
}

