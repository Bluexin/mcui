package be.bluexin.mcui.themes.serde.legacyformat.factory

import be.bluexin.mcui.themes.elements.Text
import be.bluexin.mcui.themes.miniscript.CBoolean
import be.bluexin.mcui.themes.miniscript.CDouble
import be.bluexin.mcui.themes.miniscript.CInt
import be.bluexin.mcui.themes.miniscript.CString
import be.bluexin.mcui.themes.serde.Factory
import be.bluexin.mcui.themes.serde.Factory.Context.Companion.tryRun
import be.bluexin.mcui.themes.serde.legacyformat.xml.StringXml
import org.koin.core.annotation.Single

@Single
internal class TextFactory : LegacyFactory<StringXml, Text>(StringXml::class) {
    override fun create(
        input: StringXml,
        context: Factory.Context
    ): Result<Text> = context.tryRun("glString[${input.name}]") {
        val renderState = createRenderState(input, context)

        val transform = createTransform(input, context).let { original ->
            @Suppress("DEPRECATION")
            val legacyOffsetY = input::legacyHeight.compileDouble(context)
            if (legacyOffsetY == null) original
            else original.copy(
                // Legacy compatibility: apply h/2 offset to y position when legacyHeight is present
                y = CDouble { original.y() + legacyOffsetY() / 2.0 }
            )
        }

        val textProperties = Text.TextProperties(
            text = input::text.compileString(context) ?: CString.EMPTY,
            rgba = input::rgba.compileInt(context) ?: CInt.WHITE,
            shadow = input::shadow.compileBoolean(context) ?: CBoolean.TRUE,
            centered = input::centered.compileBoolean(context) ?: CBoolean.TRUE,
        )

        Text(renderState, transform, textProperties)
    }
}
