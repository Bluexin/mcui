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

import be.bluexin.mcui.deprecated.api.info.PlayerStatsProvider

/**
 * Part of saoui

 * @author Bluexin
 */
class PlayerStats private constructor(val stats: PlayerStatsProvider) {
    companion object {
        private var instance: PlayerStats? = null

        fun init(provider: PlayerStatsProvider) {
            check(instance == null) { "PlayerStats already initialized!" }
            instance = PlayerStats(provider)
        }

        fun instance(): PlayerStats {
            checkNotNull(instance) { "PlayerStats not initialized!" }
            return instance!!
        }
    }
}
