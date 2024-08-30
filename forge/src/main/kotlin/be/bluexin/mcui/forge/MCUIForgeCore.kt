package be.bluexin.mcui.forge

import be.bluexin.mcui.Constants
import be.bluexin.mcui.MCUICore
import be.bluexin.mcui.commands.McuiCommand
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.fml.common.Mod

@Mod(Constants.MOD_ID)
object MCUIForgeCore {
    init {
        MCUICore.init()

        MinecraftForge.EVENT_BUS.addListener(MCUIForgeCore::registerCommands)
    }

    private fun registerCommands(event: RegisterCommandsEvent) {
        McuiCommand.setup(event.dispatcher)
    }
}