package be.bluexin.mcui.themes.elements

import be.bluexin.luajksp.annotations.LuajExclude
import be.bluexin.luajksp.annotations.LuajExpose
import be.bluexin.mcui.themes.elements.access.GroupAccess
import org.luaj.vm2.LuaValue

@LuajExpose
data class Group(
    val renderState: RenderState,
    val transform: Transform,
    val children: List<Element>,
) : Element {
    @LuajExclude
    override fun visit(visitor: ElementVisitor, context: ElementVisitor.Context) {
        if (visitor.start(renderState, context)) {
            visitor.transform(transform, context)
            children.forEach { it.visit(visitor, context) }
        }
    }

    @LuajExclude
    override fun toLua(): LuaValue = GroupAccess(this)
}
