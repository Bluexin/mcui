package be.bluexin.mcui.themes.serde.legacyformat.factory

import be.bluexin.mcui.themes.elements.Group
import be.bluexin.mcui.themes.serde.Factory
import be.bluexin.mcui.themes.serde.Factory.Context.Companion.tryRun
import be.bluexin.mcui.themes.serde.legacyformat.xml.GroupXml
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Single
internal class GroupFactory : LegacyFactory<GroupXml, Group>(GroupXml::class), KoinComponent {

    private val registry: LegacyFactoryRegistry by inject()

    override fun create(
        input: GroupXml,
        context: Factory.Context
    ): Result<Group> = context.tryRun("elementGroup[${input.name}]") {
        val renderState = createRenderState(input, context)
        val transform = createTransform(input, context)
        val texture = input::texture.compileTextureCompat(context)
        val children = createChildren(input.children, context, registry)

        Group(renderState, transform, children, texture)
    }
}
