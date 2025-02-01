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

package be.bluexin.mcui.themes.meta

import be.bluexin.mcui.commands.GeneralCommands
import be.bluexin.mcui.commands.McuiCommand
import be.bluexin.mcui.config.ConfigHandler
import be.bluexin.mcui.logger
import be.bluexin.mcui.screens.LuaScriptedScreen
import be.bluexin.mcui.themes.elements.Hud
import be.bluexin.mcui.themes.loader.AbstractThemeLoader
import be.bluexin.mcui.themes.loader.SettingsLoader
import be.bluexin.mcui.themes.loader.TexturesFallbackHandler
import be.bluexin.mcui.themes.scripting.lib.RegisterScreen
import be.bluexin.mcui.util.Client
import be.bluexin.mcui.util.Client.resourceManager
import be.bluexin.mcui.util.debug
import be.bluexin.mcui.util.info
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import org.koin.core.annotation.Single

@Single
class ThemeManager(
    private val themeDetector: ThemeDetector,
    private val themeAnalyzer: ThemeAnalyzer,
    private val settingsLoader: SettingsLoader,
    private val texturesFallbackHandler: TexturesFallbackHandler,
) {

    // TODO: tests
    // TODO: theme format versions
    // TODO: loading reporter (amount of issues, details, missing keys, ..?)

    lateinit var HUD: Hud
        private set
    lateinit var themeList: Map<ResourceLocation, ThemeDefinition>
        private set

    /**
     * Map of all screen IDs to the themes which registered an implementation for it (and their callback)
     * TODO: This is not filtered for "valid" screen IDs !
     * map<screenId, map<themeId, screenCallback>>
     */
    private val availableThemeScreens =
        mutableMapOf<ResourceLocation, Map<ResourceLocation, (ResourceLocation) -> Unit>>()

    /**
     * Map of all configured screens
     * map<screenId, themeId>
     */
    private val screenConfiguration = ConfigHandler.getScreenSettings().toMutableMap()

    private val screenCache = mutableMapOf<ResourceLocation, LuaScriptedScreen?>()

    private var isReloading = false

    private fun load() {
        screenConfiguration.forEach { (key, value) ->
            val callback = availableThemeScreens[key]?.get(value)
            if (callback != null) initializeScreen(key, callback)
        }

        reportLoading()

//        if (!isReloading) GLCore.setFont(Client.mc, OptionCore.CUSTOM_FONT.isEnabled)
    }

    fun applyData(data: Map<ResourceLocation, ThemeDefinition>, resourceManager: ResourceManager) {
        RegisterScreen.clear()
        themeList = data
        isReloading = true
        load()
        isReloading = false
    }

    private val logger = logger()

    fun loadData(resourceManager: ResourceManager): Map<ResourceLocation, ThemeDefinition> {
        availableThemeScreens.clear()
        screenCache.clear()
        return themeDetector.listThemes(resourceManager).onEach { (_, themeDefinition) ->
            analyzeTheme(
                resourceManager = resourceManager,
                themeDefinition = themeDefinition,
                successReport = logger::debug,
                failureReport = {
                    AbstractThemeLoader.Reporter += it()
                }
            )
        }
    }

    fun reloadThemes(
        successReport: (() -> String) -> Unit,
        failureReport: (() -> String) -> Unit,
    ) {
        availableThemeScreens.clear()
        screenCache.clear()
        themeList.forEach { (_, themeDefinition) ->
            analyzeTheme(
                resourceManager = resourceManager,
                themeDefinition = themeDefinition,
                successReport = successReport,
                failureReport = failureReport
            )
        }
    }

    private fun analyzeTheme(
        resourceManager: ResourceManager,
        themeDefinition: ThemeDefinition,
        successReport: (() -> String) -> Unit,
        failureReport: (() -> String) -> Unit,
    ) {
        val themeScreens = themeAnalyzer.analyzeThemeScreens(
            resourceManager = resourceManager,
            theme = themeDefinition,
            setHud = {
                logger.info("Setting HUD to ${themeDefinition.id}")
                // This only handles status effects icons atm, which are primarily for use in HUD
                texturesFallbackHandler.init(themeDefinition)
                HUD = it
            },
            successReport = successReport,
            failureReport = failureReport
        )

        themeScreens.forEach { (screenId, callback) ->
            this.availableThemeScreens.compute(screenId) { _, existing ->
                if (existing != null) existing + (themeDefinition.id to callback)
                else mapOf(themeDefinition.id to callback)
            }
        }

        logger.info { "Found ${themeScreens.size} screens defined in ${themeDefinition.id} : ${themeScreens.keys}" }

        settingsLoader.loadSettings(resourceManager, themeDefinition)
    }

    private fun reportLoading() {
        Client.mc.chatListener.let {
            val style = Style.EMPTY
                .withClickEvent(
                    ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        McuiCommand.useCommand(GeneralCommands.PrintErrors)
                    )
                )

            Component.translatable(
                "saoui.menu.errors",
                AbstractThemeLoader.Reporter.errors.size,
                // TODO : report errors per theme (& include lua errors now that it's all eagerly loaded pl0x)
                Component.translatableWithFallback(
                    "__todo",
                    "(TODO : report errors per theme)"
                ).apply {
                    this.style = Style.EMPTY.withHoverEvent(
                        HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            Component.literal("(TODO : add back theme id)")
                        )
                    )
                }
            ).apply {
                this.style = style
                it.handleSystemMessage(this, false)
            }
            if (AbstractThemeLoader.Reporter.errors.isNotEmpty()) Component.translatableWithFallback(
                "saoui.menu.clicktoexpand",
                "(click to expand)"
            ).apply {
                this.style = style
                    .withColor(ChatFormatting.GRAY)
                    .withItalic(true)
                it.handleSystemMessage(this, false)
            }
        }
    }

    val allScreenIds get() = availableThemeScreens.keys

    fun getAllScreens(screenId: ResourceLocation): Map<ResourceLocation, (ResourceLocation) -> Unit> =
        availableThemeScreens[screenId].orEmpty()

    fun getScreenConfiguration(screenId: ResourceLocation): ResourceLocation? =
        screenConfiguration[screenId]

    private fun initializeScreen(screenId: ResourceLocation, registeredScreen: (ResourceLocation) -> Unit) {
        if (screenId == ThemeAnalyzer.HUD) registeredScreen(ThemeAnalyzer.HUD)
        else getScreen(screenId)
    }

    fun setScreenConfiguration(screenId: ResourceLocation, themeId: ResourceLocation) {
        val registeredScreen = availableThemeScreens[screenId]?.get(themeId)
        if (registeredScreen != null) {
            screenConfiguration[screenId] = themeId
            ConfigHandler.setScreenSettings(screenConfiguration)
            screenCache -= screenId
            initializeScreen(screenId, registeredScreen)
        }
    }

    /**
     * This will not cache and is exposed for use in debug commands !
     * @return a new screen instance for the specified [screenId] as implemented by given [themeId]
     */
    fun getThemeScreen(screenId: ResourceLocation, themeId: ResourceLocation): LuaScriptedScreen? =
        getAllScreens(screenId)[themeId]?.let { callback ->
            LuaScriptedScreen(screenId, themeId).also {
                it.load(callback)
            }
        }

    /**
     * This will configure screens lazily and cache results.
     * @return the screen instance for the specified [screenId]
     */
    fun getScreen(screenId: ResourceLocation): LuaScriptedScreen? = screenCache.getOrPut(screenId) {
        getScreenConfiguration(screenId)?.let {
            getThemeScreen(screenId, it)
        }
    }
}
