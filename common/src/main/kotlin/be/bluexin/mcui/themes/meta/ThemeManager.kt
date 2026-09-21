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
import be.bluexin.mcui.themes.loader.*
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
    private val themeLoaderRegistry: ThemeLoaderRegistry,
) {

    // TODO: tests
    // TODO: theme format versions
    // TODO: loading reporter (amount of issues, details, missing keys, ..?)

    private var activeHudThemeId: ResourceLocation? = null

    /**
     * The single active HUD pair (legacy tree + optional modern tree, built together by the loader).
     * Only the **one** active theme's HUD resources are ever loaded : switching the HUD theme
     * (via [setScreenConfiguration] or a theme reload) evicts the previous asset set, mirroring the
     * screen cache. This is a pure read for rendering ; it is (re)populated by [resolveActiveHud]
     * at reload/config-apply boundaries. Null when no theme provides a HUD.
     */
    var activeHudAssets: ThemeAssetSet? = null
        private set

    /**
     * A/B toggle : when true, [ThemeAssetSet.modernHud] (new element tree) is rendered instead of
     * the legacy HUD. Falls back to legacy rendering when no modern tree is available.
     * Controlled via the `/mcui debug modern` command.
     */
    var renderModernHud: Boolean = false

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
        screenConfiguration.keys.forEach(::getScreen)
        resolveActiveHud()

        reportLoading()

//        if (!isReloading) GLCore.setFont(Client.mc, OptionCore.CUSTOM_FONT.isEnabled)
    }

    fun applyData(data: Map<ResourceLocation, ThemeDefinition>, resourceManager: ResourceManager) {
        RegisterScreen.clear()
        themeList = data
        unloadActiveHud()
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
        unloadActiveHud()
        themeList.forEach { (_, themeDefinition) ->
            analyzeTheme(
                resourceManager = resourceManager,
                themeDefinition = themeDefinition,
                successReport = successReport,
                failureReport = failureReport
            )
        }
        resolveActiveHud()
    }

    private fun analyzeTheme(
        resourceManager: ResourceManager,
        themeDefinition: ThemeDefinition,
        successReport: (() -> String) -> Unit,
        failureReport: (() -> String) -> Unit,
    ) {
        val themeScreens = themeAnalyzer.analyzeThemeScreens(
            theme = themeDefinition,
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

        if (themeDefinition.hud != null) {
            // `mcui:hud` is treated as a selectable screen so it reappears in the settings theme-selection
            // UI and `/mcui debug open` suggestions. The callback is a pure capability marker : a HUD is
            // not Lua-backed, switching happens through setScreenConfiguration / resolveActiveHud.
            availableThemeScreens.compute(ThemeAnalyzer.HUD) { _, existing ->
                (existing.orEmpty() + (themeDefinition.id to { _ -> }))
            }
        }
    }

    /**
     * (Re)loads the HUD for the theme selected by `screenConfiguration[mcui:hud]`, evicting any
     * previously loaded HUD.
     * Falls back to the first theme providing a HUD when the configured theme is unknown or has none.
     */
    private fun resolveActiveHud() {
        val requested = screenConfiguration[ThemeAnalyzer.HUD]
        val requestedTheme = requested?.let(themeList::get)

        val theme = requestedTheme?.takeIf { it.hud != null }
            ?: run {
                if (requested != null) {
                    // TODO: surface this warning through the reload report
                    logger.warn("Theme $requested is configured as the HUD but provides none ; falling back to the first theme with a HUD")
                }
                themeList.values.firstOrNull { it.hud != null }
            }

        val themeId = theme?.id
        if (themeId == activeHudThemeId) return

        unloadActiveHud()

        if (theme != null) {
            logger.info("Setting HUD to ${theme.id}")
            // Status effects icons are drawn mainly by the HUD, so the fallback textures follow
            // the active HUD's lifecycle.
            texturesFallbackHandler.init(theme)
            activeHudAssets = themeLoaderRegistry.resolve(theme.metadata)?.load(resourceManager, theme)
            activeHudThemeId = theme.id
        }
    }

    /**
     * Drops the current HUD asset pair so the next [resolveActiveHud] reloads from scratch.
     */
    private fun unloadActiveHud() {
        activeHudAssets = null
        activeHudThemeId = null
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

    fun setScreenConfiguration(screenId: ResourceLocation, themeId: ResourceLocation) {
        if (availableThemeScreens[screenId]?.containsKey(themeId) == true) {
            screenConfiguration[screenId] = themeId
            ConfigHandler.setScreenSettings(screenConfiguration)
            if (screenId == ThemeAnalyzer.HUD) {
                resolveActiveHud()
            } else {
                screenCache -= screenId
                getScreen(screenId)
            }
        }
    }

    /**
     * This will not cache and is exposed for use in debug commands !
     * @return a new screen instance for the specified [screenId] as implemented by given [themeId]
     */
    fun getThemeScreen(screenId: ResourceLocation, themeId: ResourceLocation): LuaScriptedScreen? {
        if (screenId == ThemeAnalyzer.HUD) return null // the HUD is not a LuaScriptedScreen
        return getAllScreens(screenId)[themeId]?.let { callback ->
            LuaScriptedScreen(screenId, themeId).also {
                it.load(callback)
            }
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
