package be.bluexin.mcui.commands

import be.bluexin.mcui.themes.loader.AbstractThemeLoader
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component

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