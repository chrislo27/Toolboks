package io.github.chrislo27.toolboks.util.gdxutils

import com.badlogic.gdx.math.Rectangle


val Rectangle.maxX: Float get() = this.x + this.width

val Rectangle.maxY: Float get() = this.y + this.height
