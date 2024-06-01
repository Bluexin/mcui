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

package be.bluexin.mcui.themes.elements

import be.bluexin.luajksp.annotations.LuajExpose
import be.bluexin.mcui.deprecated.api.themes.IHudDrawContext
import be.bluexin.mcui.themes.elements.access.RepetitionGroupAccess
import be.bluexin.mcui.themes.miniscript.CInt
import be.bluexin.mcui.themes.miniscript.profile
import com.mojang.blaze3d.vertex.PoseStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.luaj.vm2.LuaValue

/**
 * Part of saoui by Bluexin.
 *
 * @author Bluexin
 */
@Serializable
@SerialName("repetitionGroup")
@LuajExpose(LuajExpose.IncludeType.OPT_IN)
class RepetitionGroup(
    @SerialName("amount")
    @XmlSerialName("amount")
    @LuajExpose
    var amount: CInt
) : ElementGroupParent() {

    override fun draw(ctx: IHudDrawContext, poseStack: PoseStack, mouseX: Double, mouseY: Double) {
        if (!enabled(ctx)) return

        prepareDraw(ctx, poseStack)

        val relMouseX = mouseX - x(ctx)
        val relMouseY = mouseY - y(ctx)

        val m = amount(ctx)
        for (i in 0 until m) {
            ctx.profile(i.toString()) {
                ctx.setI(i)
                super.drawChildren(ctx, poseStack, relMouseX, relMouseY)
            }
        }

        finishDraw(ctx, poseStack)
    }

    override fun toLua(): LuaValue = RepetitionGroupAccess(this)
}
