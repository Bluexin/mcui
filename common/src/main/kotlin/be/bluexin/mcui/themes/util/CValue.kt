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
import be.bluexin.mcui.api.themes.IHudDrawContext
import be.bluexin.mcui.themes.util.serialization.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Wraps around custom types implementation for XML loading and value caching.
 *
 * @author Bluexin
 */
@LuajMapped(UnknownCValueMapper::class)
sealed class CValue<out T : Any>(@Transient val value: (IHudDrawContext) -> T) : (IHudDrawContext) -> T {
    override fun invoke(ctx: IHudDrawContext) = value(ctx)
}

/**
 * Custom Int type.
 */
@Serializable(CIntSerializer::class)
@LuajMapped(CIntMapper::class)
class CInt(value: (IHudDrawContext) -> Int) : CValue<Int>(value) {
    companion object {
        val ZERO = CInt { 0 }
        val WHITE = CInt { 0xFFFFFFFF.toInt() }
    }
}

/**
 * Custom Double type.
 */
@Serializable(CDoubleSerializer::class)
@LuajMapped(CDoubleMapper::class)
class CDouble(value: (IHudDrawContext) -> Double) : CValue<Double>(value) {
    companion object {
        val ZERO = CDouble { 0.0 }
        val ONE = CDouble { 1.0 }
    }
}

/**
 * Custom String type.
 */
@Serializable(CStringSerializer::class)
@LuajMapped(CStringMapper::class)
class CString(value: (IHudDrawContext) -> String) : CValue<String>(value) {
    companion object {
        val EMPTY = CString { "" }
    }
}

/**
 * Custom Boolean type.
 */
@Serializable(CBooleanSerializer::class)
@LuajMapped(CBooleanMapper::class)
class CBoolean(value: (IHudDrawContext) -> Boolean) : CValue<Boolean>(value) {
    companion object {
        val TRUE = CBoolean { true }
        val FALSE = CBoolean { false }
    }
}

/**
 * Custom Unit/Void type.
 */
@Serializable(CUnitSerializer::class)
class CUnit(value: (IHudDrawContext) -> Unit) : CValue<Unit>(value) {
    companion object {
        val UNIT = CUnit { }
    }
}

val ((IHudDrawContext) -> Any).expressionIntermediate: ExpressionIntermediate? get() = (this as? CachedExpression<*>)?.expressionIntermediate
val ((IHudDrawContext) -> Any).expression: String? get() = this.expressionIntermediate?.expression
