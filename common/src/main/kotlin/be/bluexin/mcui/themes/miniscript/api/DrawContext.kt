package be.bluexin.mcui.themes.miniscript.api

/**
 * Not using Kotlin conventions (val player / fun getPlayer()) to simplify JEL access
 * as it doesn't support property access syntax
 */
@Suppress("unused", "MemberVisibilityCanBePrivate") // Exposed to JEL
interface DrawContext {

    fun player(): MiniscriptPlayer

    /**
     * Not yet implemented
     */
    fun party(): Array<MiniscriptPlayer> = emptyArray()

    fun settings(): MiniscriptSettings
}