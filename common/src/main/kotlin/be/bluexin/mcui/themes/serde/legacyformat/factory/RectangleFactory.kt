package be.bluexin.mcui.themes.serde.legacyformat.factory

import be.bluexin.mcui.themes.elements.Rectangle
import be.bluexin.mcui.themes.miniscript.CDouble
import be.bluexin.mcui.themes.miniscript.CInt
import be.bluexin.mcui.themes.miniscript.CResourceLocation
import be.bluexin.mcui.themes.serde.Factory
import be.bluexin.mcui.themes.serde.Factory.Context.Companion.tryRun
import be.bluexin.mcui.themes.serde.legacyformat.xml.RectangleXml
import org.koin.core.annotation.Single

@Single
internal class RectangleFactory : LegacyFactory<RectangleXml, Rectangle>(RectangleXml::class) {
    override fun create(
        input: RectangleXml,
        context: Factory.Context
    ): Result<Rectangle> = context.tryRun("glRectangle[${input.name}]") {
        val renderState = createRenderState(input, context)
        val transform = createTransform(input, context)
        val width = input::width.compileDouble(context) ?: CDouble.ZERO
        val height = input::height.compileDouble(context) ?: CDouble.ZERO
        val texture = input::texture.compileString(context)
        val props = Rectangle.Geometry(
            rgba = input::rgba.compileInt(context),
            width = width,
            height = height,
            texture = texture?.let(::CResourceLocation),
            sourceX = input::sourceX.compileDouble(context) ?: CDouble.ZERO,
            sourceY = input::sourceY.compileDouble(context) ?: CDouble.ZERO,
            sourceWidth = input::sourceWidth.compileDouble(context) ?: width,
            sourceHeight = input::sourceHeight.compileDouble(context) ?: height,
            textureWidth = input::textureWidth.compileInt(context) ?: CInt { 256 },
            textureHeight = input::textureHeight.compileInt(context) ?: CInt { 256 },
        )

        // Do we want to return a crippled Rectangle, or no Rectangle at all if parts of it fail ? Rn the former
        Rectangle(renderState, transform, props)
    }
}