package be.bluexin.mcui.fabric

import be.bluexin.mcui.Constants
import be.bluexin.mcui.MCUICore
import be.bluexin.mcui.commands.McuiCommand
import be.bluexin.mcui.screens.McuiGui
import be.bluexin.mcui.themes.meta.ThemeManager
import be.bluexin.mcui.themes.meta.ThemeMetadata
import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.util.profiling.ProfilerFiller
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

@Suppress("unused")
object MCUIFabricCore : ClientModInitializer, KoinComponent {

    private val themeManager by inject<ThemeManager>()

    override fun onInitializeClient() {
        MCUICore.init()

        ModConfigEvents.loading(Constants.MOD_ID).register {
            Constants.LOG.info("Loading config ${it.fullPath}")
        }
        ModConfigEvents.reloading(Constants.MOD_ID).register {
            Constants.LOG.info("Reloading config ${it.fullPath}")
        }

        CommandRegistrationCallback.EVENT.register { commandDispatcher, _, _ ->
            McuiCommand.setup(commandDispatcher)
        }

        // Only in Fabric for now
        Minecraft.getInstance().tell {
            Minecraft.getInstance().gui = McuiGui(Minecraft.getInstance())
        }

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
            .registerReloadListener(object : SimpleResourceReloadListener<Map<ResourceLocation, ThemeMetadata>> {
                private val id = ResourceLocation(Constants.MOD_ID, "theme_reload_listener")

                override fun getFabricId() = id

                override fun load(
                    manager: ResourceManager,
                    profiler: ProfilerFiller,
                    executor: Executor
                ) = CompletableFuture.supplyAsync({
                    themeManager.loadData(manager)
                }, executor)

                override fun apply(
                    data: Map<ResourceLocation, ThemeMetadata>,
                    manager: ResourceManager,
                    profiler: ProfilerFiller,
                    executor: Executor
                ) = CompletableFuture.runAsync({
                    themeManager.applyData(data, manager)
                }, executor)
            })
    }
}
