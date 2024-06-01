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

package be.bluexin.mcui.util

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player


enum class HealthStep(private val limit: Float, var rgba: Int) {

    VERY_LOW(0.1f, 0xBD0000FF.toInt()),
    LOW(0.2f, 0xF40000FF.toInt()),
    VERY_DAMAGED(0.3f, 0xF47800FF.toInt()),
    DAMAGED(0.4f, 0xF4BD00FF.toInt()),
    OKAY(0.5f, 0xEDEB38FF.toInt()),
    GOOD(1.0f, 0x93F43EFF.toInt()),
    CREATIVE(-1.0f, 0xBBF6F3FF.toInt()),
    DEV(-1.0f, 0xB32DE3FF.toInt()),
    INVALID(-1.0f, 0x8B8B8BFF.toInt());

    fun rgba() = rgba

    private operator fun next(): HealthStep {
        return entries[ordinal + 1]
    }

    fun glColor() {
//        GLCore.color(rgba)
    }

    companion object {

        fun getStep(entity: LivingEntity): HealthStep {
//            return getStep(entity, (entity.getRenderData()?.healthSmooth ?: entity.health) / StaticPlayerHelper.getMaxHealth(entity).toDouble())
            return getStep(entity, entity.health / entity.maxHealth.toDouble())
        }

        fun getStep(entity: LivingEntity?, health: Double): HealthStep = when (entity) {
            null -> INVALID
            is Player -> {
                val gameMode = Client.mc.connection?.getPlayerInfo(entity.gameProfile.id)?.gameMode
                when {
                    gameMode == null -> INVALID
                    gameMode.isCreative -> CREATIVE
                    gameMode.isSurvival -> getStep(health)
                    else -> INVALID
                }
            }
            else -> getStep(health)
        }

        // TODO : depends on saomclib
        /*fun getStep(entity: PlayerInfo, health: Double): HealthStep {
            var state = GOOD
            if (entity.player == null) state = INVALID
            else Client.mc.connection?.playerInfoMap?.firstOrNull { it.gameProfile.id == entity.uuid }?.gameType?.let {
                if (it.isCreative) {
                    state = CREATIVE
                } else if (!it.isSurvivalOrAdventure) {
                    state = INVALID
                }
            } ?: let { state = INVALID }

            return if (state != GOOD) state
            else getStep(health)
        }*/

        fun getStep(health: Double): HealthStep {
            var step = VERY_LOW
            while (health > step.limit && step.ordinal + 1 < maxStepIndex) step = step.next()
            return if (step == CREATIVE) GOOD else step
        }

        private val maxStepIndex = entries.size
    }
}
