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

package be.bluexin.mcui

import be.bluexin.mcui.util.Client
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.entity.Entity
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvent
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*

enum class SoundCore {

    CONFIRM,
    DIALOG_CLOSE,
    MENU_POPUP,
    MESSAGE,
    ORB_DROPDOWN,
    PARTICLES_DEATH;

    val sound = SoundEvent(ResourceLocation(Constants.MOD_ID, name.lowercase(Locale.getDefault())))

    companion object {
        @SubscribeEvent
        fun registerSoundEvent(event: RegistryEvent.Register<SoundEvent>) {
            values().forEach { event.registry.register(it.sound.setRegistryName(it.name.lowercase(Locale.getDefault()))) }
        }
    }
}

fun SoundCore.play() = Client.mc.soundHandler.playSound(PositionedSoundRecord.getMasterRecord(this.sound, 1.0f))
fun SoundCore.playAtEntity(ent: Entity) = ent.world.playSound(ent.posX, ent.posY, ent.posZ, this.sound, SoundCategory.AMBIENT, 1.0f, 1.0f, true)
