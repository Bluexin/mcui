package be.bluexin.mcui.commands

import be.bluexin.mcui.Constants
import be.bluexin.mcui.themes.meta.ThemeManager
import be.bluexin.mcui.util.Client
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
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
        private val themeManager: ThemeManager by inject()

        private fun reloadAll(commandContext: CommandContext<CommandSourceStack>): Int =
            reloadScripts(commandContext)

        private fun reloadScripts(commandContext: CommandContext<CommandSourceStack>): Int = try {
            // TODO : don't reload theme settings here
            themeManager.reloadThemes({
                commandContext.source.sendSuccess(Component.literal(it()), false)
            }) {
                commandContext.source.sendFailure(Component.literal(it()))
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

    data object Open : DebugCommands("open"), KoinComponent {
        private val missingScreenError =
            SimpleCommandExceptionType(Component.translatable("mcui.commands.open.missingscreen"))
        private val conflictingScreenError =
            SimpleCommandExceptionType(Component.translatable("mcui.commands.open.conflictingscreen"))

        private val themeManager: ThemeManager by inject()

        override fun register(): CommandRegistrar = literal(literal).then(
            argument("screen", id())
                .suggests { _, builder ->
                    SharedSuggestionProvider.suggestResource(themeManager.allScreenIds, builder)
                }
                .executes { context ->
                    val screenId = ResourceLocationArgument.getId(context, "screen")
                    val implementations = themeManager.getAllScreens(screenId)

                    when (implementations.size) {
                        0 -> throw missingScreenError.create()
                        1 -> Client.tell {
                            it.setScreen(themeManager.getThemeScreen(screenId, implementations.keys.single()))
                        }

                        else -> {
                            context.source.sendFailure(Component.literal("Options : ${implementations.keys}"))
                            throw conflictingScreenError.create()
                        }
                    }

                    1
                }.then(
                    argument("theme", id())
                        .suggests { context, builder ->
                            val screenId = ResourceLocationArgument.getId(context, "screen")
                            val implementations = themeManager.getAllScreens(screenId)
                            SharedSuggestionProvider.suggestResource(implementations.keys, builder)
                        }
                        .executes { context ->
                            val screenId = ResourceLocationArgument.getId(context, "screen")
                            val themeId = ResourceLocationArgument.getId(context, "theme")
                            val implementations = themeManager.getAllScreens(screenId)

                            if (themeId in implementations) {
                                Client.tell {
                                    it.setScreen(themeManager.getThemeScreen(screenId, themeId))
                                }
                            } else {
                                context.source.sendFailure(Component.literal("Screen $screenId not defined by theme $themeId (themes defining this screen : ${implementations.keys.joinToString()})"))
                                throw missingScreenError.create()
                            }

                            1
                        }
                )
        )
    }

    override val defaultUseCommand = "$DEBUG_LITERAL $literal"

    companion object {
        private const val DEBUG_LITERAL = "debug"

        fun register(builder: CommandRegistrar): CommandRegistrar = builder.then(
            literal(DEBUG_LITERAL)
                .then(Reload.register())
                .then(Open.register())
        )
    }
}
