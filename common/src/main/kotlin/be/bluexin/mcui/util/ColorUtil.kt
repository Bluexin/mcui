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

enum class ColorUtil(var rgba: Int) {
    DEFAULT_COLOR(0xFFFFFFFF.toInt()),
    DEFAULT_FONT_COLOR(0x1B1B1BFF),
    DEFAULT_BOX_COLOR(0xBBBBBBFF.toInt()),
    DEFAULT_BOX_FONT_COLOR(0x555555FF),

    HOVER_COLOR(0xC99B13FF.toInt()),
    HOVER_FONT_COLOR(0xFFFFFFFF.toInt()),

    DISABLED_COLOR(0x7C7C7CFF),
    DISABLED_FONT_COLOR(0xFFFFFFFF.toInt()),

    CONFIRM_COLOR(0x4782E3FF),
    CONFIRM_COLOR_LIGHT(0x629DFFFF),

    CANCEL_COLOR(0xE34747FF.toInt()),
    CANCEL_COLOR_LIGHT(0xFF6262FF.toInt()),

    CURSOR_COLOR(0x8EE1E8),

    DEAD_COLOR(0xC94141FF.toInt()),
    HARDCORE_DEAD_COLOR(0x990000FF.toInt());

    fun multiplyAlpha(alpha: Float): Int {
        return multiplyAlpha(this.rgba, alpha)
    }

    fun mediumColor(color: ColorUtil): Int {
        return this.mediumColor(color.rgba)
    }

    fun mediumColor(rgba: Int): Int {
        return mediumColor(this.rgba, rgba)
    }

    companion object {

        fun multiplyAlpha(rgba: Int, alpha: Float): Int {
            val value = ((rgba and 0xFF) * alpha).toInt()

            return rgba and 0xFFFFFF00.toInt() or (value and 0xFF)
        }

        fun mediumColor(rgba0: Int, rgba1: Int): Int {
            return ((rgba0 shr 24 and 0xFF) + (rgba1 shr 24 and 0xFF)) / 2 shl 24 or
                (((rgba0 shr 16 and 0xFF) + (rgba1 shr 16 and 0xFF)) / 2 shl 16) or
                (((rgba0 shr 8 and 0xFF) + (rgba1 shr 8 and 0xFF)) / 2 shl 8) or
                ((rgba0 and 0xFF) + (rgba1 and 0xFF)) / 2
        }
    }

    infix fun and(other: ColorUtil) = this.rgba and other.rgba
}

enum class ColorIntent {
    NORMAL,
    HOVERED,
    DISABLED
}
