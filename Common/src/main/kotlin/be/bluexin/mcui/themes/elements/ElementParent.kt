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

package be.bluexin.mcui.themes.elements

import be.bluexin.mcui.api.themes.IHudDrawContext

/**
 * Part of saoui by Bluexin.

 * @author Bluexin
 */
interface ElementParent {
    fun getX(ctx: IHudDrawContext): Double

    fun getY(ctx: IHudDrawContext): Double

    fun getZ(ctx: IHudDrawContext): Double

    val name: String

    companion object {
        val ZERO = object : ElementParent {
            override fun getX(ctx: IHudDrawContext) = 0.0
            override fun getY(ctx: IHudDrawContext) = 0.0
            override fun getZ(ctx: IHudDrawContext) = 0.0
            override val name = "__ZERO"
        }
    }
}
