package be.bluexin.mcui.themes.miniscript.api

import be.bluexin.luajksp.annotations.LuajExclude
import be.bluexin.luajksp.annotations.LuajExpose
import be.bluexin.mcui.themes.miniscript.api.access.MiniscriptPlayerInventoryAccess
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack
import org.luaj.vm2.LuaValue

@LuajExpose
interface MiniscriptPlayerInventory : MiniscriptContainer {
    fun selectedSlot(): Int

    /**
     * Subset of inventory limited to hotbar slots
     * @param slot hotbar slot index, from 0 to 8
     */
    fun hotbarItem(slot: Int): MiniscriptItemStack

    /**
     * Subset of inventory limited to non-hotbar, non-armor and non-offhand slots
     * @param slot inventory slot index, from 0 to 35
     */
    fun inventoryItem(slot: Int): MiniscriptItemStack

    /**
     * Armor slots
     * @param slot armor slot index, from 0 to 3
     */
    fun armorItem(slot: Int): MiniscriptItemStack

    /**
     * Offhand slots
     * @param slot offhand slot index, from 0 to 0
     */
    fun offHandItem(slot: Int): MiniscriptItemStack

    @LuajExclude
    override fun toLua(): LuaValue = MiniscriptPlayerInventoryAccess(this)
}

class MiniscriptPlayerInventoryImpl(val inventory: Inventory) : MiniscriptPlayerInventory,
    MiniscriptContainer by MiniscriptContainerImpl(inventory) {
    override fun selectedSlot() = inventory.selected
    override fun armorItem(slot: Int) = MiniscriptItemStackImpl(inventory.getArmor(slot))
    override fun hotbarItem(slot: Int) = MiniscriptItemStackImpl(
        if (Inventory.isHotbarSlot(slot)) inventory.items[slot] else ItemStack.EMPTY
    )

    override fun inventoryItem(slot: Int) = MiniscriptItemStackImpl(
        inventory.items[slot + Inventory.getSelectionSize()]
    )

    override fun offHandItem(slot: Int) = MiniscriptItemStackImpl(inventory.offhand[slot])

    override fun toLua() = super<MiniscriptPlayerInventory>.toLua()
}
