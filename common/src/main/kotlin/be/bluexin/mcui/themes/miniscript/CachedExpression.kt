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
import be.bluexin.mcui.themes.loader.AbstractThemeLoader
import be.bluexin.mcui.themes.miniscript.api.GameWindowInfo
import be.bluexin.mcui.themes.serde.legacyformat.dto.ExpressionIntermediate
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Part of saoui by Bluexin.
 *
 * @author Bluexin
 */
sealed class CachedExpression<T : Any>(
    var expression: () -> T,
    val expressionIntermediate: ExpressionIntermediate
) : () -> T, KoinComponent {
    protected abstract val cache: T?

    protected fun warn(e: Throwable) {
        val message = "An error occurred while executing the expression \"${expressionIntermediate.expression}\""
        Constants.LOG.warn(message, e)
        AbstractThemeLoader.Reporter += e.message ?: message
        // let ElementGroup handle it
//        throw RuntimeException(message, e)
    }

    protected fun safeCall(): T = try {
        expression()
    } catch (e: Exception) {
        warn(e)
        val expr = expression
        require(expr is CompiledExpressionWrapper<T>) {
            "An error occurred while executing a default expression !"
        }
        expression = { expr.default }
        expr.default
    }

    override fun toString(): String {
        return "${javaClass.simpleName}(expressionIntermediate=$expressionIntermediate, cache=$cache)"
    }
}

class FrameCachedExpression<T : Any>(
    expression: () -> T,
    expressionIntermediate: ExpressionIntermediate
) : CachedExpression<T>(expression, expressionIntermediate) {
    override var cache: T? = null

    private val gameWindowInfo: GameWindowInfo by inject()

    private var lastTime = -1.0F

    private fun checkUpdateTime() =
        if (lastTime == gameWindowInfo.partialTicks) false
        else {
            lastTime = gameWindowInfo.partialTicks
            true
        }

    override fun invoke(): T = cache
        .takeUnless { checkUpdateTime() }
        ?: safeCall().also { cache = it }
}

class StaticCachedExpression<T : Any>(
    expression: () -> T,
    expressionIntermediate: ExpressionIntermediate
) : CachedExpression<T>(expression, expressionIntermediate) {
    override val cache: T by lazy { safeCall() }

    override fun invoke() = cache
}

class SizeCachedExpression<T : Any>(
    expression: () -> T,
    expressionIntermediate: ExpressionIntermediate
) : CachedExpression<T>(expression, expressionIntermediate) {
    override var cache: T? = null

    private val gameWindowInfo: GameWindowInfo by inject()

    private var lastW = 0
    private var lastH = 0

    private fun checkUpdateSize() =
        if (lastW == gameWindowInfo.scaledWidth && lastH == gameWindowInfo.scaledHeight) false
        else {
            lastW = gameWindowInfo.scaledWidth
            lastH = gameWindowInfo.scaledHeight
            true
        }

    override fun invoke(): T = cache
        .takeUnless { checkUpdateSize() }
        ?: safeCall().also { cache = it }
}

class UnCachedExpression<T : Any>(
    expression: () -> T,
    expressionIntermediate: ExpressionIntermediate
) : CachedExpression<T>(expression, expressionIntermediate) {
    override val cache: T
        get() = throw UnsupportedOperationException()

    override fun invoke() = expression()
}
