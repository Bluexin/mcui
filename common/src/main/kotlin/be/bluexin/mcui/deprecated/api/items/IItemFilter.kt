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

package be.bluexin.mcui.deprecated.api.items

import be.bluexin.mcui.deprecated.api.screens.IIcon
import be.bluexin.mcui.util.Client
import be.bluexin.mcui.util.IconCore
import net.minecraft.world.inventory.InventoryMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

/**
 * This is used to send a specific set of items to be rendered as slots
 */
@FunctionalInterface
interface IItemFilter : (ItemStack, Boolean) -> Boolean, (ItemStack) -> Boolean {

    val displayName: String

    /**
     * @return Returns true if this is a Category or not
     * Categories do not have filters
     */
    val isCategory: Boolean
        get() = subFilters.isNotEmpty()

    val category: IItemFilter?
        get() = null

    val subFilters
        get() = ItemFilterRegister.filters().filter { it.category == this }

    val icon: IIcon
        get() = IconCore.ITEMS

    /**
     * Issues a check to see if an ItemStack matches a filter
     * Note that you will need to add your own filter when using
     * this.
     * Additional note that if isCategory is true, this will not
     * be checked
     *
     * @param stack The item stack to check
     * @param equipped If the item is Equipped or not
     * @return Returns a slot if true
     */
    override operator fun invoke(stack: ItemStack, equipped: Boolean): Boolean { return false }

    override fun invoke(p1: ItemStack) = this(p1, false)

    /**
     * If this filter should appear as a button in
     * the ingame menu
     */
    fun getValidSlots(): Set<Slot> { return hotbarSlots() }

    companion object {
        fun getPlayerSlots(slotId: Int): Set<Slot> {
            return setOf(Client.mc.player!!.inventoryMenu.getSlot(slotId))
        }

        fun getPlayerSlots(slotID: IntRange): Set<Slot> {
            return Client.mc.player!!.inventoryMenu.slots
                .filter { it.index in slotID }
                .toSet()
        }

        fun hotbarSlots(): Set<Slot> {
            return getPlayerSlots(InventoryMenu.USE_ROW_SLOT_START until InventoryMenu.USE_ROW_SLOT_END)
        }
    }
}
