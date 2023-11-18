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

import be.bluexin.mcui.api.themes.IHudDrawContext
import be.bluexin.mcui.themes.util.CUnit
import com.mojang.blaze3d.vertex.PoseStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.resources.ResourceLocation

/**
 * Part of saoui by Bluexin, released under GNU GPLv3.
 *
 * @author Bluexin
 */
@Serializable
@SerialName("rawElement")
class RawElement(private var expression: CUnit) : Element() {

    @Transient
    private var rl: ResourceLocation? = null
    private val texture: String? = null

    override fun setup(parent: ElementParent, fragments: Map<ResourceLocation, () -> Fragment>): Boolean {
        val anonymous = super.setup(parent, fragments)
        if (this.texture != null) this.rl = ResourceLocation(this.texture)
        return anonymous
    }

    override fun draw(ctx: IHudDrawContext, poseStack: PoseStack) {
        poseStack.pushPose()
        val p = parentOrZero
        val x = this.x(ctx) + p.getX(ctx)
        val y = this.y(ctx) + p.getY(ctx)
        val z = this.z(ctx) + p.getZ(ctx) + ctx.z
        poseStack.translate(x, y, z)
        expression(ctx)
        poseStack.popPose()
    }
}
