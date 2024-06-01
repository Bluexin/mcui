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

package be.bluexin.mcui.themes.elements

/**
 * Part of saoui by Bluexin.
 *
 * @author Bluexin
 */
enum class HudPartType {

    /**
     * Key for the HudPart rendering the HP box on-screen.
     */
    HEALTH_BOX,

    /**
     * Key for the HudPart rendering the hotbar parts
     */
    HOTBAR,

    /**
     * Key for the HudPart rendering the XP box on-screen.
     */
    EXPERIENCE,

    /**
     * Key for the HudPart rendering the cross-hair.
     */
    CROSS_HAIR,

    /**
     * Key for the HudPart rendering the armor.
     */
    ARMOR,

    /**
     * Key for the HudPart rendering the horse jump bar.
     */
    JUMP_BAR,

    /**
     * Key for the HudPart rendering the am2 bars (not in use!).
     */
    AM2BARS,

    /**
     * Key for the HudPart rendering the party.
     */
    PARTY,

    /**
     * Key for the HudPart rendering food.
     */
    FOOD,

    /**
     * Key for the HudPart rendering potion effects.
     */
    EFFECTS,

    /**
     * Key for the HudPart rendering air bubbles.
     */
    AIR,

    /**
     * Key for the HudPart rendering mount health.
     */
    MOUNT_HEALTH,

    /**
     * Key for the HudPart rendering entity health on the hud.
     */
    ENTITY_HEALTH_HUD;
}
