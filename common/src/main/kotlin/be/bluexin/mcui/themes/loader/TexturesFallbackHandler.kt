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

package be.bluexin.mcui.themes.loader

import be.bluexin.mcui.Constants
import be.bluexin.mcui.GLCore
import be.bluexin.mcui.effects.StatusEffect
import be.bluexin.mcui.themes.meta.ThemeDefinition
import be.bluexin.mcui.util.append
import net.minecraft.resources.ResourceLocation
import org.koin.core.annotation.Single
import java.util.*

/**
 * Right now this only handles status effects, for compatibility purposes.
 */
@Single
class TexturesFallbackHandler {

    private fun logMissingAndUse(type: String, default: ResourceLocation, theme: ThemeDefinition): ResourceLocation {
        Constants.LOG.info("Theme {} missing custom {}, defaulting to SAO", theme.id, type)
        return default
    }

    fun init(theme: ThemeDefinition) {
        val textureRoot = theme.texturesRoot

        /*gui = GLCore.takeTextureIfExists(textureRoot.append("gui.png"))
            ?: logMissingAndUse("gui", defaultGui, theme)
        slot = GLCore.takeTextureIfExists(textureRoot.append("slot.png"))
            ?: logMissingAndUse("slot", defaultSlot, theme)
        entities = GLCore.takeTextureIfExists(textureRoot.append("entities.png"))
            ?: logMissingAndUse("entity health bars", defaultEntities, theme)
        particleLarge = GLCore.takeTextureIfExists(textureRoot.append("particlelarge.png"))
            ?: logMissingAndUse("death particles", defaultParticleLarge, theme)*/

        val missingEffects = StatusEffect.entries.filter {
            !GLCore.checkTexture(textureRoot.append("status_icons/${it.name.lowercase(Locale.getDefault())}.png"))
        }
        statusIcons = if (missingEffects.isEmpty()) {
            textureRoot.append("status_icons/")
        } else logMissingAndUse(
            "status icons ${missingEffects.map(StatusEffect::name)}",
            defaultStatusIcons, theme
        )

        /*val missingMenuIcons = buildList {
            IconCore.entries.forEach {
                it.rl = GLCore.takeTextureIfExists(textureRoot.append(it.path)) ?: run {
                    add(it.name)
                    defaultMenuIcons.append(it.path)
                }
            }
        }
        if (missingMenuIcons.isNotEmpty()) logMissingAndUse(
            "menu icons $missingMenuIcons",
            defaultStatusIcons, theme
        )*/
    }

    //    lateinit var gui: ResourceLocation
//    lateinit var slot: ResourceLocation
//    lateinit var entities: ResourceLocation
//    lateinit var particleLarge: ResourceLocation
    lateinit var statusIcons: ResourceLocation

    //    private val defaultGui = ResourceLocation(Constants.LEGACY_MOD_ID, "textures/sao/gui.png")
//    private val defaultSlot = ResourceLocation(Constants.LEGACY_MOD_ID, "textures/slot.png")
//    private val defaultEntities = ResourceLocation(Constants.LEGACY_MOD_ID, "textures/sao/entities.png")
//    private val defaultParticleLarge = ResourceLocation(Constants.LEGACY_MOD_ID, "textures/sao/particlelarge.png")
    private val defaultStatusIcons = ResourceLocation(Constants.LEGACY_MOD_ID, "textures/sao/status_icons/")
//    private val defaultMenuIcons = ResourceLocation(Constants.LEGACY_MOD_ID, "textures/sao/")
}
