package be.bluexin.mcui.themes.elements

import be.bluexin.luajksp.annotations.LuajExclude
import be.bluexin.luajksp.annotations.LuajExpose
import be.bluexin.mcui.themes.elements.access.TextAccess
import be.bluexin.mcui.themes.miniscript.CBoolean
import be.bluexin.mcui.themes.miniscript.CInt
import be.bluexin.mcui.themes.miniscript.CString
import org.luaj.vm2.LuaValue

@LuajExpose
data class Text(
    override val renderState: RenderState,
    override val transform: Transform,
    val textProperties: TextProperties,
) : Element {

    @LuajExclude
    override fun visit(visitor: ElementVisitor, context: ElementVisitor.Context) {
        if (visitor.start(renderState, context)) {
            visitor.transform(transform, context)
            visitor.draw(context) {
                val (text, rgba, shadow, centered) = textProperties

                drawString(
                    string = text(),
                    x = 0f,
                    y = 0f,
                    rgba = rgba.invoke(),
                    shadow = shadow(),
                    centered = centered()
                )
            }
        }
    }

    @LuajExpose
    data class TextProperties(
        var text: CString,
        var rgba: CInt,
        var shadow: CBoolean,
        var centered: CBoolean,
    )

    @LuajExclude
    override fun toLua(): LuaValue = TextAccess(this)
}
