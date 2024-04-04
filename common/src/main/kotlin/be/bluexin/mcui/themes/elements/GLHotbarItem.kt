/*
 * Copyright (C) 2016-2019 Arnaud 'Bluexin' Solé
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

package be.bluexin.mcui.themes.elements

import be.bluexin.mcui.GLCore
import be.bluexin.mcui.api.themes.IHudDrawContext
import be.bluexin.mcui.themes.util.CDouble
import be.bluexin.mcui.themes.util.CInt
import com.mojang.blaze3d.vertex.PoseStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.world.entity.HumanoidArm
import net.minecraft.world.item.ItemStack
import nl.adaptivity.xmlutil.serialization.XmlSerialName

/**
 * Part of saoui by Bluexin.

 * @author Bluexin
 */
@Serializable
@SerialName("glHotbarItem")
class GLHotbarItem(
    @SerialName("slot")
    @XmlSerialName("slot")
    private var slot: CInt,
    @SerialName("itemXoffset")
    @XmlSerialName("itemXoffset")
    private var itemXoffset: CInt,
    @SerialName("itemYoffset")
    @XmlSerialName("itemYoffset")
    private var itemYoffset: CInt,
    @SerialName("hand")
    @XmlSerialName("hand")
    private var hand: HumanoidArm? = null
) : GLRectangleParent() {

    /*
    From net.minecraft.client.gui.GuiIngame
    1.19.4 : net.minecraft.client.gui.Gui.renderSlot
     */
    private fun renderHotbarItem(
        x: Int,
        y: Int,
        partialTicks: Float,
        stack: ItemStack,
        ctx: IHudDrawContext,
        poseStack: PoseStack
    ) {
        if (stack.isEmpty) return
        val f = stack.popTime - partialTicks

        if (f > 0.0f) {
            poseStack.pushPose()
            val f1 = 1.0f + f / 5.0f
            poseStack.translate((x + 8).toFloat(), (y + 12).toFloat(), 0.0f)
            poseStack.scale(1.0f / f1, (f1 + 1.0f) / 2.0f, 1.0f)
            poseStack.translate((-(x + 8)).toFloat(), (-(y + 12)).toFloat(), 0.0f)
        }

        GLCore.color(0xFFFFFFFFu.toInt())
        ctx.itemRenderer.renderGuiItem(poseStack, stack, x, y)

        if (f > 0.0f) poseStack.popPose()

        ctx.itemRenderer.renderGuiItemDecorations(poseStack, ctx.fontRenderer, stack, x, y)
    }

    override fun draw(ctx: IHudDrawContext, poseStack: PoseStack) {
        if (!enabled(ctx) || hand == ctx.player.mainArm) return
        super.draw(ctx, poseStack)

        val pushed = scale?.let {
            val scale = it(ctx).toFloat()
            poseStack.pushPose()
            poseStack.scale(scale, scale, scale)
            true
        } ?: false
        val it: ItemStack = if (hand == null) ctx.player.inventory.items[slot(ctx)]
        else ctx.player.inventory.offhand[slot(ctx)]

        if (it == ItemStack.EMPTY) return

//        GLCore.glBlend(false)
//        GLCore.glRescaleNormal(true)
//        RenderHelper.enableGUIStandardItemLighting()

        renderHotbarItem(
            (x(ctx) + itemXoffset(ctx)).toInt(),
            (y(ctx) + itemYoffset(ctx)).toInt(),
            ctx.partialTicks,
            it, ctx, poseStack
        )

        if (pushed) poseStack.popPose()

//        GLCore.glRescaleNormal(false)
//        RenderHelper.disableStandardItemLighting()
//        GLCore.glBlend(true)
    }
}
