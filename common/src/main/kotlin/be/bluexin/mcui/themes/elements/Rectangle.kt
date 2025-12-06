package be.bluexin.mcui.themes.elements

import be.bluexin.luajksp.annotations.LuajExclude
import be.bluexin.luajksp.annotations.LuajExpose
import be.bluexin.mcui.themes.elements.GLOperations.Companion.withColor
import be.bluexin.mcui.themes.elements.access.RectangleAccess
import be.bluexin.mcui.themes.miniscript.CDouble
import be.bluexin.mcui.themes.miniscript.CInt
import be.bluexin.mcui.themes.miniscript.CResourceLocation
import org.luaj.vm2.LuaValue

@LuajExpose
data class Rectangle(
    val renderState: RenderState,
    val transform: Transform,
    val geometry: Geometry,
) : Element {

    @LuajExclude
    override fun visit(visitor: ElementVisitor, context: ElementVisitor.Context) {
        if (visitor.start(renderState, context)) {
            visitor.transform(transform, context)
            visitor.draw(context) {
                val (color, width, height, texture, sourceX, sourceY, sourceWidth, sourceHeight, textureWidth, textureHeight) = geometry
                configureBlend(true)
                withColor(color?.invoke()) {
                    texture?.let(::bindTexture)
                    drawRectangle(
                        // Position relative to parent should be handled by transform
                        x = .0, y = .0, z = .0,
                        width = width(),
                        height = height(),
                        sourceX = sourceX(),
                        sourceY = sourceY(),
                        sourceWidth = sourceWidth(),
                        sourceHeight = sourceHeight(),
                        textureWidth = textureWidth(),
                        textureHeight = textureHeight(),
                    )
                }
            }
        }
    }

    @LuajExpose
    data class Geometry(
        var rgba: CInt?,
        var width: CDouble,
        var height: CDouble,
        override var texture: CResourceLocation?,
        var sourceX: CDouble,
        var sourceY: CDouble,
        var sourceWidth: CDouble,
        var sourceHeight: CDouble,
        var textureWidth: CInt,
        var textureHeight: CInt,
    ) : WithTexture

    @LuajExclude
    override fun toLua(): LuaValue = RectangleAccess(this)
}