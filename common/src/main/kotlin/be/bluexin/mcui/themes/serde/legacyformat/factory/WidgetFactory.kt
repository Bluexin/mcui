package be.bluexin.mcui.themes.serde.legacyformat.factory

import be.bluexin.mcui.themes.elements.Group
import be.bluexin.mcui.themes.serde.Factory
import be.bluexin.mcui.themes.serde.Factory.Context.Companion.tryRun
import be.bluexin.mcui.themes.serde.legacyformat.xml.WidgetXml
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Widgets render exactly like [Group] in the new tree - there is no dedicated widget *element* :
 * interaction (contentWidth/contentHeight, tooltip, active, onClick/onMouseOver/onLoseFocus) is
 * deferred until the Lua rework phase adds it to every element via a shared model.
 */
@Single
internal class WidgetFactory : LegacyFactory<WidgetXml, Group>(WidgetXml::class), KoinComponent {

    private val registry: LegacyFactoryRegistry by inject()

    override fun create(
        input: WidgetXml,
        context: Factory.Context
    ): Result<Group> = context.tryRun("widget[${input.name}]") {
        val renderState = createRenderState(input, context)
        val transform = createTransform(input, context)
        val texture = input::texture.compileTextureCompat(context)
        val children = createChildren(input.children, context, registry)

        Group(renderState, transform, children, texture)
    }
}