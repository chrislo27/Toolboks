package io.github.chrislo27.toolboks.util.gdxutils

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Matrix4
import io.github.chrislo27.toolboks.ToolboksGame

fun SpriteBatch.fillRect(x: Float, y: Float, width: Float, height: Float) {
    this.draw(ToolboksGame.smallTexture, x, y, width, height)
}

fun SpriteBatch.drawRect(x: Float, y: Float, width: Float, height: Float, line: Float) {
    this.draw(ToolboksGame.smallTexture, x, y, width, line)
    this.draw(ToolboksGame.smallTexture, x, y + height, width, -line)
    this.draw(ToolboksGame.smallTexture, x, y, line, height)
    this.draw(ToolboksGame.smallTexture, x + width, y, -line, height)
}

inline fun SpriteBatch.batchCall(projection: Matrix4 = this.projectionMatrix, drawFunction: SpriteBatch.() -> Unit) {
    val oldProjection = this.projectionMatrix
    val oldColor = this.packedColor

    this.begin()
    this.drawFunction()
    this.end()

    this.projectionMatrix = oldProjection
    this.setColor(oldColor)
}
