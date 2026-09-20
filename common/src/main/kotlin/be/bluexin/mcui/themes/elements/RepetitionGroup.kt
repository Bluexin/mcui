package be.bluexin.mcui.themes.elements

import be.bluexin.luajksp.annotations.LuajExclude
import be.bluexin.luajksp.annotations.LuajExpose
import be.bluexin.mcui.deprecated.api.elements.animator.internal.minus
import be.bluexin.mcui.themes.elements.access.RepetitionGroupAccess
import be.bluexin.mcui.themes.elements.visitor.ElementVisitor
import be.bluexin.mcui.themes.miniscript.CInt
import be.bluexin.mcui.themes.miniscript.CResourceLocation
import org.joml.Vector2d
import org.luaj.vm2.LuaValue

@LuajExpose
data class RepetitionGroup(
    val renderState: RenderState,
    override val transform: Transform,
    val children: List<Element>,
    val amount: CInt,
    val texture: CResourceLocation?,
) : Element {

    @LuajExclude
    override fun visit(visitor: ElementVisitor, context: ElementVisitor.Context) {
        if (visitor.start(renderState, context)) {
            visitor.transform(transform, context)

            texture?.let { tex ->
                visitor.draw(context) {
                    bindTexture(tex)
                }
            }

            val relativeContext = context.copy(
                mouse = context.mouse - Vector2d(transform.x(), transform.y())
            )

            val sortedChildren = children.sortedBy { it.transform.z() }
            val count = amount()
            for (i in 0 until count) {
                context.loopIndex(i)
                sortedChildren.forEach { child ->
                    child.visit(visitor, relativeContext)
                }
            }

            visitor.popTransform(context)
        }
    }

    @LuajExclude
    override fun toLua(): LuaValue = RepetitionGroupAccess(this)
}