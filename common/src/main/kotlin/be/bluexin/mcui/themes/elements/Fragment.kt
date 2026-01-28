package be.bluexin.mcui.themes.elements

import be.bluexin.luajksp.annotations.LuajExclude
import be.bluexin.luajksp.annotations.LuajExpose
import be.bluexin.mcui.themes.elements.access.FragmentAccess
import be.bluexin.mcui.themes.serde.legacyformat.dto.NamedExpressionIntermediate
import org.luaj.vm2.LuaValue

@LuajExpose
data class Fragment(
    val group: Group,
    @LuajExclude
    val expect: Map<String, NamedExpressionIntermediate>
) : Element {
    override val transform: Transform
        get() = group.transform

    @LuajExclude
    override fun visit(visitor: ElementVisitor, context: ElementVisitor.Context) = group.visit(visitor, context)

    @LuajExclude
    override fun toLua(): LuaValue = FragmentAccess(this)
}
