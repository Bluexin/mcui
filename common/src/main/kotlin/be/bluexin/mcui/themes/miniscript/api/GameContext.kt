package be.bluexin.mcui.themes.miniscript.api

import be.bluexin.luajksp.annotations.LuajExclude
import be.bluexin.luajksp.annotations.LuajExpose

/**
 * Not using Kotlin conventions (val player / fun getPlayer()) to simplify JEL access
 * as it doesn't support property access syntax
 */
@Suppress("unused", "MemberVisibilityCanBePrivate") // Exposed to JEL
@LuajExpose
interface GameContext {

    /**
     * @return the current player
     */
    fun player(): MiniscriptPlayer

    /**
     * @return the list of members in the player's party, excluding the player
     * Not yet implemented.
     */
    fun party(): List<MiniscriptPlayer>

    /**
     * @return the [MiniscriptPlayer] at [index] in the party, or null if [index] is out of bounds.
     * Not yet implemented.
     */
    fun partyMember(index: Int): MiniscriptPlayer? = party().getOrNull(index)

    /**
     * @return access to the theme settings
     */
    @LuajExclude // more access is given to Lua via SettingsLib
    fun settings(): MiniscriptSettings

    /**
     * @return information about the game window
     */
    fun gameWindowInfo(): GameWindowInfo

    fun currentlyOpenContainer(): MiniscriptContainer?
}