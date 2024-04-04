package be.bluexin.mcui.fabric

import be.bluexin.mcui.CommonClass
import be.bluexin.mcui.Constants
import be.bluexin.mcui.commands.McuiCommand
import be.bluexin.mcui.screens.ingame.McuiGui
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
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

@Suppress("unused")
object MCUIFabricCore : ClientModInitializer {
    override fun onInitializeClient() {
        ModConfigEvents.loading(Constants.MOD_ID).register {
            Constants.LOG.info("Loading config ${it.fullPath}")
        }
        ModConfigEvents.reloading(Constants.MOD_ID).register {
            Constants.LOG.info("Reloading config ${it.fullPath}")
        }

        CommonClass.init()
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
                    ThemeManager.loadData(manager)
                }, executor)

                override fun apply(
                    data: Map<ResourceLocation, ThemeMetadata>,
                    manager: ResourceManager,
                    profiler: ProfilerFiller,
                    executor: Executor
                ) = CompletableFuture.runAsync({
                    ThemeManager.applyData(data, manager)
                }, executor)
            })
    }
}
