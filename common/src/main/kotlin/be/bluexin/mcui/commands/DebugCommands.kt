package be.bluexin.mcui.commands

import be.bluexin.mcui.Constants
import be.bluexin.mcui.screens.LuaScriptedScreen
import be.bluexin.mcui.screens.LuaTestScreen
import be.bluexin.mcui.themes.scripting.LuaJManager
import be.bluexin.mcui.themes.scripting.lib.RegisterScreen
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.client.Minecraft
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.commands.arguments.ResourceLocationArgument.id
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

sealed class DebugCommands(usage: String) : McuiCommand(usage) {
    data object Reload : DebugCommands("reload"), KoinComponent {
        private val luaJManager: LuaJManager by inject()

        private fun reloadAll(commandContext: CommandContext<CommandSourceStack>): Int =
            reloadScripts(commandContext)

        private fun reloadScripts(commandContext: CommandContext<CommandSourceStack>): Int = try {
            luaJManager.runScript(ResourceLocation("mcui", "themes/hex2/screens.lua"))
            commandContext.source.sendSuccess(Component.literal("Reloaded scripts"), false)

            1
        } catch (e: Throwable) {
            commandContext.source.sendFailure(Component.literal("Something went wrong : ${e.message}. See console for more info."))
            Constants.LOG.error("Couldn't evaluate screens.lua", e)

            0
        }

        override fun register(): CommandRegistrar = literal(literal)
            .executes(::reloadAll)
            .then(literal("all").executes(::reloadAll))
            .then(literal("scripts").executes(::reloadScripts))
    }

    data object Open : DebugCommands("open") {
        private val missingScreenError =
            SimpleCommandExceptionType(Component.translatable("mcui.commands.open.missingscreen"))

        override fun register(): CommandRegistrar = literal(literal).then(
            argument("screen", id())
                .suggests { _, builder ->
                    SharedSuggestionProvider.suggestResource(RegisterScreen.allIds(), builder)
                }
                .executes { context: CommandContext<CommandSourceStack> ->
                    val screenId = ResourceLocationArgument.getId(context, "screen")
                    if (RegisterScreen[screenId] == null) throw missingScreenError.create()
                    Minecraft.getInstance().tell {
                        Minecraft.getInstance().setScreen(LuaScriptedScreen(screenId))
                    }

                    1
                }
        )
    }

    data object TestGui : DebugCommands("testgui") {
        override fun register(): CommandRegistrar = literal(literal).executes {
            Minecraft.getInstance().tell {
                Minecraft.getInstance().setScreen(LuaTestScreen())
            }
            1
        }
    }

    override val defaultUseCommand = "$DEBUG_LITERAL $literal"

    companion object {
        private const val DEBUG_LITERAL = "debug"

        fun register(builder: CommandRegistrar): CommandRegistrar = builder.then(
            literal(DEBUG_LITERAL)
                .then(Reload.register())
                .then(Open.register())
                .then(TestGui.register())
        )
    }
}
