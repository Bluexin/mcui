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

package be.bluexin.mcui.social

import be.bluexin.mcui.config.OptionCore
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import java.util.*
import kotlin.math.max
import kotlin.math.roundToLong

/**
 * Part of SAOUI

 * @author Bluexin
 */
object StaticPlayerHelper {
    private const val HEALTH_ANIMATION_FACTOR = 0.075f
    private const val HEALTH_FRAME_FACTOR = HEALTH_ANIMATION_FACTOR * HEALTH_ANIMATION_FACTOR * 0x40f * 0x64f

    // private val healthSmooth = HashMap<UUID, Float>()
    private val hungerSmooth = HashMap<UUID, Float>()

    fun listOnlinePlayers(mc: Minecraft, range: Double): List<Player> {
//        return mc.level!!.getNearbyPlayers(TargetingConditions.forNonCombat(), mc.player!!, AABB.ofSize()) { p -> mc.player.getDistance(p!!) <= range }
        return mc.level!!.players().filter { mc.player!!.distanceToSqr(it) <= range * range }
    }

    private fun listOnlinePlayers(mc: Minecraft): List<Player> {
        return mc.level!!.players()
    }

    fun findOnlinePlayer(mc: Minecraft, username: String): Player? {
        return listOnlinePlayers(mc).find { it.name.string == username }
    }

    private fun isOnline(mc: Minecraft, names: Array<String>): BooleanArray { // TODO: update a boolean[] upon player join server? (/!\ client-side)
        val players = listOnlinePlayers(mc)
        val online = BooleanArray(names.size)

        for (i in names.indices) {
            online[i] = players.stream().anyMatch { player -> getName(player) == names[i] }
        }

        return online
    }

    fun isOnline(mc: Minecraft, name: String): Boolean {
        return isOnline(mc, arrayOf(name))[0]
    }

    fun getName(player: Player?): String {
        return if (player == null) "" else player.displayName.string
    }

    fun getName(mc: Minecraft): String {
        return getName(mc.player)
    }

    fun unformatName(name: String): String {
        var name = name
        var index = name.indexOf("�")

        while (index != -1) {
            name = if (index + 1 < name.length) {
                name.replace(name.substring(index, index + 2), "")
            } else {
                name.replace("�", "")
            }

            index = name.indexOf("�")
        }

        return name
    }

    /*
    fun getHealth(mc: Minecraft, entity: Entity?, time: Float): Float { // FIXME: this seems to break if called many times in a single render frame
        if (OptionCore.SMOOTH_HEALTH.isEnabled) {
            val healthReal: Float = if (entity is LivingEntity)
                entity.health
                else 0f
            val uuid = entity?.uniqueID

            if (uuid != null) {
                if (healthSmooth.containsKey(uuid)) {
                    var healthValue: Float = healthSmooth[uuid]!!
                    if (healthValue > healthReal) {
                        healthValue = healthReal
                        healthSmooth[uuid] = healthReal
                    }

                    if (healthReal <= 0 && entity is LivingEntity) {
                        val value = (18 - entity.deathTime).toFloat() / 18

                        if (value <= 0) healthSmooth.remove(uuid)

                        return max(0.0f, healthValue * value)
                    } else if ((healthValue * 10).roundToLong() != (healthReal * 10).roundToLong())
                        healthValue += (healthReal - healthValue) * (gameTimeDelay(mc, time) * HEALTH_ANIMATION_FACTOR)
                    else
                        healthValue = healthReal

                    healthSmooth[uuid] = healthValue
                    return max(0.0f, healthValue)
                } else {
                    healthSmooth[uuid] = healthReal
                    return max(0.0f, healthReal)
                }
            } else return healthReal
        } else
            return if (entity is LivingEntity) max(0.0f, entity.health) else 0f
    }*/

    fun getMaxHealth(entity: Entity?): Float {
        return if (entity is LivingEntity) max(0.0000001f, entity.maxHealth) else 1f
    }

    fun getHungerFract(mc: Minecraft, entity: Entity, time: Float) =
        if (entity !is Player) 1.0f
        else getHungerLevel(mc, entity, time) / 20.0f

    fun getHungerLevel(mc: Minecraft, entity: Entity, time: Float): Float {
        if (entity !is Player) return 1.0f
        val hungerReal: Float
        if (OptionCore.SMOOTH_HEALTH.isEnabled) {
            val uuid = entity.uuid

            hungerReal = entity.foodData.foodLevel.toFloat()

            if (hungerSmooth.containsKey(uuid)) {
                var hungerValue: Float = hungerSmooth[uuid]!!
                if (hungerValue > hungerReal) {
                    hungerValue = hungerReal
                    hungerSmooth[uuid] = hungerReal
                }

                when {
                    hungerReal <= 0 -> {
                        val value = (18 - entity.deathTime).toFloat() / 18
                        if (value <= 0) hungerSmooth.remove(uuid)

                        return hungerValue * value
                    }
                    (hungerValue * 10).roundToLong() != (hungerReal * 10).roundToLong() -> hungerValue += (hungerReal - hungerValue) * (gameTimeDelay(mc, time) * HEALTH_ANIMATION_FACTOR)
                    else -> hungerValue = hungerReal
                }

                hungerSmooth[uuid] = hungerValue
                return hungerValue
            } else {
                hungerSmooth[uuid] = hungerReal
                return hungerReal
            }
        } else return entity.foodData.foodLevel.toFloat()
    }

    private fun gameTimeDelay(mc: Minecraft, time: Float): Float {
        return if (time >= 0f) time else HEALTH_FRAME_FACTOR / mc.fps
    }

    fun isCreative(player: Player): Boolean { // TODO: test this!
        return player.isCreative
    }
}
