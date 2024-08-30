package be.bluexin.mcui.util

import be.bluexin.mcui.Constants
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.server.packs.resources.ResourceManager

object Client {
    val mc: Minecraft get() = Minecraft.getInstance()
    val resourceManager: ResourceManager get() = mc.resourceManager

    fun tell(block: (Minecraft) -> Unit) {
        mc.tell { block(mc) }
    }

    fun showError(message: String, exception: Throwable? = null) {
        if (exception == null) Constants.LOG.error(message)
        else Constants.LOG.error(message, exception)
        mc.player?.sendSystemMessage(Component.literal(message))
        exception?.message?.let { mc.player?.sendSystemMessage(Component.literal(it)) }
    }
}