package be.bluexin.mcui.themes.serde.legacyformat.factory

import be.bluexin.mcui.themes.elements.Fragment
import be.bluexin.mcui.themes.elements.Group
import be.bluexin.mcui.themes.miniscript.CResourceLocation
import be.bluexin.mcui.themes.miniscript.LibHelper
import be.bluexin.mcui.themes.serde.Factory
import be.bluexin.mcui.themes.serde.Factory.Context.Companion.tryRun
import be.bluexin.mcui.themes.serde.legacyformat.dto.ElementXml
import be.bluexin.mcui.themes.serde.legacyformat.dto.FragmentXml
import org.koin.core.annotation.Single

@Single
internal class FragmentFactory(
    private val registry: LegacyFactoryRegistry,
    private val libHelper: LibHelper,
) : LegacyFactory<FragmentXml, Fragment>(FragmentXml::class) {

    override fun create(
        input: FragmentXml,
        context: Factory.Context
    ): Result<Fragment> = context.tryRun("fragment[${input.name}]") {
        val variables = input.expect?.variables.orEmpty()
        libHelper.pushContext(variables.mapValues { (_, value) -> value.type })

        val renderState = createRenderState(input, context)
        val transform = createTransform(input, context)
        val texture = input::texture.compileString(context)?.let(::CResourceLocation)

        // Recursively create child elements
        val children = input.children?.elements?.mapNotNull { childXml ->
            val childName = (childXml as? ElementXml.WithRenderState)?.name ?: "unknown"
            context.nested(childName)
            val result = registry.createFromXml(childXml, context)
            context.pop()

            if (result.isFailure) {
                context.error("Failed to create child element: ${result.exceptionOrNull()?.message}")
            }
            result.getOrNull()
        } ?: emptyList()

        libHelper.popContext()

        Fragment(
            group = Group(renderState, transform, children, texture),
            expect = variables
        )
    }
}
