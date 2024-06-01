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
package be.bluexin.mcui.deprecated.api.info

/**
 * Public interface for options.
 *
 * @author Bluexin
 */
interface IOption {
    /**
     * @return Returns true if the Option is selected/enabled
     */
    val isEnabled: Boolean

    /**
     * This checks if the Option is restricted or not.
     * Restricted Options can only have one option enabled
     * in their Category.
     *
     * @return Returns true if restricted
     */
    val isRestricted: Boolean

    /**
     * @return Returns the Category
     */
    val category: IOption?
}
