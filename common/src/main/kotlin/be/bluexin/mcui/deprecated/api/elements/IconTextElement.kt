package be.bluexin.mcui.deprecated.api.elements

import be.bluexin.mcui.GLCore
import be.bluexin.mcui.config.OptionCore
import be.bluexin.mcui.util.ColorUtil
import be.bluexin.mcui.util.IconCore
import be.bluexin.mcui.util.math.Vec2d
import be.bluexin.mcui.util.math.vec
import com.mojang.blaze3d.vertex.PoseStack

class IconTextElement(var text: String, override val description: MutableList<String> = mutableListOf()) : IconElement(IconCore.NONE) {

    init {
        if (text.length > 3) text = text.substring(0, 2)
    }

    override fun draw(poseStack: PoseStack, mouse: Vec2d, partialTicks: Float) {
        super.draw(poseStack, mouse, partialTicks)
        val color = ColorUtil.multiplyAlpha(getTextColor(mouse), opacity)
        GLCore.color(color)
        if (this.highlighted || mouse in this) {
            GLCore.glString(text, pos + vec(width / 2 - GLCore.glStringWidth(text) / 2, height / 2), color, shadow = OptionCore.TEXT_SHADOW.isEnabled, centered = true,
                poseStack = poseStack
            )
        } else {
            GLCore.glString(text, pos + vec(width / 2 - GLCore.glStringWidth(text) / 2, height / 2), color, shadow = false, centered = true,
                poseStack = poseStack
            )
        }
        if (mouse in this) {
            drawHoveringText(poseStack, mouse)
            GLCore.lighting(false)
            GLCore.depth(false)
        }
    }
}
