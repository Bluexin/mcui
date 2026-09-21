package be.bluexin.mcui.themes.serde.legacyformat.factory

import be.bluexin.mcui.themes.elements.Fragment
import be.bluexin.mcui.themes.elements.Group
import be.bluexin.mcui.themes.miniscript.LibHelper
import be.bluexin.mcui.themes.serde.Factory
import be.bluexin.mcui.themes.serde.Factory.Context.Companion.tryRun
import be.bluexin.mcui.themes.serde.legacyformat.xml.FragmentXml
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Single
internal class FragmentFactory(
    private val libHelper: LibHelper,
) : LegacyFactory<FragmentXml, Fragment>(FragmentXml::class), KoinComponent {

    private val registry: LegacyFactoryRegistry by inject()

    override fun create(
        input: FragmentXml,
        context: Factory.Context
    ): Result<Fragment> = context.tryRun("fragment[${input.name}]") {
        val variables = input.expect?.variables.orEmpty()
        libHelper.pushContext(variables.mapValues { (_, value) -> value.type })

        val renderState = createRenderState(input, context)
        val transform = createTransform(input, context)
        val texture = input::texture.compileTextureCompat(context)
        val children = createChildren(input.children, context, registry)

        libHelper.popContext()

        Fragment(
            group = Group(renderState, transform, children, texture),
            expect = variables
        )
    }
}
