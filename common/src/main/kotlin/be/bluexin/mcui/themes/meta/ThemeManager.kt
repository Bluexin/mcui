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

import be.bluexin.mcui.Constants
import be.bluexin.mcui.commands.GeneralCommands
import be.bluexin.mcui.commands.McuiCommand
import be.bluexin.mcui.config.ConfigHandler
import be.bluexin.mcui.themes.elements.Hud
import be.bluexin.mcui.themes.loader.AbstractThemeLoader
import be.bluexin.mcui.themes.scripting.lib.RegisterScreen
import be.bluexin.mcui.util.Client
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
    private val themeDetector: ThemeDetector
) {

    // TODO: tests
    // TODO: theme format versions
    // TODO: loading reporter (amount of issues, details, missing keys, ..?)

    lateinit var HUD: Hud
    lateinit var themeList: Map<ResourceLocation, ThemeDefinition>
        private set

    // TODO : this should eventually be replaced with a list of loaded themes and the screens they provide
    // combined with the user setting the theme providing each screen
    var currentTheme: ThemeDefinition = ThemeDefinition(
        id = ResourceLocation(Constants.MOD_ID, "debug"),
        themeRoot = ResourceLocation(Constants.MOD_ID, "debug"),
        name = "mcui.debug",
        metadata = ThemeMetadata(
            version = "debug",
            format = ThemeFormat.ERROR
        ),
        hud = null,
        settings = null,
        fragments = emptyMap(),
        widgets = emptyMap(),
        scripts = emptyMap(),
    )
        private set
    private var isReloading = false

    fun load(resourceManager: ResourceManager, theme: ResourceLocation = ConfigHandler.currentTheme) {
        val oldTheme = ConfigHandler.currentTheme
        currentTheme = themeList[theme] ?: themeList[oldTheme] ?: themeList[ConfigHandler.DEFAULT_THEME]!!

        ConfigHandler.currentTheme = currentTheme.id

        currentTheme.hud
            ?.let(HudFormat::fromFile)?.loader
            // FIXME : this should probably not be loading the whole theme this way
            ?.load(resourceManager, currentTheme) { HUD = it }
        reportLoading()

//        if (!isReloading) GLCore.setFont(Client.mc, OptionCore.CUSTOM_FONT.isEnabled)
    }

    fun applyData(data: Map<ResourceLocation, ThemeDefinition>, resourceManager: ResourceManager) {
        RegisterScreen.clear()
        themeList = data
        isReloading = true
        load(resourceManager)
        isReloading = false
    }

    fun loadData(resourceManager: ResourceManager): Map<ResourceLocation, ThemeDefinition> =
        themeDetector.listThemes(resourceManager)
    // TODO : analyse themes

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
                Component.translatableWithFallback(
                    currentTheme.nameTranslationKey,
                    currentTheme.name
                ).apply {
                    this.style = Style.EMPTY.withHoverEvent(
                        HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            Component.literal(currentTheme.id.toString())
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
}
