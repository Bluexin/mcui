package be.bluexin.mcui.deprecated.api.elements.animator

/*
 * This is a copy from the library LibrarianLib
 * This code is covered under GNU Lesser General Public License v3.0
 */

/**
 * A basic animation from [from] to [to]. Both values default to the current value of this animation's property
 */
@Deprecated("Old animation lib from LibrarianLib")
class BasicAnimation<T : Any>(target: T, property: IAnimatable<T>) : Animation<T>(target, property) {
    @PublishedApi internal constructor(target: T, property: AnimatableProperty<T>) : this(target, property as IAnimatable<T>)

    /**
     * The value of the property at [start]
     */
    var from: Any = property.get(target)

    /**
     * The value of the property at [end]
     */
    var to: Any = property.get(target)

    /**
     * The easing function to use to animate between [start] and [end]
     */
    var easing: Easing = Easing.linear

    private var lerper = LerperHandler.getLerperOrError(property.type)

    override fun update(time: Float) {
        val progress = easing(timeFraction(time))
        val new = lerper.lerp(from, to, progress)
        property.set(target, new)
    }
}
