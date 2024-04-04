/*
 * Copyright (C) 2016-2019 Arnaud 'Bluexin' Solé
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

package be.bluexin.mcui.resources

import be.bluexin.mcui.Constants
import be.bluexin.mcui.GLCore
import be.bluexin.mcui.effects.StatusEffects
import be.bluexin.mcui.themes.meta.ThemeManager
import be.bluexin.mcui.util.IconCore
import be.bluexin.mcui.util.append
import net.minecraft.resources.ResourceLocation
import java.util.*

object StringNames {

    private fun logMissingAndUse(type: String, default: ResourceLocation): ResourceLocation {
        Constants.LOG.info("Theme {} missing custom {}, defaulting to SAO", ThemeManager.currentTheme, type)
        return default
    }

    fun init() {
        val textureRoot = ThemeManager.currentTheme.texturesRoot

        gui = GLCore.takeTextureIfExists(textureRoot.append("gui.png"))
            ?: logMissingAndUse("gui", defaultGui)
        slot = GLCore.takeTextureIfExists(textureRoot.append("slot.png"))
            ?: logMissingAndUse("slot", defaultSlot)
        entities = GLCore.takeTextureIfExists(textureRoot.append("entities.png"))
            ?: logMissingAndUse("entity health bars", defaultEntities)
        particleLarge = GLCore.takeTextureIfExists(textureRoot.append("particlelarge.png"))
            ?: logMissingAndUse("death particles", defaultParticleLarge)

        val missingEffects = StatusEffects.entries.filter {
            !GLCore.checkTexture(textureRoot.append("status_icons/${it.name.lowercase(Locale.getDefault())}.png"))
        }
        statusIcons = if (missingEffects.isEmpty()) {
            textureRoot.append("status_icons/")
        } else logMissingAndUse(
            "status icons ${missingEffects.map(StatusEffects::name)}",
            defaultStatusIcons
        )

        val missingMenuIcons = buildList {
            IconCore.entries.forEach {
                it.rl = GLCore.takeTextureIfExists(textureRoot.append(it.path)) ?: run {
                    add(it.name)
                    defaultMenuIcons.append(it.path)
                }
            }
        }
        if (missingMenuIcons.isNotEmpty()) logMissingAndUse(
            "menu icons $missingMenuIcons",
            defaultStatusIcons
        )
    }

    lateinit var gui: ResourceLocation
    lateinit var slot: ResourceLocation
    lateinit var entities: ResourceLocation
    lateinit var particleLarge: ResourceLocation
    lateinit var statusIcons: ResourceLocation

    private val defaultGui = ResourceLocation(Constants.MOD_ID, "textures/sao/gui.png")
    private val defaultSlot = ResourceLocation(Constants.MOD_ID, "textures/slot.png")
    private val defaultEntities = ResourceLocation(Constants.MOD_ID, "textures/sao/entities.png")
    private val defaultParticleLarge = ResourceLocation(Constants.MOD_ID, "textures/sao/particlelarge.png")
    private val defaultStatusIcons = ResourceLocation(Constants.MOD_ID, "textures/sao/status_icons/")
    private val defaultMenuIcons = ResourceLocation(Constants.MOD_ID, "textures/sao/")
}
