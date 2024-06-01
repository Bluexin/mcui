package be.bluexin.mcui.deprecated.api.elements.animator.internal

/**
 * This is a copy from the library LibrarianLib
 * This code is covered under GNU Lesser General Public License v3.0
 */

import be.bluexin.mcui.util.math.*
import org.joml.*

/**
 * TODO: Document file VecMutators
 *
 * Created by TheCodeWarrior
 */
object VecMutators {
    fun doubleFrom(any: Any): Double {
        any as? Number ?: throw IllegalArgumentException("Cannot cast `${any.javaClass.canonicalName}` to a Number")
        return any.toDouble()
    }

    init {

        //region Vec2d
        ImmutableFieldMutatorHandler.registerProvider(
            Vec2d::class.java,
            object : ImmutableFieldMutatorProvider<Vec2d> {
                override fun getMutatorForImmutableField(name: String): ImmutableFieldMutator<Vec2d>? {
                    when (name) {
                        "x" -> {
                            return object : ImmutableFieldMutator<Vec2d> {
                                override fun mutate(target: Vec2d, value: Any?): Vec2d {
                                    value ?: throw IllegalArgumentException("Cannot set Vec2d.x to null")
                                    return target.withX(doubleFrom(value))
                                }
                            }
                        }
                        "y" -> {
                            return object : ImmutableFieldMutator<Vec2d> {
                                override fun mutate(target: Vec2d, value: Any?): Vec2d {
                                    value ?: throw IllegalArgumentException("Cannot set Vec2d.y to null")
                                    return target.withY(doubleFrom(value))
                                }
                            }
                        }
                    }
                    return null
                }
            }
        )
        //endregion

        //region Vector3d
        ImmutableFieldMutatorHandler.registerProvider(
            Vector3dc::class.java,
            object : ImmutableFieldMutatorProvider<Vector3dc> {
                override fun getMutatorForImmutableField(name: String): ImmutableFieldMutator<Vector3dc>? {
                    when (name) {
                        "x" -> {
                            return object : ImmutableFieldMutator<Vector3dc> {
                                override fun mutate(target: Vector3dc, value: Any?): Vector3dc {
                                    value ?: throw IllegalArgumentException("Cannot set Vector3d.x to null")
                                    return target.withX(doubleFrom(value))
                                }
                            }
                        }
                        "y" -> {
                            return object : ImmutableFieldMutator<Vector3dc> {
                                override fun mutate(target: Vector3dc, value: Any?): Vector3dc {
                                    value ?: throw IllegalArgumentException("Cannot set Vector3d.y to null")
                                    return target.withX(doubleFrom(value))
                                }
                            }
                        }
                        "z" -> {
                            return object : ImmutableFieldMutator<Vector3dc> {
                                override fun mutate(target: Vector3dc, value: Any?): Vector3dc {
                                    value ?: throw IllegalArgumentException("Cannot set Vector3d.z to null")
                                    return target.withZ(doubleFrom(value))
                                }
                            }
                        }
                    }
                    return null
                }
            }
        )
        //endregion
    }
}

fun Vec2d.withX(other: Double) = vec(other, y)
fun Vec2d.withY(other: Double) = vec(x, other)

operator fun Vector3dc.times(other: Vector3dc): Vector3d = mul(other, Vector3d())
operator fun Vector3dc.times(other: Double): Vector3d = mul(other, Vector3d())
operator fun Vector3dc.times(other: Float): Vector3d = this * other.toDouble()
operator fun Vector3dc.times(other: Int): Vector3d = this * other.toDouble()

operator fun Vector3dc.div(other: Vector3dc) = div(other, Vector3d())
operator fun Vector3dc.div(other: Double): Vector3d = div(other, Vector3d())
operator fun Vector3dc.div(other: Float): Vector3d = this / other.toDouble()
operator fun Vector3dc.div(other: Int): Vector3d = this / other.toDouble()

operator fun Vector3dc.plus(other: Vector3dc): Vector3d = add(other, Vector3d())
operator fun Vector3dc.minus(other: Vector3dc): Vector3d = sub(other, Vector3d())
operator fun Vector3dc.unaryMinus(): Vector3d = negate(Vector3d())

operator fun Vector2dc.times(other: Vector2dc): Vector2d = mul(other, Vector2d())
operator fun Vector2dc.times(other: Double): Vector2d = mul(other, Vector2d())
operator fun Vector2dc.times(other: Float): Vector2d = this * other.toDouble()
operator fun Vector2dc.times(other: Int): Vector2d = this * other.toDouble()

operator fun Vector2dc.div(other: Vector2dc) = div(other, Vector2d())
operator fun Vector2dc.div(other: Double): Vector2d = div(other, Vector2d())
operator fun Vector2dc.div(other: Float): Vector2d = this / other.toDouble()
operator fun Vector2dc.div(other: Int): Vector2d = this / other.toDouble()

operator fun Vector2dc.plus(other: Vector2dc): Vector2d = add(other, Vector2d())
operator fun Vector2dc.minus(other: Vector2dc): Vector2d = sub(other, Vector2d())
operator fun Vector2dc.unaryMinus(): Vector2dc = negate(Vector2d())

infix fun Vector3d.dot(other: Vector3d) = dot(other)

infix fun Vector3d.cross(other: Vector3d): Vector3d = cross(other)

infix fun Vector3d.angle(other: Vector3d) = angle(other)

fun Vector3dc.withX(other: Double) = vec(other, y, z)
fun Vector3dc.withY(other: Double) = vec(x, other, z)
fun Vector3dc.withZ(other: Double) = vec(x, y, other)

fun Vector3dc.withX(other: Float) = withX(other.toDouble())
fun Vector3dc.withY(other: Float) = withY(other.toDouble())
fun Vector3dc.withZ(other: Float) = withZ(other.toDouble())

fun Vector3dc.withX(other: Int) = withX(other.toDouble())
fun Vector3dc.withY(other: Int) = withY(other.toDouble())
fun Vector3dc.withZ(other: Int) = withZ(other.toDouble())

val Vector3dc.crossMatrix get() = Matrix3d(
    0.0, -z, y,
    z, 0.0, -x,
    -y, x, 0.0
)
val Vector3d.tensorMatrix get() = Matrix3d(
    x * x, x * y, x * z,
    y * x, y * y, y * z,
    z * x, z * y, z * z
)

operator fun Vector3d.component1() = x
operator fun Vector3d.component2() = y
operator fun Vector3d.component3() = z
