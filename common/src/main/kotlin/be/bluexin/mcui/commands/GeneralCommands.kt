package be.bluexin.mcui.commands

import be.bluexin.mcui.screens.LuaScriptedScreen
import be.bluexin.mcui.themes.loader.AbstractThemeLoader
import be.bluexin.mcui.util.Client
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

sealed class GeneralCommands(literal: String) : McuiCommand(literal) {
    data object PrintErrors : GeneralCommands("print_errors") {
        override fun register(): CommandRegistrar =
            literal(literal)
                .executes { context ->
                    AbstractThemeLoader.Reporter.errors.forEach {
                        context.source.sendSystemMessage(Component.literal(it))
                    }
                    AbstractThemeLoader.Reporter.errors.size
                }
    }

    data object Settings : GeneralCommands("settings") {

        private val screenId = ResourceLocation("mcui", "settings")
        private val missingScreenError =
            DynamicCommandExceptionType { Component.translatable("mcui.commands.missingscreen", it) }

        private fun openSettings(
            // For reference syntax
            @Suppress("UNUSED_PARAMETER")
            context: CommandContext<CommandSourceStack>
        ): Int {
            Client.tell {
                it.setScreen(LuaScriptedScreen(screenId))
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
