package be.bluexin.mcui.themes.serde.legacyformat.factory

import be.bluexin.mcui.themes.elements.*
import be.bluexin.mcui.themes.miniscript.CBoolean
import be.bluexin.mcui.themes.miniscript.CDouble
import be.bluexin.mcui.themes.miniscript.CInt
import be.bluexin.mcui.themes.miniscript.CResourceLocation
import be.bluexin.mcui.themes.serde.Factory
import be.bluexin.mcui.themes.serde.Factory.Context.Companion.tryRun
import be.bluexin.mcui.themes.serde.legacyformat.dto.HotbarItemXml
import org.koin.core.annotation.Single

@Single
internal class HotbarItemFactory : LegacyFactory<HotbarItemXml, Group>(HotbarItemXml::class) {
    override fun create(
        input: HotbarItemXml,
        context: Factory.Context
    ): Result<Group> = context.tryRun("glHotbarItem[${input.name}]") {
        val baseEnabled = input::enabled.compileBoolean(context) ?: CBoolean.TRUE

        // Apply hand filter to enabled state for legacy compatibility
        val enabledBasedOnMainHand = input.hand?.let { hand ->
            CBoolean { baseEnabled() && hand != it.player().mainArm() }
        } ?: baseEnabled

        val renderState = RenderState(
            enabled = enabledBasedOnMainHand,
            name = input.name,
        )

        val transform = createTransform(input, context)

        // Create background rectangle from GLRectangleParent properties
        val width = input::w.compileDouble(context) ?: CDouble.ZERO
        val height = input::h.compileDouble(context) ?: CDouble.ZERO
        val texture = input::texture.compileString(context)

        val background = Rectangle(
            renderState = RenderState(
                enabled = CBoolean.TRUE,
                name = "${input.name}_bg"
            ),
            transform = Transform.ZERO,
            geometry = Rectangle.Geometry(
                rgba = input::rgba.compileInt(context),
                width = width,
                height = height,
                texture = texture?.let(::CResourceLocation),
                sourceX = input::srcX.compileDouble(context) ?: CDouble.ZERO,
                sourceY = input::srcY.compileDouble(context) ?: CDouble.ZERO,
                sourceWidth = input::srcW.compileDouble(context) ?: width,
                sourceHeight = input::srcH.compileDouble(context) ?: height,
                textureWidth = CInt { 256 },
                textureHeight = CInt { 256 },
            )
        )

        // Create item slot
        val slot = input::slot.compileInt(context) ?: CInt.ZERO
        val itemXoffset = input::itemXoffset.compileInt(context) ?: CInt.ZERO
        val itemYoffset = input::itemYoffset.compileInt(context) ?: CInt.ZERO

        val itemStackDisplay = ItemStackDisplay(
            renderState = RenderState(
                enabled = CBoolean.TRUE,
                name = "${input.name}_item"
            ),
            transform = Transform(
                x = CDouble { itemXoffset().toDouble() },
                y = CDouble { itemYoffset().toDouble() },
                z = CDouble.ZERO,
                scale = null
            ),
            properties = ItemStackDisplay.ItemSlotProperties(
                slotIndex = slot,
                inventorySource = { gameContext, slotIndex ->
                    // Legacy: hand == null means hotbar, hand != null means offhand
                    if (input.hand == null) {
                        gameContext.player().inventory().hotbarItem(slotIndex)
                    } else {
                        gameContext.player().inventory().offHandItem(slotIndex)
                    }
                }
            )
        )

        Group(
            renderState = renderState,
            transform = transform,
            children = listOf(background, itemStackDisplay),
            texture = null,
        )
    }
}
