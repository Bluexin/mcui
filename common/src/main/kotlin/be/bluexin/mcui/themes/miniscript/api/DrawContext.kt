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
     * @return the list of members in the player's party, excluding the player
     * Not yet implemented.
     */
    fun party(): List<MiniscriptPlayer> = emptyList()

    /**
     * @return the [MiniscriptPlayer] at [index] in the party, or null if [index] is out of bounds.
     * Not yet implemented.
     */
    fun party(index: Int): MiniscriptPlayer? = party().getOrNull(index)

    /**
     * @return access to the theme settings
     */
    fun settings(): MiniscriptSettings

    /**
     * @return information about the game window
     */
    fun gameWindowInfo(): GameWindowInfo
}