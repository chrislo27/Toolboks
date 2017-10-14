package io.github.chrislo27.toolboks.aabbcollision

import com.badlogic.gdx.utils.Pool


class CollisionResult() : Pool.Poolable, Comparable<CollisionResult> {

    // normals are faces, ex: (0, 1) is the top face, (1, 0) is the right face of the object you collided against
    var normalX = 0
    var normalY = 0

    /**
     * The percentage to travel.
     */
    var distance = 0.0f

    var collider: PhysicsBody? = null
    var collidedWith: PhysicsBody? = null

    private var invalid = false

    override fun reset() {
        normalY = 0
        normalX = normalY
        distance = 1f
        collider = null
        collidedWith = collider
        invalid = true
    }

    fun getRemainingTime(): Float {
        return 1f - distance
    }

    fun makeValid(): CollisionResult {
        invalid = false
        return this
    }

    override fun toString(): String {
        return "[" + (if (invalid) "INVALID," else "") + "normals=[" + normalX + "," + normalY + "],dist=" + distance + "]"
    }

    override fun compareTo(other: CollisionResult): Int {
        if (distance < 0)
            error("This distance is negative ($distance)")
        if (other.distance < 0)
            error("The other result's distance is negative (${other.distance})")

        return if (distance < other.distance) {
            -1
        } else if (distance > other.distance) {
            1
        } else {
            0
        }
    }

    fun collided(): Boolean {
        return normalX != 0 || normalY != 0
    }
}