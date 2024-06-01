package be.bluexin.mcui.commands

import be.bluexin.mcui.screens.LuaScriptedScreen
import be.bluexin.mcui.themes.loader.AbstractThemeLoader
import be.bluexin.mcui.themes.scripting.lib.RegisterScreen
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import net.minecraft.client.Minecraft
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

@Suppress("unused") // automatic
enum class GeneralCommands(
    override vararg val arguments: ArgumentBuilder<CommandSourceStack, *>
) : Command {
    PRINT_ERRORS {
        override fun execute(c: CommandContext<CommandSourceStack>): Int {
            AbstractThemeLoader.Reporter.errors.forEach {
                c.source.sendSystemMessage(Component.literal(it))
            }
            return AbstractThemeLoader.Reporter.errors.size
        }
    },
    SETTINGS {
        private val screenId = ResourceLocation("mcui", "settings")
        private val missingScreenError =
            DynamicCommandExceptionType { Component.translatable("mcui.commands.missingscreen", it) }

        override fun execute(c: CommandContext<CommandSourceStack>): Int {
            if (RegisterScreen[screenId] == null) throw missingScreenError.create(screenId.toString())
            Minecraft.getInstance().tell {
                Minecraft.getInstance().setScreen(LuaScriptedScreen(screenId))
            }

            return 1
        }
    };

    override val id = "general.${name.lowercase()}"

    /*fun getUsage(sender: ICommandSender): String {
        return "commands.general.${name.lowercase()}.usage"
    }*/

    companion object : Command.Commands {
        override val values = entries.asIterable()
        val indexedValues = values.associateBy(GeneralCommands::id)
    }
}