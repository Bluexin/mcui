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

import be.bluexin.mcui.Constants
import be.bluexin.mcui.api.info.IOption
import be.bluexin.mcui.api.themes.IHudDrawContext
import be.bluexin.mcui.config.OptionCore
import be.bluexin.mcui.config.Settings
import be.bluexin.mcui.effects.StatusEffects
import be.bluexin.mcui.platform.Services
import be.bluexin.mcui.themes.util.typeadapters.JelType
import be.bluexin.mcui.util.ColorUtil
import be.bluexin.mcui.util.HealthStep
import gnu.jel.CompilationException
import gnu.jel.DVMap
import gnu.jel.Library
import net.minecraft.client.resources.language.I18n
import net.minecraft.locale.Language

/**
 * Part of saoui by Bluexin.
 *
 * @author Bluexin
 */
object LibHelper {
    private val emptyContext = mapOf("__empty_marker" to JelType.ERROR)
    private val contextResolver = ContextAwareDVMap(emptyContext)

    val LIB: Library by lazy {
        val staticLib = arrayOf(Math::class.java, HealthStep::class.java, StatusEffects::class.java, OptionCore::class.java, Settings.JelWrappers::class.java, ColorUtil::class.java, I18n::class.java)
        val dynLib = arrayOf(IHudDrawContext::class.java)
        val dotClasses = arrayOf(String::class.java, IOption::class.java, List::class.java, StatusEffects::class.java, HealthStep::class.java, ColorUtil::class.java, Language::class.java)
        Library(staticLib, dynLib, dotClasses, contextResolver, null)
    }

    init {
        try {
            LIB.markStateDependent("random", null)
        } catch (e: CompilationException) {
            throw IllegalStateException(e)
        }
    }

    val obfuscated: Boolean by lazy {
        val obf = !Services.PLATFORM.isDevelopmentEnvironment
        Constants.LOG.debug("Obfuscated: $obf")
        obf
    }

    // TODO : would be nice to have a cleaner way to do this with less side effects in init {}, maybe move compiling to setup ?
    fun pushContext(context: Map<String, JelType>) {
        check(emptyContext === contextResolver.context) { "Context already pushed !" }
        contextResolver.context = context
        Constants.LOG.info("Context pushed $context from $stack")
    }

    fun popContext() {
        check(emptyContext !== contextResolver.context) { "Context not pushed !" }
        contextResolver.context = emptyContext
        Constants.LOG.info("Context popped from $stack")
    }

    private val stack
        get() = StackWalker.getInstance().walk {
            it.skip(3).limit(6).toList()
    }
}

private class ContextAwareDVMap(
    var context: Map<String, JelType>
) : DVMap() {
    override fun getTypeName(name: String): String? = context[name]?.let {
        if (it == JelType.ERROR) {
            Constants.LOG.error("Variable $name was not read properly !")
            null
        } else it
    }?.typeName
}
