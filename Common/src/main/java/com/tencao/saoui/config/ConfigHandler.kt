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

package com.tencao.saoui.config

import com.tencao.saoui.Constants
import com.tencao.saoui.config.Settings.NS_BUILTIN
import net.minecraft.util.ResourceLocation

/**
 * Part of SAOUI

 * @author Bluexin
 */

object ConfigHandler{

    val DEFAULT_THEME = ResourceLocation(Constants.MODID, "sao")

    private fun general(key: String) = ResourceLocation("general", key)
    var lastVersion by StringSetting(NS_BUILTIN, general("lastUpdate"), "nothing").register()
    var ignoreUpdate by BooleanSetting(NS_BUILTIN, general("ignoreUpdate"), true).register()
    var enableDebug by BooleanSetting(NS_BUILTIN, general("debug"), false).register()
    var debugFakePT by IntSetting(
        NS_BUILTIN, general("debugFakePT"), 0, "Amount of fake party members, 0 to disable."
    ) { it in 0..10 }.register()
    var currentTheme by ResourceLocationSetting(
        NS_BUILTIN, general("currentTheme"), DEFAULT_THEME,
        "The currently selected theme. If invalid or unavailable, this will default to the builtin sao theme"
    ).register()

    // Added for JNLua
    var forceNativeLibPlatform = ""
        private set
    var alwaysTryNative = false
        private set
    var forceNativeLibPathFirst = ""
        private set
    var nativeInTmpDir = false
        private set
    var logFullLibLoadErrors = false
        private set
    var limitMemory = false
        private set
    var disableLocaleChanging = true
        private set
    var forceLuaJ = false
        private set
    var registerLuaJArchitecture = false
        private set
    var enableLua53 = true
        private set
    var enableLua54 = true
        private set
    var defaultLua53 = true
        private set
    // end of JNLua
}
