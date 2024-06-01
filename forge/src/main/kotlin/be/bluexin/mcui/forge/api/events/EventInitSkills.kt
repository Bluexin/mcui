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
package be.bluexin.mcui.forge.api.events
/*

import com.tencao.saoui.api.entity.ISkill
import net.minecraftforge.eventbus.api.Event

*/
/**
 * Part of saoui
 * Fired when the SAO UI initialises its Skill list.
 *
 *
 * Shouldn't be fired by other mods!! (unless you're really sure you know what you're doing)
 *
 *
 * Mods could use this event to get the same reference the SAO UI uses, to use the data however they want.
 *
 * @author Bluexin
 *//*

class EventInitSkills(skills: List<ISkill>) : Event() {
    private val skills: List<ISkill>

    */
/**
     * Gets whether skills can be shown on the Skills Ring around the player (instead of the hotbar to be shown there).
     *
     * @return whether skills can be shown on the Skills Ring
     *//*

    var isRingShown = false
        private set

    init {
        this.skills = skills
    }

    */
/**
     * Gets the current list of skills.
     * One can edit this list however he wants.
     *
     * @return the current list of skills to be registered for use in the SAO UI
     *//*

    fun getSkills(): List<ISkill> {
        return skills
    }

    */
/**
     * Enables or disables skills showing on the Skills Ring around the player (instead of the hotbar to be shown there).
     *
     * @param enabled whether to show the skills on the Skills Ring
     *//*

    fun setShowRing(enabled: Boolean) {
        isRingShown = enabled
    }
}
*/
