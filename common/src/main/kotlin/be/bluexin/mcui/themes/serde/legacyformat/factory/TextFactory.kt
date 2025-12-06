package be.bluexin.mcui.themes.serde.legacyformat.factory

import be.bluexin.mcui.themes.elements.RenderState
import be.bluexin.mcui.themes.elements.Text
import be.bluexin.mcui.themes.elements.Transform
import be.bluexin.mcui.themes.miniscript.CBoolean
import be.bluexin.mcui.themes.miniscript.CDouble
import be.bluexin.mcui.themes.miniscript.CInt
import be.bluexin.mcui.themes.miniscript.CString
import be.bluexin.mcui.themes.serde.Factory
import be.bluexin.mcui.themes.serde.Factory.Context.Companion.tryRun
import be.bluexin.mcui.themes.serde.legacyformat.dto.StringXml

internal data object TextFactory : LegacyFactory<StringXml, Text>() {
    override fun create(
        input: StringXml,
        context: Factory.Context
    ): Result<Text> = context.tryRun("glString[${input.name}]") {
        val renderState = RenderState(
            enabled = input::enabled.compileBoolean(context) ?: CBoolean.TRUE,
            name = input.name,
        )

        // Legacy compatibility: apply h/2 offset to y position when legacyHeight is present
        val baseY = input::y.compileDouble(context) ?: CDouble.ZERO

        @Suppress("DEPRECATION")
        val legacyOffsetY = input::legacyHeight.compileDouble(context)?.let { h ->
            CDouble { baseY() + h() / 2.0 }
        } ?: baseY

        val transform = Transform(
            x = input::x.compileDouble(context) ?: CDouble.ZERO,
            y = legacyOffsetY,
            z = input::z.compileDouble(context) ?: CDouble.ZERO,
            scale = input::scale.compileDouble(context)
        )

        val textProperties = Text.TextProperties(
            text = input::text.compileString(context) ?: CString.EMPTY,
            rgba = input::rgba.compileInt(context) ?: CInt.WHITE,
            shadow = input::shadow.compileBoolean(context) ?: CBoolean.TRUE,
            centered = input::centered.compileBoolean(context) ?: CBoolean.TRUE,
        )

        Text(renderState, transform, textProperties)
    }
}
