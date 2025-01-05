package be.bluexin.mcui.commands

import be.bluexin.mcui.logger
import be.bluexin.mcui.themes.loader.AbstractThemeLoader
import be.bluexin.mcui.themes.meta.ThemeAnalyzer
import be.bluexin.mcui.themes.meta.ThemeManager
import be.bluexin.mcui.util.Client
import be.bluexin.mcui.util.error
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.Component
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

sealed class GeneralCommands(literal: String) : McuiCommand(literal) {
    data object PrintErrors : GeneralCommands("print_errors") {
        private val logger = logger()

        override fun register(): CommandRegistrar =
            literal(literal)
                .executes { context ->
                    if (AbstractThemeLoader.Reporter.errors.any()) {
                        context.source.sendSystemMessage(Component.literal("(See console with monospaced fonts for precise error markup)"))
                        AbstractThemeLoader.Reporter.errors.forEach {
                            context.source.sendSystemMessage(Component.literal(it))
                            logger.error { it }
                        }
                    }
                    AbstractThemeLoader.Reporter.errors.size
                }
    }

    data object Settings : GeneralCommands("settings"), KoinComponent {
        private val themeManager: ThemeManager by inject()

        private fun openSettings(
            // For reference syntax
            @Suppress("UNUSED_PARAMETER")
            context: CommandContext<CommandSourceStack>
        ): Int {
            Client.tell {
                it.setScreen(themeManager.getScreen(ThemeAnalyzer.MCUI_SETTINGS))
            }

            return 1
        }

        override fun register(): CommandRegistrar = literal(literal)
            .executes(::openSettings)
            .then(literal("open").executes(::openSettings))
            .then(literal("get").executes {
                it.source.sendFailure(Component.literal("\"get\" not yet implemented"))
                0
            })
            .then(literal("set").executes {
                it.source.sendFailure(Component.literal("\"set\" not yet implemented"))
                0
            })
    }

    companion object {
        fun register(builder: CommandRegistrar): CommandRegistrar = builder
            .then(PrintErrors.register())
            .then(Settings.register())
    }
}
