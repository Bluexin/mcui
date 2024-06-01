/*
 * Copyright (C) 2016-2024 Arnaud 'Bluexin' Solé
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package be.bluexin.mcui

import be.bluexin.mcui.util.Client
import com.tencao.saomclib.SAOMCLib
import com.tencao.saomclib.capabilities.CapabilitiesHandler
import be.bluexin.mcui.deprecated.elements.registry.ElementRegistry
import be.bluexin.mcui.api.events.EventInitStatsProvider
import be.bluexin.mcui.capabilities.RenderCapability
import be.bluexin.mcui.commands.SaouiCommand
import be.bluexin.mcui.config.ConfigHandler
import be.bluexin.mcui.config.FriendData
import be.bluexin.mcui.config.OptionCore
import be.bluexin.mcui.config.Settings
import be.bluexin.mcui.events.EventCore
import be.bluexin.mcui.screens.CoreGUI
import be.bluexin.mcui.themes.ThemeManager
import be.bluexin.mcui.util.AdvancementUtil
import be.bluexin.mcui.util.DefaultStatsProvider
import be.bluexin.mcui.util.PlayerStats
import net.minecraft.client.resources.SimpleReloadableResourceManager
import net.minecraft.world.entity.LivingEntity
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.EventBus
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.util.concurrent.ConcurrentHashMap

@Mod(
    modid = Constants.MOD_ID,
    name = SAOCore.NAME,
    version = SAOCore.VERSION,
    clientSideOnly = true,
    dependencies = SAOCore.DEPS,
    acceptableSaveVersions = "*",
    canBeDeactivated = true,
    modLanguageAdapter = "io.github.chaosunity.forgelin.KotlinAdapter"
)
object SAOCore {
    const val MODID = "saoui"
    const val NAME = "Sword Art Online UI"
    const val VERSION = "2.1.7"
    const val DEPS = "required-after:saomclib@[1.4.8,);after:mantle"

    val mc by lazy { Client.mc }

    val saoConfDir: File by lazy { confDir(File(Client.mc.mcDataDir, "config")) }
    val isSAOMCLibServerSide: Boolean
        get() = SAOMCLib.proxy.isServerSideLoaded

    lateinit var modFile: File

    @JvmStatic
    @Mod.InstanceFactory
    fun shenanigan() = this

    val LOGGER: Logger = LogManager.getLogger(MODID)

    @Mod.EventHandler
    @Throws(Exception::class)
    fun preInit(event: FMLPreInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(EventCore)
        MinecraftForge.EVENT_BUS.register(SoundCore)
        Settings.initialize()
        ConfigHandler
        OptionCore
        FriendData.preInit()
        modFile = event.sourceFile

        CapabilitiesHandler.registerEntityCapability(
            RenderCapability::class.java,
            RenderCapability.Storage()
        ) { `object`: Any -> `object` is LivingEntity }
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        val s = EventInitStatsProvider(DefaultStatsProvider())
        MinecraftForge.EVENT_BUS.post(s)
        PlayerStats.init(s.implementation)
        CoreGUI.animator // Let's force things to init early
        ElementRegistry.initRegistry()
    }

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
        if (Loader.isModLoaded("mantle")) {
            LOGGER.info("Unregistering Mantle health renderer.")
            val f = EventBus::class.java.getDeclaredField("listeners")
            f.isAccessible = true
            val listeners = f.get(MinecraftForge.EVENT_BUS) as ConcurrentHashMap<*, *>
            val handler =
                listeners.keys.firstOrNull { it.javaClass.canonicalName == "slimeknights.mantle.client.ExtraHeartRenderHandler" }
            if (handler == null) LOGGER.warn("Unable to unregister Mantle health renderer!")
            else MinecraftForge.EVENT_BUS.unregister(handler)
        }

        (Client.resourceManager as SimpleReloadableResourceManager).apply {
            registerReloadListener(ThemeManager)
            registerReloadListener(ElementRegistry)
        }

        ClientCommandHandler.instance.registerCommand(SaouiCommand)
    }

    @Mod.EventHandler
    fun loadComplete(event: FMLLoadCompleteEvent) {
        AdvancementUtil.generateList()
        ThemeManager.load()
        // ElementRegistry.initRegistry()
    }

    private fun confDir(genDir: File): File {
        return File(genDir, MODID)
    }
}
