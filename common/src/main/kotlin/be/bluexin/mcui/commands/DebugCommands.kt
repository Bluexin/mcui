package be.bluexin.mcui.commands

import be.bluexin.mcui.Constants
import be.bluexin.mcui.screens.LuaScriptedScreen
import be.bluexin.mcui.screens.LuaTestScreen
import be.bluexin.mcui.themes.meta.ThemeManager
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
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

sealed class DebugCommands(usage: String) : McuiCommand(usage) {
    data object Reload : DebugCommands("reload"), KoinComponent {
        private val luaJManager: LuaJManager by inject()
        private val themeManager: ThemeManager by inject()

        private fun reloadAll(commandContext: CommandContext<CommandSourceStack>): Int =
            reloadScripts(commandContext)

        private fun reloadScripts(commandContext: CommandContext<CommandSourceStack>): Int = try {
            themeManager.themeList.values.forEach {
                it.scripts[it.themeResource("theme")]
                    ?.let(luaJManager::runScript)
                    ?.let { retVal ->
                        val result = retVal.arg1().checkboolean()
                        val resultValue = retVal.arg(2)

                        if (result) commandContext.source.sendSuccess(
                            Component.literal("Successfully reloaded scripts for ${it.id}, result: $resultValue"),
                            false
                        ) else commandContext.source.sendFailure(
                            Component.literal("Failed to reload scripts for ${it.id} : $resultValue")
                        )
                    }
            }
            commandContext.source.sendSuccess(Component.literal("Reloaded scripts"), false)

            1
        } catch (e: Throwable) {
            commandContext.source.sendFailure(Component.literal("Something went wrong : ${e.message}. See console for more info."))
            Constants.LOG.error("Couldn't reload scripts", e)

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
