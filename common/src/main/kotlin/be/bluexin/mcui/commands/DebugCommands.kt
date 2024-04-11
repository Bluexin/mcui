package be.bluexin.mcui.commands

import be.bluexin.mcui.Constants
import be.bluexin.mcui.api.scripting.LuaJTest
import be.bluexin.mcui.api.scripting.RegisterScreen
import be.bluexin.mcui.screens.LuaScriptedScreen
import be.bluexin.mcui.screens.LuaTestScreen
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.client.Minecraft
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

@Suppress("unused") // automatic
enum class DebugCommands(
    override vararg val arguments: ArgumentBuilder<CommandSourceStack, *>
) : Command {
    OPEN_TEST_GUI {
        override fun execute(c: CommandContext<CommandSourceStack>): Int {
            Minecraft.getInstance().tell {
                Minecraft.getInstance().setScreen(LuaTestScreen())
            }
            return 1
        }
    },
    RELOAD_SCRIPTS {
        override fun execute(c: CommandContext<CommandSourceStack>): Int {
            try {
                LuaJTest.runScript(ResourceLocation("mcui", "themes/hex2/screens.lua"))
                return 1
            } catch (e: Throwable) {
                Minecraft.getInstance().player?.sendSystemMessage(Component.literal("Something went wrong : ${e.message}. See console for more info."))
                Constants.LOG.error("Couldn't evaluate screens.lua", e)
                return 0
            }
        }
    },
    OPEN_SCRIPT_GUI(
        // FIXME : this doesn't work (:
        Commands.argument("screen", ResourceLocationArgument.id())
            .suggests { _, builder ->
                RegisterScreen.allIds().forEach {
                    if (it.toString().startsWith(builder.remaining)) builder.suggest(it.toString())
                }

                builder.buildFuture()
            }
    ) {
        private val missingScreenError =
            SimpleCommandExceptionType(Component.translatable("mcui.commands.open.missingscreen"))

        override fun execute(c: CommandContext<CommandSourceStack>): Int {
            val screenId = ResourceLocationArgument.getId(c, "screen")
            if (RegisterScreen[screenId] == null) throw missingScreenError.create()
            Minecraft.getInstance().tell {
                Minecraft.getInstance().setScreen(LuaScriptedScreen(screenId))
            }

            return 1
        }
    };

    override val id = "debug.${name.lowercase()}"

    /*fun getUsage(sender: ICommandSender): String {
        return "commands.general.${name.lowercase()}.usage"
    }*/

    companion object : Command.Commands {
        override val values = entries.asIterable()
        val indexedValues = values.associateBy(DebugCommands::id)
    }
}