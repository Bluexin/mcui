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

/**
 * Part of saoui
 * Fired when the SAO UI initialises its player stats retrieval fallback.
 *
 *
 * Temporary, future versions will (attempt to) fix inter-mod compatibilities.
 * Shouldn't be fired by other mods!! (unless you're really sure you know what you're doing)
 *
 *
 * Mods could use this event to get the same reference the SAO UI uses, to use the data however they want.
 *
 * @author Bluexin
 */
/*class EventInitStatsProvider(implementation: IPlayerStatsProvider) : Event() {
    private var implementation: IPlayerStatsProvider

    init {
        this.implementation = implementation
    }

    *//**
     * Gets the currently set implementation.
     *
     * @return current implementation
     *//*
    fun getImplementation(): IPlayerStatsProvider {
        return implementation
    }

    *//**
     * Sets the implementation to be used by the SAO UI to retrieve information from.
     *
     * @param provider an instance to be used to retrieve information from
     *//*
    fun setImplementation(provider: IPlayerStatsProvider) {
        implementation = provider
    }
}*/
