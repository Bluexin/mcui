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

package be.bluexin.mcui.themes.miniscript

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlOtherAttributes
import nl.adaptivity.xmlutil.serialization.XmlValue

/**
 * Part of saoui by Bluexin.
 *
 * @author Bluexin
 */
@Serializable
sealed class ExpressionIntermediate {
    abstract val expression: String
    abstract val cacheType: CacheType

    val asAnonymous: AnonymousExpressionIntermediate
        get() = AnonymousExpressionIntermediate(expression, cacheType)
}

@Serializable
data class AnonymousExpressionIntermediate(
    @SerialName("expression")
    @XmlValue
    override val expression: String,
    @SerialName("cache")
    @XmlOtherAttributes
    override val cacheType: CacheType = CacheType.PER_FRAME
) : ExpressionIntermediate() {
    companion object {
        val EMPTY = AnonymousExpressionIntermediate("")
    }
}
