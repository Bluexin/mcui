package be.bluexin.mcui.themes.serde.legacyformat.factory

import be.bluexin.mcui.themes.elements.Group
import be.bluexin.mcui.themes.miniscript.CResourceLocation
import be.bluexin.mcui.themes.serde.Factory
import be.bluexin.mcui.themes.serde.Factory.Context.Companion.tryRun
import be.bluexin.mcui.themes.serde.legacyformat.dto.ElementGroupXml
import be.bluexin.mcui.themes.serde.legacyformat.dto.ElementXml
import org.koin.core.annotation.Single

@Single
internal class ElementGroupFactory(
    private val registry: LegacyFactoryRegistry
) : LegacyFactory<ElementGroupXml, Group>(ElementGroupXml::class) {

    override fun create(
        input: ElementGroupXml,
        context: Factory.Context
    ): Result<Group> = context.tryRun("elementGroup[${input.name}]") {
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

        Group(renderState, transform, children, texture)
    }
}
