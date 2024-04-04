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

package be.bluexin.mcui.themes.util

import be.bluexin.luajksp.annotations.LuajMapped

/**
 * Part of saoui by Bluexin.
 *
 * @author Bluexin
 */
@LuajMapped(CacheTypeMapper::class)
enum class CacheType(private val provider: (CompiledExpressionWrapper<*>, ExpressionIntermediate) -> CachedExpression<*>) {

    /**
     * Values will be cached per frame rendering.
     */
    PER_FRAME({ expression, intermediate -> FrameCachedExpression(expression, intermediate) }),

    /**
     * Values will be cached per frame rendering.
     */
    @Deprecated("Replaced with explicit PER_FRAME", replaceWith = ReplaceWith("PER_FRAME"))
    DEFAULT({ expression, intermediate -> FrameCachedExpression(expression, intermediate) }),

    /**
     * Values will be cached whenever they're first queried, and never updated.
     */
    STATIC({ expression, intermediate -> StaticCachedExpression(expression, intermediate) }),

    /**
     * Values will be cached whenever a screen size change is detected.
     */
    SIZE_CHANGE({ expression, intermediate -> SizeCachedExpression(expression, intermediate) }),

    /**
     * Values will not be cached (unrecommended -- in most cases PER_FRAME is better. Use with caution).
     */
    NONE({ expression, intermediate -> UnCachedExpression(expression, intermediate) });

    @Suppress("UNCHECKED_CAST")
    fun <T: Any> cacheExpression(expr: CompiledExpressionWrapper<T>, intermediate: ExpressionIntermediate) = provider(expr, intermediate) as CachedExpression<T>

    companion object {
        private val map = entries.associateBy(CacheType::name)

        operator fun invoke(name: String) = map[name]
    }
}
