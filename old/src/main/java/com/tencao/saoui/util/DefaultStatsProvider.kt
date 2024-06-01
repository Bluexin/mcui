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

import com.tencao.saomclib.isNotEmpty
import be.bluexin.mcui.api.info.IPlayerStatsProvider
import be.bluexin.mcui.config.OptionCore
import net.minecraft.client.resources.I18n
import net.minecraft.world.entity.LivingEntity
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.ai.attributes.AttributeModifier
import net.minecraft.entity.passive.EntityHorse
import net.minecraft.world.entity.player.Player
import net.minecraft.inventory.EntityEquipmentSlot
import java.text.DecimalFormat

/**
 * Part of saoui

 * @author Bluexin
 */
class DefaultStatsProvider : IPlayerStatsProvider {

    private fun attr(attributeValue: Double): Float {
        return (attributeValue * 1000).toInt().toFloat() / 1000
    }

    override fun getStatsString(player: Player): List<String> {
        val builder = mutableListOf<String>()
        val mount = player.ridingEntity as LivingEntity?

        if (player.isRiding && OptionCore.MOUNT_STAT_VIEW.isEnabled) {
            val name = mount!!.name
            val maxHealth = attr(mount.maxHealth.toDouble()).toDouble()
            var health = attr(mount.health.toDouble()).toDouble()
            val resistance = attr(mount.totalArmorValue.toDouble()).toDouble()
            val speed = attr(mount.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).attributeValue).toDouble()
            val jump: Double
            val df3 = DecimalFormat("0.000")
            val df1 = DecimalFormat("0.0")
            val speedFormated = df3.format(speed)
            health *= 10.0
            health += 0.5
            health /= 10.0
            val healthFormated = df1.format(health)

            builder.add(I18n.get("displayName") + ": $name")
            builder.add(I18n.get("displayHpLong") + ": $healthFormated/$maxHealth")
            builder.add(I18n.get("displayResLong") + ": $resistance")
            builder.add(I18n.get("displaySpdLong") + ": $speedFormated")
            if (mount is EntityHorse) {
                jump = mount.horseJumpStrength
                val jumpFormated = df3.format(jump)
                builder.add(I18n.get("displayJmpLong") + ": $jumpFormated")
            }
        } else {
            val level = PlayerStats.instance().stats.getLevel(player)
            val experience = (PlayerStats.instance().stats.getExpPct(player) * 100).toInt()

            val health = attr(player.health.toDouble())

            val maxHealth = attr(player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).attributeValue)
            val attackDamage = attr(player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).attributeValue)
            // final float movementSpeed = attr(player.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue());
            // final float knocbackResistance = attr(player.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).getAttributeValue());

            var itemDamage = 0.0f

            if (player.heldItemMainhand.isNotEmpty) {
                val itemAttackMain = player.heldItemMainhand.getAttributeModifiers(EntityEquipmentSlot.MAINHAND)
                    .get(SharedMonsterAttributes.ATTACK_DAMAGE.name)

                itemDamage += itemAttackMain.filterIsInstance<AttributeModifier>().map { value -> value }
                    .filter { mod -> mod.name == "Weapon modifier" }.sumOf { it.amount }.toFloat()
            }
            if (player.heldItemOffhand.isNotEmpty) {
                val itemAttackOff = player.heldItemOffhand.getAttributeModifiers(EntityEquipmentSlot.OFFHAND)
                    .get(SharedMonsterAttributes.ATTACK_DAMAGE.name)

                itemDamage += itemAttackOff.filterIsInstance<AttributeModifier>().map { value -> value }
                    .filter { mod -> mod.name == "Weapon modifier" }.sumOf { it.amount }.toFloat()
            }

            val strength = attr((attackDamage + itemDamage).toDouble())
            val agility = attr(player.aiMoveSpeed.toDouble()) * 10
            val resistance = attr(player.totalArmorValue.toDouble())

            builder.add(I18n.get("displayLvLong") + ": $level")
            builder.add(I18n.get("displayXpLong") + ": $experience")
            builder.add(I18n.get("displayHpLong") + ": $health/$maxHealth")
            builder.add(I18n.get("displayStrLong") + ": $strength")
            builder.add(I18n.get("displayDexLong") + ": $agility")
            builder.add(I18n.get("displayResLong") + ": $resistance")
        }

        return builder
    }
}
