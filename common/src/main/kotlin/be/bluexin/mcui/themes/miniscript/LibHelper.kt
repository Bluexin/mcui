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
import be.bluexin.mcui.config.OptionCore
import be.bluexin.mcui.deprecated.api.info.IOption
import be.bluexin.mcui.deprecated.api.themes.IHudDrawContext
import be.bluexin.mcui.effects.StatusEffect
import be.bluexin.mcui.themes.miniscript.api.DrawContext
import be.bluexin.mcui.themes.miniscript.api.MiniscriptLivingEntity
import be.bluexin.mcui.themes.miniscript.api.MiniscriptPlayer
import be.bluexin.mcui.themes.miniscript.api.MiniscriptSettings
import be.bluexin.mcui.themes.miniscript.serialization.JelType
import be.bluexin.mcui.util.ColorUtil
import be.bluexin.mcui.util.HealthStep
import be.bluexin.mcui.util.LayeredMap
import be.bluexin.mcui.util.math.ceilInt
import be.bluexin.mcui.util.math.floorInt
import be.bluexin.mcui.util.trace
import gnu.jel.CompilationException
import gnu.jel.DVMap
import gnu.jel.Library
import net.minecraft.client.resources.language.I18n
import org.koin.core.annotation.Single
import java.lang.reflect.Member

/**
 * Part of saoui by Bluexin.
 *
 * @author Bluexin
 */
// TODO : check injected statics (access$next and other access$..., getKoin, pushContext, getClass
@Single
class LibHelper {
    private val contextResolver = ContextAwareDVMap()

    val jelLibrary: Library by lazy {
        val staticLib = arrayOf(
            Math::class.java,
            HealthStep::class.java,
            StatusEffect::class.java,
            OptionCore::class.java,
            ColorUtil::class.java,
            McuiStaticLib::class.java
        )
        val dynLib = arrayOf(
            IHudDrawContext::class.java,
            DrawContext::class.java,
        )
        val dotClasses = arrayOf(
            String::class.java,
            IOption::class.java,
            List::class.java,
            StatusEffect::class.java,
            HealthStep::class.java,
            ColorUtil::class.java,

            MiniscriptPlayer::class.java,
            MiniscriptLivingEntity::class.java,
            MiniscriptSettings::class.java,
        )
        // FIXME : currently Array#length is not supported
        // This probably needs to be fixed in JEL. `dotClasses.map(Class<*>::arrayType)` does not expose "length"
        object : Library(staticLib, dynLib, dotClasses, contextResolver, null) {
            override fun getMember(container: Class<*>?, name: String, params: Array<out Class<*>>?): Member {
                return /*if (container != null && container.isArray && name == "length") {
                    TODO()
                } else*/ super.getMember(container, name, params)
            }
        }
    }

    init {
        try {
            jelLibrary.markStateDependent("random", null)
        } catch (e: CompilationException) {
            throw IllegalStateException(e)
        }
    }

    // TODO : would be nice to have a cleaner way to do this with less side effects in init {}, maybe move compiling to setup ?
    fun pushContext(context: Map<String, JelType>) {
        Constants.LOG.trace { "Context pushed $context from $stack" }
        contextResolver.push(context)
    }

    fun popContext() {
        Constants.LOG.trace { "Context popped from $stack" }
        contextResolver.pop()
    }

    private val stack
        get() = StackWalker.getInstance().walk { stack ->
            stack.skip(2)
                .filter { !it.className.startsWith("kotlinx.serialization") }
                .limit(6)
                .toList()
                .joinToString(separator = "\n\t at ")
    }

    private class ContextAwareDVMap : DVMap() {
        private val contexts = LayeredMap<String, JelType>()

        fun push(context: Map<String, JelType>) {
            contexts += context
        }

        fun pop() {
            check(contexts.canPop) { "Context stack underflow !" }
            contexts.pop()
        }

        override fun getTypeName(name: String): String? = contexts[name]?.let {
            if (it == JelType.ERROR) {
                Constants.LOG.error("Variable $name was not read properly !")
                null
            } else it
        }?.typeName
    }
}

@Suppress("unused") // exposed via JEL
object McuiStaticLib {
    @JvmStatic
    fun iceil(n: Double) = ceilInt(n)

    @JvmStatic
    fun iceil(n: Float) = ceilInt(n)

    @JvmStatic
    fun ifloor(n: Double) = floorInt(n)

    @JvmStatic
    fun ifloor(n: Float) = floorInt(n)

    @JvmStatic // need separate no-vararg one for JEL it seems :/
    fun format(key: String): String = I18n.get(key)

    @JvmStatic
    fun formatOr(key: String, fallback: String): String = if (I18n.exists(key)) I18n.get(key) else fallback

    @JvmStatic
    fun format(key: String, vararg args: Any): String = I18n.get(key, *args)
}
