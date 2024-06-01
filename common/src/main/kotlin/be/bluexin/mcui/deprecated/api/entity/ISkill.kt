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

package be.bluexin.mcui.deprecated.api.entity
/*
import be.bluexin.mcui.api.events.EventInitSkills
import be.bluexin.mcui.api.screens.Actions
import be.bluexin.mcui.api.screens.IIcon
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiInventory

*//**
 * Part of saoui
 *
 *
 * Skill to be shown in the "Skills" menu and/or in the skills ring if set so.
 *
 * @author Bluexin
 *//*
interface ISkill {

    *//**
     * Used when this skill is clicked on (with any mouse button).
     *
     * @param mc     The Minecraft instance
     * @param parent The parent gui
     *//*
    fun activate(mc: Minecraft, parent: GuiInventory, action: Actions)

    *//**
     * Whether this skill should be visible to the player.
     *
     * @return should this skill be shown
     *//*
    fun visible(): Boolean {
        return true
    }

    *//**
     * Whether this skill's button should highlight or not.
     *
     * @return whether it should be highlighted
     *//*
    fun shouldHighlight(): Boolean

    *//**
     * Whether this skill should be shown on the "Skills" ring (around the character).
     * Only 12 skills can be shown on the ring.
     * For this to actually take effect, you need to specify it to [EventInitSkills]
     *
     * @return whether this skill should be listed on the "Skills" ring
     *//*
    fun shouldShowInRing(): Boolean {
        return true
    }

    *//**
     * Sets whether this skill should be shown on the "Skills" ring.
     * It is highly recommended to not silently ignore this.
     *//*
    fun setShowOnRing(showOnRing: Boolean)

    *//**
     * Gets an end-user-friendly name for this skill.
     * Will get localized if a key exists for it.
     *
     * @return a name for this skill to display to the end-user
     *//*
    override fun toString(): String

    *//**
     * Gets an icon to be displayed with this skill.
     *
     * @return icon representing this skill
     *//*
    val icon: IIcon
}*/
