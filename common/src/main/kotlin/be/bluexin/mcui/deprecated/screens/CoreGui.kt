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

package be.bluexin.mcui.deprecated.screens

import be.bluexin.mcui.GLCore
import be.bluexin.mcui.config.OptionCore
import be.bluexin.mcui.deprecated.api.elements.*
import be.bluexin.mcui.deprecated.api.elements.animator.Animation
import be.bluexin.mcui.deprecated.api.elements.animator.Animator
import be.bluexin.mcui.deprecated.api.screens.IIcon
import be.bluexin.mcui.deprecated.screens.util.Popup
import be.bluexin.mcui.util.Client
import be.bluexin.mcui.util.math.Vec2d
import be.bluexin.mcui.util.math.clamp
import be.bluexin.mcui.util.math.vec
import com.mojang.blaze3d.platform.InputConstants
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.Vec2

/**
 * Part of saoui by Bluexin, released under GNU GPLv3.
 *
 * @author Bluexin
 */
abstract class CoreGUI<T : Any>(
    final override var pos: Vec2d,
    override var destination: Vec2d = pos,
    val elements: MutableList<NeoElement> = mutableListOf()
) : Screen(Component.literal("Core GUI")), INeoParent {

    var viewSet: Boolean = false
    var playerView: Vec2 = Vec2(0f, 0f)
    open var previousMouse: Vec2d = Vec2d(0.0, 0.0)

    var subGui: CoreGUI<*>? = null
        private set

    override var parent: INeoParent? = null

    override var isOpen: Boolean = true

    val getPopup: Popup<*>?
        get() {
            return if (this is Popup<*>) {
                this
            } else subGui?.getPopup
        }

    override fun render(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        GLCore.glBlend(true)
        poseStack.pushPose()

        if (OptionCore.UI_MOVEMENT.isEnabled) {
            if (!viewSet) {
                playerView = Client.mc.player!!.rotationVector
                previousMouse = Vec2d(mouseX.toDouble(), mouseY.toDouble())
                viewSet = true
            } else {
                Client.mc.player!!.xRotO = playerView.x - ((width / 2 - mouseX) / 2) * 0.25F
                Client.mc.player!!.yRotO = playerView.y - ((height / 2 - mouseY) / 2) * 0.25F
                pos = pos.add((mouseX - previousMouse.x) * 0.25, (mouseY - previousMouse.y) * 0.25)
                previousMouse = Vec2d(mouseX.toDouble(), mouseY.toDouble())
            }
        } else if (viewSet) {
            Client.mc.player!!.xRotO = playerView.x
            Client.mc.player!!.yRotO = playerView.y
        }
        poseStack.translate(pos.x, pos.y, 0.0)

        val mousePos = if (subGui == null) vec(mouseX, mouseY) - pos else Vec2d.NEG_INFINITY
        elements.forEach { it.drawBackground(poseStack, mousePos, partialTicks) }
        elements.forEach { it.draw(poseStack, mousePos, partialTicks) }
        elements.forEach { it.drawForeground(poseStack, mousePos, partialTicks) }

        GLCore.lighting(false)

        poseStack.popPose()

        subGui?.render(poseStack, mouseX, mouseY, partialTicks)
    }

    /*override fun updateScreen() {
        subGui?.updateScreen() ?: elements.forEach(NeoElement::update)
    }*/


    /*override fun handleMouseInput() {
        super.handleMouseInput()

        val i = Mouse.getEventDWheel()

        val x = Mouse.getEventX() * this.width / Client.mc.displayWidth
        val y = this.height - Mouse.getEventY() * this.height / Client.mc.displayHeight - 1

        if (i > 0) this.mouseClicked(vec(x, y), MouseButton.SCROLL_UP)
        if (i < 0) this.mouseClicked(vec(x, y), MouseButton.SCROLL_DOWN)
    }*/

    override fun mouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int): Boolean {
        return this.mouseClicked(vec(mouseX, mouseY), MouseButton.fromInt(mouseButton))
    }

    override fun mouseClicked(pos: Vec2d, mouseButton: MouseButton): Boolean {
        return subGui?.mouseClicked(pos, mouseButton) ?: elements.fold(false) { acc, it ->
            it.mouseClicked(pos - this.pos, mouseButton) || acc
        }
    }

    override fun mouseDragged(
        mouseX: Double,
        mouseY: Double,
        clickedMouseButton: Int,
        dragX: Double,
        dragY: Double
    ): Boolean {
        return subGui?.mouseDragged(mouseX, mouseY, clickedMouseButton, dragX, dragY) ?: false
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, state: Int): Boolean {
        return subGui?.mouseReleased(mouseX, mouseY, state) ?: false
    }

    /*override fun mouseScrolled(`$$0`: Double, `$$1`: Double, `$$2`: Double): Boolean {
        return super.mouseScrolled(`$$0`, `$$1`, `$$2`)
    }*/

    override fun isPauseScreen() = OptionCore.GUI_PAUSE.isEnabled

    fun tlCategory(icon: IIcon, body: (CategoryButton.() -> Unit)? = null) {
        this.elements += CategoryButton(IconElement(icon, vec(0, 25 * elements.size)), this, body)
    }

    fun tlCategory(icon: IIcon, index: Int, body: (CategoryButton.() -> Unit)? = null): CategoryButton {
        return CategoryButton(IconElement(icon, vec(0, 25 * index)), this, body)
    }

    operator fun NeoElement.unaryPlus() {
        this@CoreGUI.elements += this
    }

    fun <R : Any> openGui(gui: CoreGUI<R>): CoreGUI<R> {
        check(subGui == null) { "Already opened a sub gui of type ${subGui!!::class.qualifiedName}" }
        subGui = gui
        gui.parent = this

        KeyMapping.releaseAll()

//        while (Mouse.next());
//        while (InputConstants.next());

        val sr = minecraft!!.window
        gui.resize(minecraft!!, sr.guiScaledWidth, sr.guiScaledHeight)

        gui += { subGui = null }

        return gui
    }

    override fun resize(mc: Minecraft, width: Int, height: Int) {
        super.resize(mc, width, height)
        subGui?.resize(mc, width, height)
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return if (subGui == null) {
            if (keyCode == InputConstants.KEY_ESCAPE) {
                this.onClose()
                if (this.parent == null) {
                    Client.mc.setScreen(null)
//                    if (Client.mc.currentScreen == null) Client.mc.setIngameFocus()
                }
                true
            } else {
                elements.firstOrNull { it.isOpen && it.selected }?.keyReleased(keyCode, scanCode, modifiers) ?: let {
                    // If no elements are open and focuseds
                    if (Client.mc.options.keyUp.matches(keyCode, -1) || keyCode == InputConstants.KEY_UP) {
                        val selected = elements.firstOrNull { it.selected }
                        var index = elements.indexOf(selected)
                        if (index == -1) index = 0
                        else if (--index < 0) index = elements.size.minus(1)
                        selected?.selected = false
                        elements[index].selected = true
                        true
                    } else if (Client.mc.options.keyDown.matches(keyCode, -1) || keyCode == InputConstants.KEY_DOWN) {
                        val selected = elements.firstOrNull { it.selected }
                        var index = elements.indexOf(selected)
                        if (++index >= elements.size) index = 0
                        selected?.selected = false
                        elements[index].selected = true
                        true
                    } else if (Client.mc.options.keyRight.matches(keyCode, -1)
                        || keyCode == InputConstants.KEY_RIGHT
                        || Client.mc.options.keyJump.matches(keyCode, -1)
                        || keyCode == InputConstants.KEY_RETURN
                    ) {
                        val selected = elements.firstOrNull { it.selected } ?: return false
                        return if (selected is CategoryButton) {
                            selected.open()
                            true
                        } else selected is IconElement && selected.onClickBody(selected.pos, MouseButton.LEFT)
                    } else false
                }
            }
        } else subGui!!.keyReleased(keyCode, scanCode, modifiers)
    }

    override fun onClose() {
        super.onClose()

        this.elements.forEach {
            (it as? CategoryButton)?.close()
        }

        this.callbacks.forEach { it(result) }
    }

    open lateinit var result: T

    private var callbacks: List<(T) -> Unit> = mutableListOf()

    operator fun plusAssign(callback: (T) -> Unit) {
        this.callbacks += callback
    }

    companion object {
        val animator = Animator()
    }
}

enum class MouseButton {
    LEFT,
    RIGHT,
    MIDDLE,
    BACK,
    FORWARD,
    INVALID,
    SCROLL_UP,
    SCROLL_DOWN;

    companion object {
        fun fromInt(button: Int): MouseButton = entries.getOrNull(button.clamp(0, INVALID.ordinal)) ?: INVALID
    }
}

operator fun Animation<*>.unaryPlus() {
    CoreGUI.animator += this
}

@DslMarker
annotation class CoreGUIDsl
