package be.bluexin.mcui.themes.serde.legacyformat.factory

import be.bluexin.mcui.themes.elements.Hud
import be.bluexin.mcui.themes.serde.Factory
import be.bluexin.mcui.themes.serde.Factory.Context.Companion.tryRun
import be.bluexin.mcui.themes.serde.legacyformat.xml.HudXml
import org.koin.core.annotation.Single

@Single
internal class HudFactory(
    private val groupFactory: GroupFactory
) : Factory<HudXml, Hud> {

    override fun create(
        input: HudXml,
        context: Factory.Context
    ): Result<Hud> = context.tryRun("hud[${input.name}]") {
        // Convert each HUD part (GroupXml) to Group element
        val parts = input.parts.parts.associate { entry ->
            val groupResult = context.tryRun("part[${entry.key}]") {
                groupFactory.create(entry.value, context).getOrThrow()
            }

            if (groupResult.isFailure) {
                context.error("Failed to create HUD part ${entry.key}: ${groupResult.exceptionOrNull()?.message}")
            }

            entry.key to groupResult.getOrThrow()
        }

        Hud(
            name = input.name,
            version = input.version,
            parts = parts
        )
    }
}
