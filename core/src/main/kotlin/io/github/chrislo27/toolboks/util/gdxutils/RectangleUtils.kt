package io.github.chrislo27.toolboks.util.gdxutils

import com.badlogic.gdx.math.Rectangle


val Rectangle.maxX: Float get() = this.x + this.width
val Rectangle.maxY: Float get() = this.y + this.height

/**
 * Same as [Rectangle.overlaps] but has equality instead of just less/greater than comparisons.
 */
fun Rectangle.intersects(r: Rectangle): Boolean {
    return x <= r.x + r.width && x + width >= r.x && y <= r.y + r.height && y + height >= r.y
}
