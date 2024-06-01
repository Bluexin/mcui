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

package be.bluexin.mcui.themes.miniscript.serialization

import be.bluexin.mcui.themes.miniscript.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Part of saoui by Bluexin.

 * @author Bluexin
 */
@OptIn(ExperimentalSerializationApi::class)
sealed class CValueSerializer<CValueType: CValue<T>, T: Any>(
    private val expressionAdapter: BasicExpressionAdapter<CValueType, T>
) : KSerializer<CValueType> {
    private val delegate = AnonymousExpressionIntermediate.serializer()

    override fun deserialize(decoder: Decoder): CValueType {
        return expressionAdapter.compile(decoder.decodeSerializableValue(delegate))
    }

    override val descriptor by lazy {
        SerialDescriptor(javaClass.canonicalName, delegate.descriptor)
    }

    override fun serialize(encoder: Encoder, value: CValueType) =
        when (val intermediate = (value.value as? CachedExpression<*>)?.expressionIntermediate) {
            null -> encoder.encodeNull()
            else -> encoder.encodeSerializableValue(delegate, intermediate.asAnonymous)
        }
}

/**
 * Adapts an expression that should return an int.
 */
class CIntSerializer : CValueSerializer<CInt, Int>(IntExpressionAdapter)

/**
 * Adapts an expression that should return a double.
 */
class CDoubleSerializer : CValueSerializer<CDouble, Double>(DoubleExpressionAdapter)

/**
 * Adapts an expression that should return a String.
 */
class CStringSerializer : CValueSerializer<CString, String>(StringExpressionAdapter)

/**
 * Adapts an expression that should return a boolean.
 */
class CBooleanSerializer : CValueSerializer<CBoolean, Boolean>(BooleanExpressionAdapter)

/**
 * Adapts an expression that should return [Unit] (aka void).
 */
class CUnitSerializer : CValueSerializer<CUnit, Unit>(UnitExpressionAdapter)
