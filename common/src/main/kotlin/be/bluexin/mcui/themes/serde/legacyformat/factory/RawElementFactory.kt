package be.bluexin.mcui.themes.serde.legacyformat.factory

import be.bluexin.mcui.themes.elements.RawElement
import be.bluexin.mcui.themes.miniscript.CUnit
import be.bluexin.mcui.themes.serde.Factory
import be.bluexin.mcui.themes.serde.Factory.Context.Companion.tryRun
import be.bluexin.mcui.themes.serde.legacyformat.xml.RawElementXml
import org.koin.core.annotation.Single

@Single
internal class RawElementFactory : LegacyFactory<RawElementXml, RawElement>(RawElementXml::class) {

    override fun create(
        input: RawElementXml,
        context: Factory.Context
    ): Result<RawElement> = context.tryRun("rawElement[${input.name}]") {
        val renderState = createRenderState(input, context)
        val transform = createTransform(input, context)
        val texture = input::texture.compileTextureCompat(context)
        val expression = input::expression.compileUnit(context) ?: CUnit.UNIT

        RawElement(renderState, transform, expression, texture)
    }
}