package be.bluexin.mcui.themes.miniscript.api

import be.bluexin.luajksp.annotations.LuajExclude
import be.bluexin.luajksp.annotations.LuajExpose
import be.bluexin.mcui.effects.StatusEffect
import be.bluexin.mcui.social.StaticPlayerHelper.getHungerLevel
import be.bluexin.mcui.themes.miniscript.FrameCachedExpression
import be.bluexin.mcui.themes.miniscript.PartialTicksTracker
import be.bluexin.mcui.themes.miniscript.api.access.MiniscriptPlayerAccess
import be.bluexin.mcui.themes.serde.legacyformat.dto.AnonymousExpressionIntermediate
import be.bluexin.mcui.util.HealthStep
import net.minecraft.client.player.LocalPlayer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.luaj.vm2.LuaValue
import java.lang.ref.WeakReference
import kotlin.math.min

/**
 * Not using Kotlin conventions (val player / fun getPlayer()) to simplify JEL access
 * as it doesn't support property access syntax
 */
@Suppress("unused", "MemberVisibilityCanBePrivate") // Exposed to JEL
@LuajExpose
interface MiniscriptPlayer : MiniscriptLivingEntity {

    /**
     * @return the current absorption amount the player has
     */
    fun absorption(): Float

    /**
     * @return the health step the player is currently at
     */
    @LuajExclude // TODO : support enums un LuajKSP
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

    /**
     * @return the player's current status effects
     */
    @LuajExclude // TODO : support enums un LuajKSP
    fun statusEffects(): List<StatusEffect>

    /**
     * @return the player's current status effect at given [index], or null if [index] is out of bounds
     */
    @LuajExclude // TODO : support enums un LuajKSP
    fun statusEffect(index: Int): StatusEffect? = statusEffects().getOrNull(index)

    /**
     * @return the player's food value
     */
    fun food(): Float

    /**
     * @return the player's max food value
     */
    fun maxFood(): Float = 20.0f

    /**
     * @return the player's food on a scale from 0.0 to 1.0
     */
    fun foodPercent(): Float = min((food() / maxFood()).toDouble(), 1.0).toFloat()

    /**
     * @return the player's saturation value
     */
    fun saturation(): Float

    /**
     * @return the player's max saturation value
     */
    fun maxSaturation(): Float = 20.0f

    /**
     * @return player saturation on a scale from 0.0 to 1.0
     */
    fun saturationPercent(): Float = min((saturation() / maxSaturation()).toDouble(), 1.0).toFloat()

    /**
     * @return horse jump value on a scale from 0.0 to 1.0
     */
    fun horseJump(): Float

    fun inventory(): MiniscriptPlayerInventory

    /**
     * @return the player's main hand
     */
    @LuajExclude // TODO : support enums in LuajKSP
    fun mainArm(): net.minecraft.world.entity.HumanoidArm

    @LuajExclude
    override fun toLua(): LuaValue = MiniscriptPlayerAccess(this)
}

internal class MiniscriptPlayerImpl(
    player: LocalPlayer,
) : MiniscriptPlayer, MiniscriptLivingEntity by MiniscriptLivingEntityImpl(player), KoinComponent {

    private val playerRef = WeakReference(player)
    internal val player = requireNotNull(playerRef.get()) { "Player reference was cleared" }
    private val partialTicksTracker by inject<PartialTicksTracker>()
    private val context by inject<GameContext>()

    override fun absorption() = player.absorptionAmount

    override fun healthStep() = HealthStep.getStep(player, healthPercent())
    override fun selectedSlot() = player.inventory.selected
    override fun isOffhandEmpty(slot: Int) = player.inventory.offhand[slot].isEmpty
    override fun level() = player.experienceLevel
    override fun experience() = player.experienceProgress

    private val statusEffects = FrameCachedExpression(AnonymousExpressionIntermediate.EMPTY) {
        StatusEffect.getEffects(player)
    }

    override fun statusEffects(): List<StatusEffect> = statusEffects.invoke(context)

    override fun food() = getHungerLevel(player, partialTicksTracker.partialTicks)
    override fun saturation() = player.foodData.saturationLevel

    override fun horseJump(): Float = player.jumpRidingScale

    override fun inventory() = MiniscriptPlayerInventoryImpl(player.inventory)

    override fun mainArm() = player.mainArm

    override fun toLua() = super<MiniscriptPlayer>.toLua()
}