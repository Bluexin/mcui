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

package be.bluexin.mcui.themes

import be.bluexin.mcui.commands.GeneralCommands
import be.bluexin.mcui.commands.SaouiCommand
import be.bluexin.mcui.config.ConfigHandler
import be.bluexin.mcui.config.Settings
import be.bluexin.mcui.themes.elements.Hud
import be.bluexin.mcui.util.Client
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener

/**
 * Part of saoui by Bluexin.
 *
 * @author Bluexin
 */
object ThemeManager : ResourceManagerReloadListener {

    // TODO: tests
    // TODO: theme versions
    // TODO: loading reporter (amount of issues, details, missing keys, ..?)

    lateinit var HUD: Hud
    lateinit var themeList: Map<ResourceLocation, ThemeMetadata>
        private set
    lateinit var currentTheme: ThemeMetadata
        private set
    private var isReloading = false

    fun load(theme: ResourceLocation = ConfigHandler.currentTheme) {
        val oldTheme = ConfigHandler.currentTheme
        currentTheme = themeList[theme] ?: themeList[oldTheme] ?: themeList[ConfigHandler.DEFAULT_THEME]!!

        Settings.unregister(oldTheme)
        ConfigHandler.currentTheme = currentTheme.id

        currentTheme.type.loader().load(currentTheme)
        reportLoading()

//        if (!isReloading) GLCore.setFont(Client.mc, OptionCore.CUSTOM_FONT.isEnabled)
    }

    // TODO: this supports async stuff now
    override fun onResourceManagerReload(resourceManager: ResourceManager) {
        themeList = ThemeDetector.listThemes()
        isReloading = true
        load()
        isReloading = false
    }

    private fun reportLoading() {
        Client.mc.chatListener.let {
            val style = Style.EMPTY
                .withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, SaouiCommand.useCommand(GeneralCommands.PRINT_ERRORS)))

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
