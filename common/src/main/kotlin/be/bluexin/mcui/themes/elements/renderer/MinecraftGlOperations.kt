package be.bluexin.mcui.themes.elements.renderer

import be.bluexin.mcui.GLCore
import be.bluexin.mcui.themes.elements.GLOperations
import be.bluexin.mcui.themes.miniscript.api.MiniscriptItemStack
import be.bluexin.mcui.themes.miniscript.api.MiniscriptItemStackImpl
import be.bluexin.mcui.util.Client
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.resources.ResourceLocation

class MinecraftGlOperations(
    private val poseStack: PoseStack,
) : GLOperations {
    override fun configureBlend(enabled: Boolean) {
        GLCore.glBlend(enabled)
    }

    override fun setColor(red: Float, green: Float, blue: Float, alpha: Float) {
        GLCore.color(red, green, blue, alpha)
    }

    override fun setColor(rgba: Int) {
        GLCore.color(rgba)
    }

    override fun getShaderColor(): FloatArray {
        return GLCore.getShaderColor()
    }

    override fun bindTexture(texture: () -> ResourceLocation) {
        GLCore.glBindTexture(texture())
    }

    override fun drawString(string: String, x: Float, y: Float, rgba: Int, shadow: Boolean, centered: Boolean) {
        GLCore.glString(string, x.toInt(), y.toInt(), rgba, shadow, centered, poseStack)
    }

    override fun drawRectangle(
        x: Double,
        y: Double,
        z: Double,
        width: Double,
        height: Double,
        sourceX: Double,
        sourceY: Double,
        sourceWidth: Double,
        sourceHeight: Double,
        textureWidth: Int,
        textureHeight: Int
    ) {
        GLCore.glTexturedRectV2(
            x,
            y,
            z,
            width,
            height,
            sourceX,
            sourceY,
            sourceWidth,
            sourceHeight,
            textureWidth,
            textureHeight,
            poseStack
        )
    }

    override fun renderItemStack(
        x: Int,
        y: Int,
        partialTicks: Float,
        stack: MiniscriptItemStack
    ) {
        val mcStack = (stack as MiniscriptItemStackImpl).itemStack
        val f = mcStack.popTime - partialTicks

        if (f > 0.0f) {
            poseStack.pushPose()
            val f1 = 1.0f + f / 5.0f
            poseStack.translate((x + 8).toFloat(), (y + 12).toFloat(), 0.0f)
            poseStack.scale(1.0f / f1, (f1 + 1.0f) / 2.0f, 1.0f)
            poseStack.translate((-(x + 8)).toFloat(), (-(y + 12)).toFloat(), 0.0f)
        }

        setColor(0xFFFFFFFF.toInt())
        Client.mc.itemRenderer.renderAndDecorateItem(poseStack, mcStack, x, y)

        if (f > 0.0f) poseStack.popPose()

        Client.mc.itemRenderer.renderGuiItemDecorations(poseStack, Client.mc.font, mcStack, x, y)
    }
}