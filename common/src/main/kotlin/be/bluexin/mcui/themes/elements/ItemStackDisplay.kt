package be.bluexin.mcui.themes.elements

import be.bluexin.luajksp.annotations.LuajExclude
import be.bluexin.luajksp.annotations.LuajExpose
import be.bluexin.mcui.themes.miniscript.CInt
import be.bluexin.mcui.themes.miniscript.api.GameContext
import be.bluexin.mcui.themes.miniscript.api.MiniscriptItemStack
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue

@LuajExpose
data class ItemStackDisplay(
    val renderState: RenderState,
    override val transform: Transform,
    val properties: ItemSlotProperties,
) : Element {

    @LuajExclude
    override fun visit(visitor: ElementVisitor, context: ElementVisitor.Context) {
        if (visitor.start(renderState, context)) {
            visitor.transform(transform, context)

            val (slotIndex, inventorySource) = properties
            val itemStack = inventorySource(context.gameInfo, slotIndex())

            if (itemStack.isEmpty()) return

            visitor.draw(context) {
                renderItemStack(
                    x = 0,
                    y = 0,
                    partialTicks = context.gameInfo.gameWindowInfo().partialTicks(),
                    stack = itemStack,
                )
            }

            visitor.popTransform(context)
        }
    }

    @LuajExpose
    data class ItemSlotProperties(
        var slotIndex: CInt,
        var inventorySource: (GameContext, Int) -> MiniscriptItemStack,
    )

    @LuajExclude
    override fun toLua(): LuaValue = LuaTable()
}
