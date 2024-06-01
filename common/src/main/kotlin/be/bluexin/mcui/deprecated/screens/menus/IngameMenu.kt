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

package be.bluexin.mcui.deprecated.screens.menus

import be.bluexin.mcui.deprecated.api.elements.CategoryButton
import be.bluexin.mcui.deprecated.api.elements.IconElement
import be.bluexin.mcui.deprecated.api.elements.NeoElement
import be.bluexin.mcui.deprecated.api.elements.registry.ElementRegistry
import be.bluexin.mcui.deprecated.api.screens.IIcon
import be.bluexin.mcui.deprecated.screens.CoreGUI
import be.bluexin.mcui.util.Client
import be.bluexin.mcui.util.math.Vec2d
import be.bluexin.mcui.util.math.vec

/**
 * Part of saoui by Bluexin, released under GNU GPLv3.
 *
 * @author Bluexin
 */
class IngameMenu(elements: MutableList<NeoElement> = mutableListOf()) : CoreGUI<Unit>(Vec2d.ZERO, elements = elements) {

    var loggingOut = false

    override var result: Unit
        get() = Unit
        set(_) {}

    override fun init() {
        elements.clear()
        val defaultList = getDefaultElements()
        // TODO : now we hooked up ElementRegistry this event is being sent more than before !
//        val event = MenuBuildingEvent(defaultList)
//        MinecraftForge.EVENT_BUS.post(event)
        /*event.elements.forEach {
            it.parent = this
            it.show()
            +basicAnimation(it, "pos") {
                duration = 20f
                from = Vec2d.ZERO
                easing = Easing.easeInOutQuint
            }
        }
        elements.addAll(event.elements)*/

        pos = vec(width / 2.0 - 10, (height - elements.size * 20) / 2.0)
        destination = pos
//        SoundCore.ORB_DROPDOWN.play()

        if (!hasChecked) {
            /*if (!SAOCore.isSAOMCLibServerSide) {
                openGui(
                    PopupNotice(
                        format("notificationSAOMCLibTitle"),
                        listOf(format("notificationSAOMCLibDesc"), format("notificationSAOMCLibDesc2")),
                        ""
                    )
                )
            }*/
            hasChecked = true
        }
    }

    /*override fun updateScreen() {
        if (loggingOut) {
            UIUtil.closeGame()
        } else {
            CraftingUtil.updateItemHelper()
            super.updateScreen()
        }
    }*/

    fun getDefaultElements(): List<NeoElement> {
        return ElementRegistry.registeredElements[ElementRegistry.Type.INGAMEMENU]
            ?: ElementRegistry.getDefaultElements()
    }

    override fun isPauseScreen(): Boolean {
        return loggingOut || super.isPauseScreen()
    }

    companion object {
        val mc = Client.mc
        var hasChecked: Boolean = false
        fun tlCategory(
            icon: IIcon,
            index: Int,
            description: MutableList<String> = mutableListOf(),
            body: (CategoryButton.() -> Unit)? = null
        ): CategoryButton {
            return CategoryButton(IconElement(icon, vec(0, 25 * index), description = description), null, body)
        }
    }
}
