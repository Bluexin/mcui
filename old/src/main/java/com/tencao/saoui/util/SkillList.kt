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

import be.bluexin.mcui.api.entity.ISkill
import be.bluexin.mcui.api.screens.Actions
import net.minecraft.Client.mc
import net.minecraft.client.gui.inventory.GuiInventory
import java.util.function.Consumer
import java.util.stream.Stream

/**
 * Part of saoui
 *
 * @author Bluexin
 */
class SkillList private constructor(skills: List<ISkill>, val isRingShown: Boolean) {

    private val skills = ArrayList<ISkill>(3)

    val isEmpty: Boolean
        get() = skills.isEmpty()

    init {
        this.skills.addAll(skills)
    }

    fun size(): Int {
        return skills.size
    }

    operator fun contains(o: ISkill): Boolean {
        return skills.contains(o)
    }

    operator fun iterator(): Iterator<ISkill> {
        return stream().iterator()
    }

    fun containsAll(c: Collection<*>): Boolean {
        return skills.containsAll(c)
    }

    fun indexOf(o: ISkill): Int {
        return skills.indexOf(o)
    }

    fun lastIndexOf(o: ISkill): Int {
        return skills.lastIndexOf(o)
    }

    fun stream(): Stream<ISkill> {
        return skills.stream().filter { it.visible() }
    }

    fun forEach(action: Consumer<in ISkill>) {
        stream().forEach(action)
    }

    fun hitInSkillRing(index: Int, mc: Minecraft, parent: GuiInventory, action: Actions) {
        instance()!!.stream().filter { it.shouldShowInRing() }.skip(index.toLong()).findFirst().ifPresent { s -> s.activate(mc, parent, action) }
    }

    companion object {
        private var instance: SkillList? = null

        fun init(skills: List<ISkill>, showRing: Boolean) {
            check(instance == null) { "SkillList already got initialized!" }
            instance = SkillList(skills, showRing)
        }

        fun instance(): SkillList? {
            return instance
        }
    }
}
