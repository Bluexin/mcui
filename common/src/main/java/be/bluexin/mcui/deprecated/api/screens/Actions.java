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

package be.bluexin.mcui.deprecated.api.screens;


public enum Actions {

    UNKNOWN,

    LEFT_PRESSED,
    RIGHT_PRESSED,
    MIDDLE_PRESSED,

    LEFT_RELEASED,
    RIGHT_RELEASED,
    MIDDLE_RELEASED,

    KEY_TYPED,
    MOUSE_WHEEL;

    public static Actions getAction(int button, boolean pressed) {
        return button >= 0 && button <= 2 ? values()[button + (pressed ? LEFT_PRESSED.ordinal() : LEFT_RELEASED.ordinal())] : UNKNOWN;
    }

}
