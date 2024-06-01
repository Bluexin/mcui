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

import be.bluexin.mcui.Constants
import be.bluexin.mcui.deprecated.api.elements.animator.Easing
import be.bluexin.mcui.deprecated.screens.CoreGUI
import be.bluexin.mcui.deprecated.screens.MouseButton
import be.bluexin.mcui.deprecated.screens.unaryPlus
import be.bluexin.mcui.util.math.BoundingBox2D
import be.bluexin.mcui.util.math.Vec2d
import com.mojang.blaze3d.vertex.PoseStack
import org.joml.Vector2d
import org.koin.core.component.KoinComponent

/**
 * Part of saoui by Bluexin, released under GNU GPLv3.
 *
 * @author Bluexin
 */
interface INeoParent : KoinComponent /* FIXME: meh */ {

    var parent: INeoParent?
        get() = null
        set(_) = Unit
    val tlParent: INeoParent
        get() = parent?.tlParent ?: this
    val controllingParent: INeoParent?
        get() {
            return if (parent is CoreGUI<*>) {
                parent
            } else if (parent is CategoryButton) {
                if (parent!!.parent is CoreGUI<*>) {
                    parent!!.parent
                } else parent!!.parent?.parent
            } else parent?.parent
        }
    val controllingGUI: CoreGUI<*>?
        get() {
            return tlParent as? CoreGUI<*>
        }
    var pos: Vec2d
    var destination: Vec2d
    var scroll
        get() = 0
        set(_) = Unit
    var isOpen: Boolean
    fun move(delta: Vec2d) {
        try {
            CoreGUI.animator.removeAnimationsFor(this)
        } catch (e: ConcurrentModificationException) {
            Constants.LOG.error("Element caused a concurrent modification exception on animation")
            e.printStackTrace()
        }
        destination += delta
        +basicAnimation(this, "pos") {
            to = destination
            duration = 10f
            easing = Easing.easeInOutQuint
        }
    }

    fun mouseClicked(pos: Vec2d, mouseButton: MouseButton) = false
    fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean
    fun isFocus(): Boolean {
        return isOpen || parent?.isOpen == true || (controllingParent as? CategoryButton)?.elements?.none { it.isOpen } ?: (controllingParent as? CoreGUI<*>)?.elements?.none { it.isOpen } ?: parent?.isOpen ?: true
    }
}

interface NeoElement : INeoParent {

    val boundingBox: BoundingBox2D
    var idealBoundingBox
        get() = boundingBox
        /**
         * The set method is used to apply changes. Not ideal but idk between this and [boundingBox]
         */
        set(_) {}
    val listed
        get() = true
    val visible
        get() = true
    var valid
        get() = true
        set(_) = Unit
    var selected
        get() = false
        set(_) = Unit
    var highlighted
        get() = false
        set(_) = Unit
    val disabled
        get() = false
    var opacity
        get() = 1f
        set(_) = Unit
    val scale
        get() = Vector2d(1.0)
    val canDraw: Boolean
        get() = opacity >= 0.03 || scale.x.toInt() == 0 || scale.y.toInt() == 0

    fun drawBackground(poseStack: PoseStack, mouse: Vec2d, partialTicks: Float)
    fun draw(poseStack: PoseStack, mouse: Vec2d, partialTicks: Float)
    fun drawForeground(poseStack: PoseStack, mouse: Vec2d, partialTicks: Float)
    operator fun contains(pos: Vec2d) = pos in boundingBox

    fun init() {}
    fun hide() = Unit
    fun show() = Unit
    fun update() = Unit
}

interface NeoParent : NeoElement {

    override var isOpen: Boolean
    override var selected: Boolean
    open val elements: MutableList<NeoElement>
    val elementsSequence
        get() = elements.asSequence()
    val otherElementsSequence
        get() = elementsSequence.filter { !it.listed && it.visible }
    val listedElementsSequence
        get() = elementsSequence.filter(NeoElement::listed)
    val validElementsSequence
        get() = listedElementsSequence.filter(NeoElement::valid)
    val visibleElementsSequence
        get() = validElementsSequence.filter(NeoElement::visible)

    val childrenXOffset
        get() = 0
    val childrenYOffset
        get() = 0
    val childrenXSeparator
        get() = 0
    val childrenYSeparator
        get() = 0
    override var parent: INeoParent?
    val futureOperations: MutableList<NeoParent.() -> Unit>
    override fun update() {
        super.update()

        this.elementsSequence.forEach(NeoElement::update)

        this.futureOperations.forEach { it() }
        this.futureOperations.clear()
    }

    operator fun plusAssign(element: NeoElement) {
        if (elementsSequence.none { it == element }) {
            if (elements.isNotEmpty()) {
                val bb1 = elements[0].idealBoundingBox
                val bbNew = element.idealBoundingBox
                if (bb1.widthI() >= bbNew.widthI()) {
                    element.idealBoundingBox = BoundingBox2D(bbNew.min, Vec2d(bb1.width(), bbNew.height()))
                } else {
                    elementsSequence.forEach {
                        val bb = it.idealBoundingBox
                        it.idealBoundingBox = BoundingBox2D(bb.min, Vec2d(bbNew.width(), bb.height()))
                    }
                    element.idealBoundingBox = bbNew
                }
            } else element.idealBoundingBox = element.idealBoundingBox
            elements += element

            element.parent = this
        }
    }

    operator fun NeoElement.unaryPlus() {
        this@NeoParent += this
    }

    override operator fun contains(pos: Vec2d) = super.contains(pos) /*||
            with(pos + vec(childrenXOffset, childrenYOffset)) {
                var npos = this
                elementsSequence.filter(NeoElement::visible).any {
                    val r = npos in it
                    npos += vec(childrenXSeparator, childrenYSeparator)
                    r
                }
            }*/
    fun performLater(block: NeoParent.() -> Unit) {
        futureOperations += block
    }
}
