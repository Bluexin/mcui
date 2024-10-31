package be.bluexin.mcui.themes.miniscript.api

/**
 * Not using Kotlin conventions (val player / fun getPlayer()) to simplify JEL access
 * as it doesn't support property access syntax
 */
@Suppress("unused", "MemberVisibilityCanBePrivate") // Exposed to JEL
interface DrawContext {

    /**
     * @return the current player
     */
    fun player(): MiniscriptPlayer

    /**
     * Not yet implemented
     */
    fun party(): List<MiniscriptPlayer> = emptyList()

    /**
     * @return access to the theme settings
     */
    fun settings(): MiniscriptSettings
}