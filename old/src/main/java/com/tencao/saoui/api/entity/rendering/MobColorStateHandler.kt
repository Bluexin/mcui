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
package be.bluexin.mcui.api.entity.rendering

import be.bluexin.mcui.util.Client
import be.bluexin.mcui.util.getAttackClass
import net.minecraft.entity.EntityLiving
import net.minecraft.world.entity.LivingEntity
import net.minecraft.entity.IEntityOwnable
import net.minecraft.entity.ai.EntityAIAttackMelee
import net.minecraft.entity.ai.EntityAIAttackRanged
import net.minecraft.entity.ai.EntityAIFindEntityNearestPlayer
import net.minecraft.entity.ai.EntityAINearestAttackableTarget
import net.minecraft.entity.monster.EntityPigZombie
import net.minecraft.entity.monster.IMob
import net.minecraft.entity.passive.EntityWolf
import net.minecraft.entity.passive.IAnimals
import net.minecraft.world.entity.player.Player
import net.minecraftforge.common.util.FakePlayer
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.lang.ref.WeakReference

/**
 * Part of saoui
 *
 *
 * Default implementation for mobs.
 * This will be the most common implementation for mobs and NPCs.
 *
 * @author Bluexin
 */
class MobColorStateHandler internal constructor(entity: LivingEntity) :
    IColorStateHandler {
    private val theEnt: WeakReference<LivingEntity> = WeakReference(entity)

    /**
     * Caches value when it can (ie the value will never change again)
     */
    private var cached: ColorState? = null

    private val mc = Client.mc

    @get:SideOnly(Side.CLIENT)
    private val color: ColorState
        get() {
            val entity = theEnt.get() ?: return ColorState.INVALID
            if (entity.entityData.hasKey(ColorState.STATE_TAG)) {
                return ColorState.values().firstOrNull { it.name.equals(entity.entityData.getString(ColorState.STATE_TAG), true) } ?: ColorState.INVALID
            }
            if (cached != null) cached
            return when {
                !entity.isNonBoss -> ColorState.BOSS.also { cached = it }
                entity is Player && entity !is FakePlayer -> ColorState.INNOCENT.also { cached = it }
                entity is EntityWolf && entity.isAngry -> ColorState.KILLER
                entity is EntityPigZombie && entity.isAngry -> ColorState.KILLER
                entity is IEntityOwnable && entity.owner != null -> if (entity.owner == mc.player) ColorState.INNOCENT else ColorState.VIOLENT
                entity is IMob -> if (entity.canEntityBeSeen(mc.player) || entity.attackingEntity is Player) ColorState.KILLER else ColorState.VIOLENT
                entity is IAnimals -> ColorState.INNOCENT.also { cached = it }
                entity is EntityLiving -> if (entity.targetTasks.taskEntries.any { it is EntityAIAttackMelee || it is EntityAIAttackRanged || it is EntityAINearestAttackableTarget<*> && it.getAttackClass() is Player }) ColorState.KILLER else if (entity.targetTasks.taskEntries.any { it is EntityAIFindEntityNearestPlayer }) ColorState.VIOLENT else ColorState.INNOCENT
                else -> ColorState.INVALID
            }
        }

    /**
     * @return the color state the entity should be showing.
     */
    @SideOnly(Side.CLIENT)
    override fun getColorState(): ColorState {
        return color
    }
}
