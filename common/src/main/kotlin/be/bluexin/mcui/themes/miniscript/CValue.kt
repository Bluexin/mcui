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

import be.bluexin.luajksp.annotations.LuajMapped
import be.bluexin.mcui.themes.miniscript.api.GameContext
import be.bluexin.mcui.themes.miniscript.serialization.*
import be.bluexin.mcui.themes.serde.legacyformat.dto.ExpressionIntermediate
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Wraps around custom types implementation for XML loading and value caching.
 *
 * @author Bluexin
 */
@LuajMapped(UnknownCValueMapper::class, import = "support")
sealed class CValue<out T : Any>(@Transient val value: (GameContext) -> T) : () -> T, KoinComponent {
    private val context: GameContext by inject()
    abstract val type: JelType
    override fun invoke(): T = value(context)
}

/**
 * Custom Int type.
 */
@Serializable(CIntSerializer::class)
@LuajMapped(CIntMapper::class, import = "support")
class CInt(value: (GameContext) -> Int) : CValue<Int>(value) {
    override val type: JelType
        get() = JelType.INT

    companion object {
        val ZERO = CInt { 0 }
        val WHITE = CInt { 0xFFFFFFFF.toInt() }
    }
}

/**
 * Custom Double type.
 */
@Serializable(CDoubleSerializer::class)
@LuajMapped(CDoubleMapper::class, import = "support")
class CDouble(value: (GameContext) -> Double) : CValue<Double>(value) {
    override val type: JelType
        get() = JelType.DOUBLE

    companion object {
        val ZERO = CDouble { 0.0 }
        val ONE = CDouble { 1.0 }
    }
}

/**
 * Custom String type.
 */
@Serializable(CStringSerializer::class)
@LuajMapped(CStringMapper::class, import = "support")
class CString(value: (GameContext) -> String) : CValue<String>(value) {
    override val type: JelType
        get() = JelType.STRING

    companion object {
        val EMPTY = CString { "" }
    }
}

@LuajMapped(CResourceLocationMapper::class, import = "support")
open class CResourceLocation(value: (GameContext) -> LKResourceLocation) : CValue<LKResourceLocation>(value) {
    override val type: JelType
        get() = JelType.STRING // TODO : separate type for ResourceLocation ?

    constructor(delegate: CString) : this(Delegated(delegate))

    private class Delegated(private val delegate: CString) : (GameContext) -> LKResourceLocation {
        private var previousValue: String? = null
        private lateinit var cachedRl: LKResourceLocation

        private fun checkAndGet(): LKResourceLocation {
            val value = delegate()
            if (value != previousValue) {
                previousValue = value
                // TODO : parse failure should be reported
                cachedRl = value.let(::LKResourceLocation)
            }
            return cachedRl
        }

        override fun invoke(context: GameContext): LKResourceLocation = checkAndGet()
    }
}

/**
 * Custom Boolean type.
 */
@Serializable(CBooleanSerializer::class)
@LuajMapped(CBooleanMapper::class, import = "support")
class CBoolean(value: (GameContext) -> Boolean) : CValue<Boolean>(value) {
    override val type: JelType
        get() = JelType.BOOLEAN

    companion object {
        val TRUE = CBoolean { true }
        val FALSE = CBoolean { false }
    }
}

/**
 * Custom Unit/Void type.
 */
@Serializable(CUnitSerializer::class)
@LuajMapped(CUnitMapper::class, import = "support")
class CUnit(value: (GameContext) -> Unit) : CValue<Unit>(value) {
    override val type: JelType
        get() = JelType.UNIT

    companion object {
        val UNIT = CUnit { }
    }
}

val ((GameContext) -> Any).expressionIntermediate: ExpressionIntermediate? get() = (this as? CachedExpression<*>)?.expressionIntermediate
val ((GameContext) -> Any).expression: String? get() = this.expressionIntermediate?.expression
