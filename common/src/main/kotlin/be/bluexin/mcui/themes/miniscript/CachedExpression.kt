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
import be.bluexin.mcui.effects.StatusEffects
import be.bluexin.mcui.util.HealthStep
import net.minecraft.util.profiling.InactiveProfiler
import net.minecraft.util.profiling.ProfilerFiller
import net.minecraft.world.entity.LivingEntity

/**
 * Part of saoui by Bluexin.
 *
 * @author Bluexin
 */
sealed class CachedExpression<out T: Any>(
    val expression: CompiledExpressionWrapper<T>,
    val expressionIntermediate: ExpressionIntermediate
) : (IHudDrawContext) -> T {
    protected abstract val cache: T?

    override fun toString(): String {
        return "${javaClass.simpleName}(expressionIntermediate=$expressionIntermediate, cache=$cache)"
    }
}

class FrameCachedExpression<T: Any>(
    expression: CompiledExpressionWrapper<T>,
    expressionIntermediate: ExpressionIntermediate
) : CachedExpression<T>(expression, expressionIntermediate) {
    override var cache: T? = null

    private var lastTime = -1.0F

    private fun checkUpdateTime(ctx: IHudDrawContext) = if (lastTime == ctx.partialTicks) false else {
        lastTime = ctx.partialTicks
        true
    }

    override fun invoke(ctx: IHudDrawContext): T {
        if (checkUpdateTime(ctx)) cache = expression.invoke(ctx)
        return cache!!
    }
}

class StaticCachedExpression<out T: Any>(
    expression: CompiledExpressionWrapper<T>,
    expressionIntermediate: ExpressionIntermediate
) : CachedExpression<T>(expression, expressionIntermediate) {
    override val cache: T by lazy { expression.invoke(StubContext) }

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
        override fun statusEffects() = mutableListOf<StatusEffects>()
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

class SizeCachedExpression<T: Any>(
    expression: CompiledExpressionWrapper<T>,
    expressionIntermediate: ExpressionIntermediate
) : CachedExpression<T>(expression, expressionIntermediate) {
    override var cache: T? = null

    private var lastW = 0
    private var lastH = 0

    private fun checkUpdateSize(ctx: IHudDrawContext) =
        if (lastW == ctx.scaledwidth() && lastH == ctx.scaledheight()) false else {
            lastW = ctx.scaledwidth()
            lastH = ctx.scaledheight()
            true
        }

    override fun invoke(ctx: IHudDrawContext): T {
        if (checkUpdateSize(ctx)) cache = expression.invoke(ctx)
        return cache!!
    }
}

class UnCachedExpression<out T: Any>(
    expression: CompiledExpressionWrapper<T>,
    expressionIntermediate: ExpressionIntermediate
) : CachedExpression<T>(expression, expressionIntermediate) {
    override val cache: T
        get() = throw UnsupportedOperationException()

    override fun invoke(ctx: IHudDrawContext) = expression.invoke(ctx)
}
