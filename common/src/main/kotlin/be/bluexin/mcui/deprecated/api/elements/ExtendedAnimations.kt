/*
 * Copyright (C) 2016-2024 Arnaud 'Bluexin' Solé
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package be.bluexin.mcui.deprecated.api.elements

import be.bluexin.mcui.deprecated.api.elements.animator.*

/**
 * Part of saoui by Bluexin, released under GNU GPLv3.
 *
 * @author Bluexin
 */
open class IndexedScheduledCounter(val delay: Float, maxIdx: Int = 0, private val callBack: (Int) -> Unit) : Animation<ScheduledEventAnimation.PointlessAnimatableObject>(
    ScheduledEventAnimation.PointlessAnimatableObject,
    AnimatableProperty.get(ScheduledEventAnimation.PointlessAnimatableObject::class.java, "field")
) {

    var maxIdx = maxIdx
        set(value) {
            field = value
            duration = delay * maxIdx
        }
    private val lerper = LerperHandler.getLerperOrError(Int::class.javaPrimitiveType!!)
    private var currentIdx = -1

    init {
        duration = delay * maxIdx
    }

    override fun update(time: Float) {
        val progress = Easing.linear(timeFraction(time))
        val new = lerper.lerp(0, maxIdx, progress)
        if (new != currentIdx) {
            currentIdx = new
            callBack(currentIdx)
        }
    }
}

fun <T : Any> basicAnimation(target: T, property: AnimatableProperty<T>, init: (BasicAnimation<T>.() -> Unit)? = null): BasicAnimation<T> {
    val anim = BasicAnimation(target, property)
    if (init != null) anim.init()
    return anim
}

fun <T : Any> basicAnimation(target: T, property: String, init: (BasicAnimation<T>.() -> Unit)? = null) = basicAnimation(target, AnimatableProperty.get(target.javaClass, property), init)

operator fun Animator.plusAssign(animation: Animation<*>) = this.add(animation)
