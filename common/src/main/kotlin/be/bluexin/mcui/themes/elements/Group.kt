package be.bluexin.mcui.themes.elements

import be.bluexin.luajksp.annotations.LuajExclude
import be.bluexin.luajksp.annotations.LuajExpose
import be.bluexin.mcui.deprecated.api.elements.animator.internal.minus
import be.bluexin.mcui.themes.elements.access.GroupAccess
import be.bluexin.mcui.themes.miniscript.CResourceLocation
import org.joml.Vector2d
import org.luaj.vm2.LuaValue

@LuajExpose
data class Group(
    override val renderState: RenderState,
    override val transform: Transform,
    val children: List<Element>,
    val texture: CResourceLocation? = null,
) : Element {

    @LuajExclude
    override fun visit(visitor: ElementVisitor, context: ElementVisitor.Context) {
        if (visitor.start(renderState, context)) {
            visitor.transform(transform, context)

            // Bind group texture if present (optimization for children sharing same texture)
            texture?.let { tex ->
                visitor.draw(context) {
                    bindTexture(tex)
                }
            }

            // Transform mouse coordinates relative to group position
            val relativeContext = context.copy(
                mouse = context.mouse - Vector2d(transform.x(), transform.y())
            )

            // Visit children in z-order
            children.sortedBy { it.transform.z() }.forEach { child ->
                child.visit(visitor, relativeContext)
            }
        }
    }

    @LuajExclude
    override fun toLua(): LuaValue = GroupAccess(this)
}
