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
import kotlinx.serialization.Transient
import net.minecraft.resources.ResourceLocation
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

/**
 * Part of saoui by Bluexin.

 * @author Bluexin
 */
@Serializable
sealed class GLRectangleParent : Element() {
    @SerialName("rgba")
    @XmlSerialName("rgba")
    protected var rgba: CInt? = null
    @SerialName("srcX")
    @XmlSerialName("srcX")
    protected var srcX= CDouble.ZERO
    @SerialName("srcY")
    @XmlSerialName("srcY")
    protected var srcY= CDouble.ZERO
    @SerialName("w")
    @XmlSerialName("w")
    protected var w= CDouble.ZERO
    @SerialName("h")
    @XmlSerialName("h")
    protected var h= CDouble.ZERO
    @SerialName("srcW")
    @XmlSerialName("srcW")
    protected var srcW= CDouble.ZERO
    @SerialName("srcH")
    @XmlSerialName("srcH")
    protected var srcH= CDouble.ZERO
    @Transient
    protected var rl: ResourceLocation? = null
    @XmlElement
    private val texture: String? = null

    override fun draw(ctx: IHudDrawContext, poseStack: PoseStack) {
        if (!enabled(ctx)) return

        val x = this.x(ctx)
        val y = this.y(ctx)
        val z = this.z(ctx) + ctx.z

        val pushed = scale?.let {
            val scale = it(ctx).toFloat()
            poseStack.pushPose()
            poseStack.scale(scale, scale, scale)
            true
        } ?: false
        GLCore.glBlend(true)
        GLCore.withColor(rgba?.let { it(ctx) }) {
            this.rl?.let(GLCore::glBindTexture)
            GLCore.glTexturedRectV2(
                x, y, z,
                w(ctx), h(ctx),
                srcX(ctx), srcY(ctx),
                srcW(ctx), srcH(ctx),
                poseStack = poseStack
            )
        }
        if (pushed) poseStack.popPose()
    }

    override fun setup(parent: ElementParent, fragments: Map<ResourceLocation, () -> Fragment>): Boolean {
        val anonymous = super.setup(parent, fragments)
        if (this.texture != null) this.rl = ResourceLocation(this.texture)
        return anonymous
    }
}

@Serializable
@SerialName("glRectangle")
class GLRectangle : GLRectangleParent()
