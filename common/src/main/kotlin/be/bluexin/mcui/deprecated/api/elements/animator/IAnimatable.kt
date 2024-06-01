package be.bluexin.mcui.deprecated.api.elements.animator

/**
 * This is a copy from the library LibrarianLib
 * This code is covered under GNU Lesser General Public License v3.0
 */

/**
 * A property that can be animated.
 */
interface IAnimatable<T> {
    fun get(target: T): Any
    fun set(target: T, value: Any)
    fun doesInvolve(target: T, obj: Any): Boolean

    val type: Class<Any>
}
