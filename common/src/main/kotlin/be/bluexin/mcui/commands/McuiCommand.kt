package be.bluexin.mcui.commands

import be.bluexin.mcui.Constants
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.literal

typealias CommandRegistrar = LiteralArgumentBuilder<CommandSourceStack>

abstract class McuiCommand(
    val literal: String
) {
    abstract fun register(): CommandRegistrar
    open val defaultUseCommand: String get() = literal

    companion object {
        private const val ROOT = Constants.MOD_ID

        fun setup(dispatcher: CommandDispatcher<CommandSourceStack>) {
            dispatcher.register(
                literal(ROOT)
                    .apply(DebugCommands::register)
                    .apply(GeneralCommands::register)
            )
        }

        fun useCommand(command: McuiCommand) = "/$ROOT ${command.defaultUseCommand}"
    }
}
