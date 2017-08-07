package io.github.chrislo27.toolboks.util.gdxutils

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Matrix4
import io.github.chrislo27.toolboks.ToolboksGame

fun SpriteBatch.fillRect(x: Float, y: Float, width: Float, height: Float) {
    this.draw(ToolboksGame.smallTexture, x, y, width, height)
}

fun SpriteBatch.drawRect(x: Float, y: Float, width: Float, height: Float, lineX: Float, lineY: Float) {
    this.draw(ToolboksGame.smallTexture, x, y, width, lineY)
    this.draw(ToolboksGame.smallTexture, x, y + height, width, -lineY)
    this.draw(ToolboksGame.smallTexture, x, y, lineX, height)
    this.draw(ToolboksGame.smallTexture, x + width, y, -lineX, height)
}

fun SpriteBatch.drawRect(x: Float, y: Float, width: Float, height: Float, line: Float) {
    this.drawRect(x, y, width, height, line, line)
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
