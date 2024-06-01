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

import be.bluexin.mcui.Constants
import be.bluexin.mcui.config.Settings
import be.bluexin.mcui.deprecated.api.themes.IHudDrawContext
import be.bluexin.mcui.themes.loader.AbstractThemeLoader
import gnu.jel.CompiledExpression

/**
 * Typesafe wrappers for {@link CompiledExpression}
 *
 * @author Bluexin
 */
sealed class CompiledExpressionWrapper<out T: Any>(val compiledExpression: CompiledExpression) : Function1<IHudDrawContext, T> {
    protected fun warn(e: Throwable) {
        val message = "An error occurred while executing an Expression"
        Constants.LOG.warn(message, e)
        AbstractThemeLoader.Reporter += e.message ?: message
        // let ElementGroup handle it
//        throw RuntimeException(message, e)
    }
}

class IntExpressionWrapper(compiledExpression: CompiledExpression) : CompiledExpressionWrapper<Int>(compiledExpression) {
    override fun invoke(ctx: IHudDrawContext): Int = try {
        compiledExpression.evaluate_int(arrayOf(ctx, Settings.JelWrappers))
    } catch (e: Throwable) {
        warn(e)
        0
    }
}

class DoubleExpressionWrapper(compiledExpression: CompiledExpression) : CompiledExpressionWrapper<Double>(compiledExpression) {
    override fun invoke(ctx: IHudDrawContext): Double = try {
        compiledExpression.evaluate_double(arrayOf(ctx, Settings.JelWrappers))
    } catch (e: Throwable) {
        warn(e)
        0.0
    }
}

class StringExpressionWrapper(compiledExpression: CompiledExpression) : CompiledExpressionWrapper<String>(compiledExpression) {
    override fun invoke(ctx: IHudDrawContext): String = try {
        compiledExpression.evaluate(arrayOf(ctx, Settings.JelWrappers)).toString()
    } catch (e: Throwable) {
        warn(e)
        "--Error!"
    }
}

class BooleanExpressionWrapper(compiledExpression: CompiledExpression) : CompiledExpressionWrapper<Boolean>(compiledExpression) {
    override fun invoke(ctx: IHudDrawContext): Boolean = try {
        compiledExpression.evaluate_boolean(arrayOf(ctx, Settings.JelWrappers))
    } catch (e: Throwable) {
        warn(e)
        false
    }
}

class UnitExpressionWrapper(compiledExpression: CompiledExpression) : CompiledExpressionWrapper<Unit>(compiledExpression) {
    override fun invoke(ctx: IHudDrawContext) = try {
        compiledExpression.evaluate_void(arrayOf(ctx, Settings.JelWrappers))
    } catch (e: Throwable) {
        warn(e)
    }
}
