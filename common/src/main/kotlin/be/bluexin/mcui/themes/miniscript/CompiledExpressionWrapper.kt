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

import be.bluexin.mcui.deprecated.api.themes.IHudDrawContext
import be.bluexin.mcui.themes.miniscript.api.GameContext
import gnu.jel.CompiledExpression
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Typesafe wrappers for [CompiledExpression]
 *
 * @author Bluexin
 */
sealed class CompiledExpressionWrapper<out T : Any>(
    val compiledExpression: CompiledExpression
) : (GameContext) -> T, KoinComponent {
    protected val legacyCtx: IHudDrawContext by inject()

    abstract val default: T
}

class IntExpressionWrapper(compiledExpression: CompiledExpression) :
    CompiledExpressionWrapper<Int>(compiledExpression) {
    override fun invoke(context: GameContext): Int =
        compiledExpression.evaluate_int(arrayOf(legacyCtx, context))

    override val default = 0
}

class DoubleExpressionWrapper(compiledExpression: CompiledExpression) :
    CompiledExpressionWrapper<Double>(compiledExpression) {
    override fun invoke(context: GameContext): Double =
        compiledExpression.evaluate_double(arrayOf(legacyCtx, context))

    override val default = 0.0
}

class StringExpressionWrapper(compiledExpression: CompiledExpression) :
    CompiledExpressionWrapper<String>(compiledExpression) {
    override fun invoke(context: GameContext): String =
        compiledExpression.evaluate(arrayOf(legacyCtx, context)).toString()

    override val default = "--Error!"
}

class BooleanExpressionWrapper(compiledExpression: CompiledExpression) :
    CompiledExpressionWrapper<Boolean>(compiledExpression) {
    override fun invoke(context: GameContext): Boolean =
        compiledExpression.evaluate_boolean(arrayOf(legacyCtx, context))

    override val default = false
}

class UnitExpressionWrapper(compiledExpression: CompiledExpression) :
    CompiledExpressionWrapper<Unit>(compiledExpression) {
    override fun invoke(context: GameContext) =
        compiledExpression.evaluate_void(arrayOf(legacyCtx, context))

    override val default = Unit
}
