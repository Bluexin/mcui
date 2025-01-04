package be.bluexin.mcui.screens

import be.bluexin.mcui.themes.meta.ThemeManager
import be.bluexin.mcui.themes.miniscript.HudDrawContext
import be.bluexin.mcui.themes.miniscript.PartialTicksTracker
import be.bluexin.mcui.themes.miniscript.PoseStackTracker
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.components.DebugScreenOverlay
import net.minecraft.util.Mth
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * TODO : Forge got a fancy system now with Gui Overlay registering etc.
 * Need to find a way to properly abstract that & Fabric version
 */
class McuiGui(private val mc: Minecraft) : Gui(mc, mc.itemRenderer), KoinComponent {
    private val context = HudDrawContext(mc)
    private val themeManager by inject<ThemeManager>()
    private val partialTicksTracker by inject<PartialTicksTracker>()
    private val poseStackTracker by inject<PoseStackTracker>()

    private val debugScreen by lazy { DebugScreenOverlay(mc) }

    override fun render(poseStack: PoseStack, partialTick: Float) {
        context.setTime(partialTick)
        partialTicksTracker.partialTicks = partialTick
        poseStackTracker.poseStack = poseStack
        poseStackTracker.withStack(poseStack) {
            themeManager.HUD.drawAll(context, poseStack)
        }

        mc.profiler.push("chat")
        val window = mc.window
        val n = Mth.floor(mc.mouseHandler.xpos() * window.guiScaledWidth / window.screenWidth)
        val p = Mth.floor(mc.mouseHandler.ypos() * window.guiScaledHeight / window.screenHeight)
        chat.render(poseStack, this.tickCount, n, p)
        mc.profiler.pop()

        if (mc.options.renderDebug) debugScreen.render(poseStack)
    }
}