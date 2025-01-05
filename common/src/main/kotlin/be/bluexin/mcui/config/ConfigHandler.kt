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

package be.bluexin.mcui.config

import be.bluexin.mcui.Constants
import be.bluexin.mcui.config.Settings.NS_BUILTIN
import be.bluexin.mcui.themes.meta.ThemeAnalyzer
import be.bluexin.mcui.util.RLSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.json.Json
import net.minecraft.resources.ResourceLocation
import java.util.*

/**
 * Part of SAOUI

 * @author Bluexin
 */
object ConfigHandler {
    val DEFAULT_THEME = ResourceLocation(Constants.MOD_ID, "hex2")

    val DEFAULT_SCREEN_CONFIG: Map<ResourceLocation, ResourceLocation> = mapOf(
        ThemeAnalyzer.MCUI_SETTINGS to ResourceLocation(Constants.MOD_ID, "hex2"),
        ThemeAnalyzer.HUD to ResourceLocation(Constants.LEGACY_MOD_ID, "sao"),
    )

    private fun general(key: String) = ResourceLocation("general", key)

    private val allSettings = LinkedList<Setting<*>>()
    private fun <T : Setting<*>> wrap(setting: T) = setting.also {
        it.namespace = NS_BUILTIN
        allSettings.add(it)
    }

    var lastVersion by wrap(
        StringSetting(
            key = general("last_update"),
            defaultValue = "nothing",
            showInUI = false
        )
    )
    var ignoreUpdate by wrap(BooleanSetting(general("ignore_update"), true))
    var enableDebug by wrap(BooleanSetting(general("debug"), false))
    var debugFakePT by wrap(
        IntSetting(
            general("debug_fake_pt"), 0, "Amount of fake party members, 0 to disable.",
            min = 0, max = 10
        )
    )

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        ignoreUnknownKeys = true
        useAlternativeNames = false
        allowTrailingComma = true
        allowComments = true
    }
    private val screenSettingsSerializer = MapSerializer(
        RLSerializer(), RLSerializer()
    )
    private var screenSettings by wrap(
        StringSetting(
            key = general("screen_settings"),
            defaultValue = DEFAULT_SCREEN_CONFIG.let {
                json.encodeToString(screenSettingsSerializer, it)
            },
            comment = "Screen theme selection. If invalid or unavailable, this will default to built-in themes",
            showInUI = false,
        )
    )

    fun setScreenSettings(settings: Map<ResourceLocation, ResourceLocation>) {
        screenSettings = json.encodeToString(screenSettingsSerializer, settings)
    }

    fun getScreenSettings(): Map<ResourceLocation, ResourceLocation> =
        json.decodeFromString(screenSettingsSerializer, screenSettings)

    fun registerSettings() {
        allSettings.forEach(Setting<*>::register)
    }

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
