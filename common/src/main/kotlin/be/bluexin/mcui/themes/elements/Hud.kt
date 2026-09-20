package be.bluexin.mcui.themes.elements

import be.bluexin.luajksp.annotations.LuajExclude
import be.bluexin.luajksp.annotations.LuajExpose
import be.bluexin.mcui.themes.elements.access.HudAccess
import be.bluexin.mcui.themes.elements.visitor.ElementVisitor
import org.luaj.vm2.LuaValue

/**
 * Top-level HUD container over named parts.
 *
 * Like [Group], the container is itself an [Element] whose [visit] traverses its parts, so the whole
 * tree is uniformly visitable (rendering, interaction, Lua) - entry points are just
 * `visitor.visit(hud, ...)`. Parts are visited in declared order, keyed by their String slot key
 * (e.g. `"HOTBAR"`), matching the legacy `Hud.drawAll` profiler entries.
 */
@LuajExpose
data class Hud(
    val name: String,
    // Lua access classes support Lists but not Maps : parts (and the keyed `get`) stay Kotlin-side
    @LuajExclude
    val parts: Map<String, Group>,
) : Element {

    override val transform: Transform = Transform.ZERO

    @LuajExclude
    override fun visit(visitor: ElementVisitor, context: ElementVisitor.Context) {
        context.profile(name) {
            parts.forEach { (key, part) ->
                context.profile(key) { visitor.visit(part, context) }
            }
        }
    }

    @LuajExclude
    override fun toLua(): LuaValue = HudAccess(this)

    operator fun get(key: String): Group? = parts[key]
}