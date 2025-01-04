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
import be.bluexin.mcui.deprecated.api.themes.IHudDrawContext
import be.bluexin.mcui.effects.StatusEffect
import be.bluexin.mcui.themes.loader.AbstractThemeLoader
import be.bluexin.mcui.themes.miniscript.api.GameWindowInfo
import be.bluexin.mcui.util.HealthStep
import net.minecraft.util.profiling.InactiveProfiler
import net.minecraft.util.profiling.ProfilerFiller
import net.minecraft.world.entity.LivingEntity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Part of saoui by Bluexin.
 *
 * @author Bluexin
 */
sealed class CachedExpression<T : Any>(
    var expression: (IHudDrawContext) -> T,
    val expressionIntermediate: ExpressionIntermediate
) : (IHudDrawContext) -> T, KoinComponent {

    protected val gameWindowInfo: GameWindowInfo by inject()
    protected abstract val cache: T?

    protected fun warn(e: Throwable) {
        val message = "An error occurred while executing the expression \"${expressionIntermediate.expression}\""
        Constants.LOG.warn(message, e)
        AbstractThemeLoader.Reporter += e.message ?: message
        // let ElementGroup handle it
//        throw RuntimeException(message, e)
    }

    override fun toString(): String {
        return "${javaClass.simpleName}(expressionIntermediate=$expressionIntermediate, cache=$cache)"
    }
}

class FrameCachedExpression<T : Any>(
    expression: (IHudDrawContext) -> T,
    expressionIntermediate: ExpressionIntermediate
) : CachedExpression<T>(expression, expressionIntermediate) {
    override var cache: T? = null

    private var lastTime = -1.0F

    private fun checkUpdateTime() =
        if (lastTime == gameWindowInfo.partialTicks) false
        else {
            lastTime = gameWindowInfo.partialTicks
            true
        }

    override fun invoke(ctx: IHudDrawContext): T {
        if (checkUpdateTime()) cache = try {
            expression(ctx)
        } catch (e: Exception) {
            warn(e)
            val expr = expression
            require(expr is CompiledExpressionWrapper<T>) {
                "An error occurred while executing a default expression !"
            }
            expression = { expr.default }
            expr.default
        }
        return cache!!
    }
}

class StaticCachedExpression<T : Any>(
    expression: (IHudDrawContext) -> T,
    expressionIntermediate: ExpressionIntermediate
) : CachedExpression<T>(expression, expressionIntermediate) {
    override val cache: T by lazy { expression(StubContext) }

    companion object StubContext : IHudDrawContext {
        override fun hasMount() = false
        override fun mountHp() = 0f
        override fun mountMaxHp() = 1f
        override fun inWater() = false
        override fun air() = 0
        override fun armor() = 1
        override fun ptHealthStep(index: Int): HealthStep = HealthStep.INVALID
        override fun ptName(index: Int): String = ""
        override fun ptHp(index: Int) = 0f
        override fun ptMaxHp(index: Int) = 0f
        override fun ptHpPct(index: Int) = 0f
        override fun ptSize() = 0
        override fun setI(i: Int) = Unit
        override fun i() = 0
        override fun username() = null
        override fun usernamewidth() = 0.0
        override fun hpPct() = 0.0
        override fun hp() = 0f
        override fun maxHp() = 0f
        override fun healthStep() = null
        override fun selectedslot() = 0
        override fun scaledwidth() = 0
        override fun scaledheight() = 0
        override fun offhandEmpty(slot: Int) = false
        override fun strWidth(s: String) = 0
        override fun strHeight(): Int = 0
        override fun absorption() = 0f
        override fun level() = 0
        override fun experience() = 0f
        override fun getZ() = 0.0f
        override fun getFontRenderer() = null
        override fun getItemRenderer() = null
        override fun getPlayer() = null
        override fun getPartialTicks() = 0f
        override fun horsejump() = 0f
        override fun foodLevel() = 0f
        override fun saturationLevel() = 0f
        override fun statusEffects() = mutableListOf<StatusEffect>()
        override fun nearbyEntities() = mutableListOf<LivingEntity>()
        override fun entityName(index: Int) = ""
        override fun entityHp(index: Int) = 0f
        override fun entityMaxHp(index: Int) = 0f
        override fun entityHpPct(index: Int) = 0f
        override fun entityHealthStep(index: Int) = HealthStep.INVALID
        override fun targetEntity() = null
        override fun targetName() = ""
        override fun targetHp() = 0f
        override fun targetMaxHp() = 0f
        override fun targetHpPct() = 0f
        override fun targetHealthStep() = HealthStep.INVALID
        override fun getStringProperty(name: String) = ""
        override fun getDoubleProperty(name: String) = 0.0
        override fun getIntProperty(name: String) = 0
        override fun getBooleanProperty(name: String) = false
        override fun getUnitProperty(name: String) = Unit
        override fun getProfiler(): ProfilerFiller = InactiveProfiler.INSTANCE
    }

    override fun invoke(ctx: IHudDrawContext) = cache
}

class SizeCachedExpression<T : Any>(
    expression: (IHudDrawContext) -> T,
    expressionIntermediate: ExpressionIntermediate
) : CachedExpression<T>(expression, expressionIntermediate) {
    override var cache: T? = null

    private var lastW = 0
    private var lastH = 0

    private fun checkUpdateSize() =
        if (lastW == gameWindowInfo.scaledWidth && lastH == gameWindowInfo.scaledHeight) false else {
            lastW = gameWindowInfo.scaledWidth
            lastH = gameWindowInfo.scaledHeight
            true
        }

    override fun invoke(ctx: IHudDrawContext): T {
        if (checkUpdateSize()) cache = expression(ctx)
        return cache!!
    }
}

class UnCachedExpression<T : Any>(
    expression: (IHudDrawContext) -> T,
    expressionIntermediate: ExpressionIntermediate
) : CachedExpression<T>(expression, expressionIntermediate) {
    override val cache: T
        get() = throw UnsupportedOperationException()

    override fun invoke(ctx: IHudDrawContext) = expression(ctx)
}
