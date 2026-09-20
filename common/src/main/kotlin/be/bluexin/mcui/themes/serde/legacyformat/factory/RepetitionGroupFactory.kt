package be.bluexin.mcui.themes.serde.legacyformat.factory

import be.bluexin.mcui.themes.elements.RepetitionGroup
import be.bluexin.mcui.themes.miniscript.CInt
import be.bluexin.mcui.themes.miniscript.CResourceLocation
import be.bluexin.mcui.themes.serde.Factory
import be.bluexin.mcui.themes.serde.Factory.Context.Companion.tryRun
import be.bluexin.mcui.themes.serde.legacyformat.xml.RepetitionGroupXml
import org.koin.core.annotation.Single

@Single
internal class RepetitionGroupFactory(
    private val registry: LegacyFactoryRegistry
) : LegacyFactory<RepetitionGroupXml, RepetitionGroup>(RepetitionGroupXml::class) {

    override fun create(
        input: RepetitionGroupXml,
        context: Factory.Context
    ): Result<RepetitionGroup> = context.tryRun("repetitionGroup[${input.name}]") {
        val renderState = createRenderState(input, context)
        val transform = createTransform(input, context)
        val texture = input::texture.compileString(context)?.let(::CResourceLocation)
        val children = createChildren(input.children, context, registry)
        val amount = input::amount.compileInt(context) ?: CInt.ZERO

        RepetitionGroup(renderState, transform, children, amount, texture)
    }
}