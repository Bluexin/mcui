package be.bluexin.mcui.themes.miniscript.api

import be.bluexin.luajksp.annotations.LuajExpose
import net.minecraft.world.item.ItemStack

@LuajExpose
interface MiniscriptItemStack {
    fun isEmpty(): Boolean
    fun count(): Int
}

class MiniscriptItemStackImpl(val itemStack: ItemStack) : MiniscriptItemStack {
    override fun isEmpty(): Boolean = itemStack.isEmpty
    override fun count(): Int = itemStack.count
}
