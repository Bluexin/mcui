package be.bluexin.mcui.themes.miniscript.api

import be.bluexin.luajksp.annotations.LKExposed
import be.bluexin.luajksp.annotations.LuajExclude
import be.bluexin.luajksp.annotations.LuajExpose
import be.bluexin.mcui.themes.miniscript.api.access.MiniscriptContainerAccess
import net.minecraft.world.Container
import org.luaj.vm2.LuaValue

@LuajExpose
interface MiniscriptContainer : LKExposed {
    fun item(slot: Int): MiniscriptItemStack
    fun size(): Int

    @LuajExclude
    override fun toLua(): LuaValue = MiniscriptContainerAccess(this)
}

class MiniscriptContainerImpl(val container: Container) : MiniscriptContainer {
    override fun item(slot: Int) = MiniscriptItemStackImpl(container.getItem(slot))
    override fun size() = container.containerSize
}

