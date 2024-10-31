package be.bluexin.mcui.themes.miniscript.api

import be.bluexin.mcui.effects.StatusEffects
import be.bluexin.mcui.util.HealthStep
import net.minecraft.world.entity.player.Player
import java.lang.ref.WeakReference
import kotlin.math.min

/**
 * Not using Kotlin conventions (val player / fun getPlayer()) to simplify JEL access
 * as it doesn't support property access syntax
 */
@Suppress("unused", "MemberVisibilityCanBePrivate") // Exposed to JEL
interface MiniscriptPlayer {

    /**
     * @return the player's username
     */
    fun username(): String

    /**
     * @return the current hp of the player on a scale from 0.0 to 1.0
     */
    fun healthPercent(): Double = min(health() / maxHealth().toDouble(), 1.0)

    /**
     * @return the current hp of the player (1 heart = 2 HP)
     */
    fun health(): Float

    /**
     * @return the current maximum hp of the player (1 heart = 2 HP)
     */
    fun maxHealth(): Float

    /**
     * @return the current absorption amount the player has
     */
    fun absorption(): Float

    /**
     * @return the health step the player is currently at
     */
    fun healthStep(): HealthStep

    /**
     * @return the id of the hotbar slot the player is using (from 0 to 8)
     */
    fun selectedSlot(): Int

    /**
     * Used to know whether the specified offhand slot is empty.
     * Currently there is only 1 offhand slot.
     *
     * @param slot offhand slot to query
     * @return whether the specified offhand slot is empty
     */
    fun isOffhandEmpty(slot: Int): Boolean

    /**
     * @return the current experience level of the player
     */
    fun level(): Int

    /**
     * @return the current experience of the player on a scale from 0.0 to 1.0
     */
    fun experience(): Float

    fun statusEffects(): Array<StatusEffects>
}

internal class MiniscriptPlayerImplementation(
    player: Player
) : MiniscriptPlayer {
    private val playerRef = WeakReference(player)
    private val player = requireNotNull(playerRef.get()) { "Player reference was cleared" }

    override fun username(): String = player.displayName.string
    override fun health() = player.health
    override fun maxHealth() = player.maxHealth
    override fun absorption() = player.absorptionAmount

    override fun healthStep() = HealthStep.getStep(player, healthPercent())
    override fun selectedSlot() = player.inventory.selected
    override fun isOffhandEmpty(slot: Int) = player.inventory.offhand[slot].isEmpty
    override fun level() = player.experienceLevel
    override fun experience() = player.experienceProgress
    override fun statusEffects(): Array<StatusEffects> = StatusEffects.getEffects(player).toTypedArray()
}