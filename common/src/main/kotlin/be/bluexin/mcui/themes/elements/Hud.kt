package be.bluexin.mcui.themes.elements

import be.bluexin.luajksp.annotations.LuajExclude
import be.bluexin.luajksp.annotations.LuajExpose
import be.bluexin.mcui.themes.elements.access.HudAccess
import be.bluexin.mcui.themes.elements.legacy.HudPartType
import org.luaj.vm2.LuaValue

/**
 * HUD element containing multiple named parts (health, hotbar, etc.)
 * Each part is a Group that can be rendered independently.
 */
@LuajExpose
data class Hud(
    val name: String,
    val version: String,
    @LuajExclude
    val parts: Map<HudPartType, Group>,
) : Element {

    @LuajExclude
    override val transform: Transform = Transform.ZERO

    @LuajExclude // FIXME : should add support in luaj-ksp
    operator fun get(key: HudPartType): Group? = parts[key]

    @LuajExclude
    override fun visit(visitor: ElementVisitor, context: ElementVisitor.Context) {
        // HUD itself doesn't transform or render - it delegates to its parts
        // Rendering individual parts is handled by external code calling visitor.visit(hud[key], context)
    }

    @LuajExclude
    override fun toLua(): LuaValue = HudAccess(this)
}
