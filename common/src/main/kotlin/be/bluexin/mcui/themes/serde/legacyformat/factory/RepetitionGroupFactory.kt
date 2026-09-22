package be.bluexin.mcui.themes.serde.legacyformat.factory

import be.bluexin.mcui.themes.elements.RepetitionGroup
import be.bluexin.mcui.themes.miniscript.CInt
import be.bluexin.mcui.themes.miniscript.LibHelper
import be.bluexin.mcui.themes.miniscript.serialization.JelType
import be.bluexin.mcui.themes.serde.Factory
import be.bluexin.mcui.themes.serde.Factory.Context.Companion.tryRun
import be.bluexin.mcui.themes.serde.legacyformat.xml.RepetitionGroupXml
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Single
internal class RepetitionGroupFactory(
    private val libHelper: LibHelper,
) : LegacyFactory<RepetitionGroupXml, RepetitionGroup>(RepetitionGroupXml::class), KoinComponent {

    private val registry: LegacyFactoryRegistry by inject()

    override fun create(
        input: RepetitionGroupXml,
        context: Factory.Context
    ): Result<RepetitionGroup> = context.tryRun("repetitionGroup[${input.name}]") {
        val renderState = createRenderState(input, context)
        val transform = createTransform(input, context)
        val texture = input::texture.compileTextureCompat(context)
        val amount = input::amount.compileInt(context) ?: CInt.ZERO

        libHelper.pushContext(mapOf("i" to JelType.INT))
        val children = createChildren(input.children, context, registry)
        libHelper.popContext()

        RepetitionGroup(renderState, transform, children, amount, texture)
    }
}