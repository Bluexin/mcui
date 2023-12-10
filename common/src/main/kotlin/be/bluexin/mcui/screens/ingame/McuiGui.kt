package be.bluexin.mcui.screens.ingame

import be.bluexin.mcui.themes.ThemeManager
import be.bluexin.mcui.themes.util.HudDrawContext
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui

/**
 * TODO : Forge got a fancy system now with Gui Overlay registering etc.
 * Need to find a way to properly abstract that & Fabric version
 */
class McuiGui(private val mc: Minecraft) : Gui(mc, mc.itemRenderer) {
    private val context = HudDrawContext(mc)

    override fun render(poseStack: PoseStack, partialTick: Float) {
        context.setTime(partialTick)
        ThemeManager.HUD.drawAll(context, poseStack)
    }
}