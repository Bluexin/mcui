package be.bluexin.mcui.themes.serde.legacyformat.factory

import be.bluexin.mcui.themes.elements.Group
import be.bluexin.mcui.themes.elements.Hud
import be.bluexin.mcui.themes.miniscript.CBoolean
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
        val parts = input.parts.parts.mapNotNull { entry ->
            val part = context.tryRun("part[${entry.key}]") {
                groupFactory.create(entry.value, context).getOrNull()
            }.getOrNull()

            if (part == null) {
                context.error("Failed to create HUD part ${entry.key}")
                return@mapNotNull null
            }

            entry.key to withJumpBarCompat(entry.key, part)
        }.toMap()

        Hud(
            name = input.name,
            parts = parts
        )
    }

    /**
     * Legacy-compat: the JUMP_BAR part is only displayed while the player is mounted.
     */
    private fun withJumpBarCompat(key: String, group: Group): Group =
        if (key == JUMP_BAR) {
            group.copy(renderState = group.renderState.copy(enabled = CBoolean { ctx ->
                ctx.player().hasMount() && group.renderState.enabled()
            }))
        } else group

    private companion object {
        const val JUMP_BAR = "JUMP_BAR"
    }
}
