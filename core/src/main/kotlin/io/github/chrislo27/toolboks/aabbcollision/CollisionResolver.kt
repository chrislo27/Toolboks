package io.github.chrislo27.toolboks.aabbcollision

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.ReflectionPool
import io.github.chrislo27.toolboks.util.gdxutils.getMaxX
import io.github.chrislo27.toolboks.util.gdxutils.getMaxY
import java.util.*


/**
 * Swept AABB collision
 * https://www.gamedev.net/resources/_/technical/game-programming/swept-aabb-collision-detection-and-response-r3084
 */
class CollisionResolver {

    private val resultPool: Pool<CollisionResult> = ReflectionPool(CollisionResult::class.java, 4)
    private val tempResults = LinkedList<CollisionResult>()
    private val broadphase = Rectangle(0f, 0f, 1f, 1f)
    var timescale = 1f

    fun returnBorrowedResult(result: CollisionResult) {
        resultPool.free(result)
    }

    fun findCollisionPoint(body: PhysicsBody, target: PhysicsBody, result: CollisionResult): CollisionResult {
        result.reset()

        // get the distance from each possible edge
        val xEntryDist: Float
        val yEntryDist: Float
        val xExitDist: Float
        val yExitDist: Float

        if (body.velocity.x > 0) {
            xEntryDist = target.bounds.getX() - body.bounds.getMaxX()
            xExitDist = target.bounds.getMaxX() - body.bounds.getX()
        } else {
            xEntryDist = target.bounds.getMaxX() - body.bounds.getX()
            xExitDist = target.bounds.getX() - body.bounds.getMaxX()
        }

        if (body.velocity.y > 0) {
            yEntryDist = target.bounds.getY() - body.bounds.getMaxY()
            yExitDist = target.bounds.getMaxY() - body.bounds.getY()
        } else {
            yEntryDist = target.bounds.getMaxY() - body.bounds.getY()
            yExitDist = target.bounds.getY() - body.bounds.getMaxY()
        }

        var xEntryTime: Float
        var yEntryTime: Float
        val xExitTime: Float
        val yExitTime: Float

        if (body.velocity.x == 0f) {
            xEntryTime = Float.NEGATIVE_INFINITY
            xExitTime = Float.POSITIVE_INFINITY
        } else {
            xEntryTime = xEntryDist / (body.velocity.x * timescale)
            xExitTime = xExitDist / (body.velocity.x * timescale)
        }

        if (body.velocity.y == 0f) {
            yEntryTime = Float.NEGATIVE_INFINITY
            yExitTime = Float.POSITIVE_INFINITY
        } else {
            yEntryTime = yEntryDist / (body.velocity.y * timescale)
            yExitTime = yExitDist / (body.velocity.y * timescale)
        }

        if (xEntryTime == -0f) {
            xEntryTime = 0f
        }
        if (yEntryTime == -0f) {
            yEntryTime = 0f
        }

        val entryTime = Math.max(xEntryTime, yEntryTime)
        val exitTime = Math.min(xExitTime, yExitTime)

        // if there was no collision
        if (entryTime > exitTime || (xEntryTime < 0 && yEntryTime < 0) || xEntryTime > 1 || yEntryTime > 1) {
            result.reset()
            return result
        } else {
            // calculate normal of collided surface
            if (xEntryTime > yEntryTime) {
                if (xEntryDist < 0.0f) {
                    result.normal = Normal.RIGHT
                } else {
                    result.normal = Normal.LEFT
                }
            } else {
                if (yEntryDist < 0.0f) {
                    result.normal = Normal.TOP
                } else {
                    result.normal = Normal.BOTTOM
                }
            }

            // return the time of collision
            result.makeValid()
            result.distance = entryTime
            result.collider = body
            result.collidedWith = target

            return result
        }

    }

    fun findCollisionPoints(body: PhysicsBody, others: List<PhysicsBody>): List<CollisionResult> {
        val results: List<CollisionResult>
        val borrowed = tempResults
        borrowed.clear()

        broadphase.set(body.bounds.getX(), body.bounds.getY(), body.bounds.getWidth(),
                       body.bounds.getHeight())
        broadphase.setX(broadphase.getX() + Math.min(0f, body.velocity.x))
        broadphase.setY(broadphase.getY() + Math.min(0f, body.velocity.y))
        broadphase.setWidth(body.bounds.getWidth() + Math.abs(body.velocity.x))
        broadphase.setHeight(body.bounds.getHeight() + Math.abs(body.velocity.y))

        results = others.filter { pb ->
            pb !== body && pb.bounds.overlaps(broadphase)
        }.map { pb ->
            val temp = resultPool.obtain()
            borrowed += temp
            findCollisionPoint(body, pb, temp)
        }.filter(CollisionResult::collided).sorted()

        borrowed.filter { it !in results }.forEach(this::returnBorrowedResult)
        borrowed.clear()

        return results.takeUnless(List<CollisionResult>::isEmpty) ?: listOf(resultPool.obtain())
    }

    fun findFirstCollisionPoint(body: PhysicsBody, others: List<PhysicsBody>): CollisionResult {
        val results = findCollisionPoints(body, others)
        return results[0]
    }

}