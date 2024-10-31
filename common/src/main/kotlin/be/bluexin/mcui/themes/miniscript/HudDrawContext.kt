/*
 * Copyright (C) 2016-2024 Arnaud 'Bluexin' Solé
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package be.bluexin.mcui.themes.miniscript

import be.bluexin.mcui.deprecated.api.info.PlayerStatsProvider
import be.bluexin.mcui.deprecated.api.themes.IHudDrawContext
import be.bluexin.mcui.effects.StatusEffects
import be.bluexin.mcui.effects.StatusEffects.Companion.getEffects
import be.bluexin.mcui.social.StaticPlayerHelper.getHungerLevel
import be.bluexin.mcui.social.StaticPlayerHelper.getMaxHealth
import be.bluexin.mcui.themes.miniscript.*
import be.bluexin.mcui.util.Client
import be.bluexin.mcui.util.HealthStep
import be.bluexin.mcui.util.HealthStep.Companion.getStep
import be.bluexin.mcui.util.LayeredMap
import com.mojang.blaze3d.platform.Window
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.player.LocalPlayer
import net.minecraft.client.renderer.entity.ItemRenderer
import net.minecraft.util.profiling.ProfilerFiller
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import java.lang.ref.WeakReference
import kotlin.math.min

/**
 * Part of saoui by Bluexin.
 * Provides extra info for what's about to be drawn.
 *
 * @author Bluexin
 */
class HudDrawContext(
    val mc: Minecraft = Client.mc,
    private val itemRenderer: ItemRenderer = mc.itemRenderer
) : IHudDrawContext {
    /*
    Feel free to add anything you'd need here.
     */
    private val username: String get() = player.displayName.string
    private val usernameWidth: Double get() = mc.font.width(username).toDouble()
    private val stats: PlayerStatsProvider = object : PlayerStatsProvider {
        override fun getStatsString(player: Player): List<String> = emptyList()
    }
    private var healthStep: HealthStep? = null
    private var z = 0f
    private var hp = 0f
    private var maxHp = 0f
    private var scaledResolution: Window = mc.window
    private var partialTicks = 0f
    private var i = 0
//    private var pt: List<PlayerInfo> = listOf()
    private var effects: List<StatusEffects>? = null
    private var nearbyEntities: List<WeakReference<LivingEntity>> = listOf()
    private var targetEntity: WeakReference<LivingEntity>? = null
    private var lastTargetedTick = 0L

    /*fun setPt(pt: List<PlayerInfo>) {
        this.pt = pt
    }*/

    fun setTargetEntity(entity: LivingEntity?) {
        if (entity != null) {
            this.targetEntity = entity.let(::WeakReference)
            lastTargetedTick = entity.level.gameTime
        } else if (targetEntity != null && mc.level!!.gameTime - lastTargetedTick > 60) {
            targetEntity = null
        }
    }

    fun setNearbyEntities(entities: List<LivingEntity>) {
        this.nearbyEntities = entities.map(::WeakReference)
    }

    override fun username(): String {
        return username
    }

    override fun getZ(): Float {
        return z
    }

    fun setZ(z: Float) {
        this.z = z
    }

    override fun getFontRenderer(): Font {
        return mc.font
    }

    override fun usernamewidth(): Double {
        return usernameWidth
    }

    override fun hpPct(): Double {
        return min(hp / maxHp.toDouble(), 1.0)
    }

    override fun hp(): Float {
        return hp
    }

    override fun maxHp(): Float {
        return maxHp
    }

    /**
     * Aka partialTicks
     */
    fun setTime(time: Float) {
        hp = player.health//.getRenderData()!!.healthSmooth
        maxHp = getMaxHealth(player)
        healthStep = getStep(player, hpPct())
        partialTicks = time
        effects = getEffects(player)
    }

    override fun getPlayer(): Player = mc.player!!

    override fun getItemRenderer(): ItemRenderer {
        return itemRenderer
    }

    override fun healthStep(): HealthStep {
        return healthStep!!
    }

    override fun selectedslot(): Int {
        return player.inventory.selected
    }

    override fun scaledwidth(): Int {
        return scaledResolution.guiScaledWidth
    }

    override fun scaledheight(): Int {
        return scaledResolution.guiScaledHeight
    }

    fun setScaledResolution(scaledResolution: Window) {
        this.scaledResolution = scaledResolution
    }

    override fun getPartialTicks(): Float {
        return partialTicks
    }

    override fun offhandEmpty(slot: Int): Boolean {
        return player.inventory.offhand[slot].isEmpty
    }

    override fun strWidth(s: String): Int {
        return mc.font.width(s)
    }

    override fun strHeight(): Int {
        return mc.font.lineHeight
    }

    override fun absorption(): Float {
        return player.absorptionAmount
    }

    override fun level(): Int {
        return stats.getLevel(player)
    }

    override fun experience(): Float {
        return stats.getExpPct(player)
    }

    override fun horsejump(): Float {
        return (player as LocalPlayer).jumpRidingScale
    }

    fun getI(): Int {
        return i
    }

    override fun setI(i: Int) {
        this.i = i
    }

    override fun i(): Int {
        return getI()
    }

    override fun ptName(index: Int): String {
        return "" //pt[index].username
    }

    override fun ptHp(index: Int): Float {
        return 0f /*pt[index].let {
            it.player?.getRenderData()?.healthSmooth ?: it.health
        }*/
    }

    override fun ptMaxHp(index: Int): Float {
        return 0f //pt[index].maxHealth
    }

    override fun ptHpPct(index: Int): Float {
        return ptHp(index) / ptMaxHp(index)
    }

    override fun ptSize(): Int {
        return 0 //pt.size
    }

    override fun ptHealthStep(index: Int): HealthStep {
        return HealthStep.INVALID
        /*val ptMember = pt[index]
        return getStep(ptMember, ptHpPct(index).toDouble())*/
    }

    override fun foodLevel(): Float {
        return getHungerLevel(player, partialTicks)
    }

    override fun saturationLevel(): Float {
        return player.foodData.saturationLevel
    }

    override fun statusEffects(): List<StatusEffects> {
        return effects!!
    }

    override fun hasMount(): Boolean {
        return player.isPassenger
    }

    override fun mountHp(): Float {
        val t = player.vehicle
        return if (t is LivingEntity) {
            t.health
        } else 0f
    }

    override fun mountMaxHp(): Float {
        val t = player.vehicle
        return if (t is LivingEntity) {
            t.maxHealth
        } else 1f
    }

    override fun inWater(): Boolean {
        return player.isInWater
    }

    override fun air(): Int {
        return player.airSupply
    }

    override fun armor(): Int {
        return player.armorValue
    }

    override fun nearbyEntities(): List<LivingEntity> {
        return nearbyEntities.mapNotNull(WeakReference<LivingEntity>::get)
    }

    override fun entityName(index: Int): String? = nearbyEntities[index].get()?.displayName?.string

    override fun entityHp(index: Int): Float = nearbyEntities[index].get()?.health ?: 0F

    override fun entityMaxHp(index: Int): Float = nearbyEntities[index].get()?.maxHealth ?: 0F

    override fun entityHpPct(index: Int): Float = entityHp(index) / entityMaxHp(index)

    override fun entityHealthStep(index: Int): HealthStep = getStep(nearbyEntity(index), entityHpPct(index).toDouble())

    override fun targetEntity(): LivingEntity? = targetEntity?.get()

    override fun targetName(): String = targetEntity?.get()?.displayName?.string ?: ""

    override fun targetHp(): Float = targetEntity?.get()?.health ?: 0F

    override fun targetMaxHp(): Float = targetEntity?.get()?.maxHealth ?: 0F

    override fun targetHpPct(): Float = if (targetEntity?.get() != null) targetHp() / targetMaxHp() else 0f

    override fun targetHealthStep(): HealthStep = getStep(targetEntity?.get(), targetHpPct().toDouble())

    private var context = LayeredMap<String, CValue<*>>()

    override fun pushContext(context: Map<String, CValue<*>>) {
        this.context += context
    }

    override fun popContext() {
        this.context.pop()
    }

    // TODO: error report ?
    /**
     * JEL dynamic context access
     */
    override fun getStringProperty(name: String): String = when (val prop = context[name]) {
        null -> "<null>"
        is CString -> prop.invoke(this)
        else -> "<invalid type of $name>"
    }

    override fun getDoubleProperty(name: String): Double = when (val prop = context[name]) {
        null -> 0.0
        is CDouble -> prop.invoke(this)
        else -> -1.0
    }

    override fun getIntProperty(name: String): Int = when (val prop = context[name]) {
        null -> 0
        is CInt -> prop.invoke(this)
        else -> -1
    }

    override fun getBooleanProperty(name: String): Boolean = when (val prop = context[name]) {
        null -> false
        is CBoolean -> prop.invoke(this)
        else -> false
    }

    override fun getUnitProperty(name: String): Unit = when (val prop = context[name]) {
        null -> Unit
        is CUnit -> prop.invoke(this)
        else -> Unit
    }

    override fun getProfiler(): ProfilerFiller = mc.profiler
}

inline fun <T> IHudDrawContext.profile(key: String, body: () -> T): T {
    profiler.push(key)
    val ret = body()
    profiler.pop()
    return ret
}
