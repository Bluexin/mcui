package be.bluexin.mcui.themes.elements

import be.bluexin.luajksp.annotations.LuajExclude
import be.bluexin.luajksp.annotations.LuajExpose
import be.bluexin.mcui.themes.elements.access.RawElementAccess
import be.bluexin.mcui.themes.elements.visitor.ElementVisitor
import be.bluexin.mcui.themes.miniscript.CResourceLocation
import be.bluexin.mcui.themes.miniscript.CUnit
import org.luaj.vm2.LuaValue

@LuajExpose
data class RawElement(
    val renderState: RenderState,
    override val transform: Transform,
    val expression: CUnit,
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

            visitor.draw(context) {
                expression()
            }

            visitor.popTransform(context)
        }
    }

    @LuajExclude
    override fun toLua(): LuaValue = RawElementAccess(this)
}